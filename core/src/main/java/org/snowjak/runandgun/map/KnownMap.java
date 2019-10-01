/**
 * 
 */
package org.snowjak.runandgun.map;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.IntSet.IntSetIterator;

import squidpony.squidgrid.gui.gdx.MapUtility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.GreasedRegion;

/**
 * Like {@link GlobalMap}, but records what an entity has actually seen or been
 * told about.
 * 
 * @author snowjak88
 *
 */
public class KnownMap extends Map {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(KnownMap.class.getName());
	
	private int width = 0, height = 0;
	private short[] known = null;
	
	private short[][] timestamps = null;
	private final java.util.Map<Character, short[]> map = new HashMap<>();
	private final java.util.Map<Color, short[]> colors = new HashMap<>();
	private final java.util.Map<Color, short[]> bgColors = new HashMap<>();
	
	private final java.util.Map<Coord, Collection<Entity>> coordToEntities = new HashMap<>();
	private final java.util.Map<Entity, Coord> entityToCoord = new HashMap<>();
	
	public KnownMap(int width, int height) {
		
		resize(width, height);
	}
	
	/**
	 * If {@code width} and {@code height} do not match this KnownMap's dimensions,
	 * then clears this KnownMap's contents and resizes it.
	 * 
	 * @param width
	 * @param height
	 */
	public void resize(int width, int height) {
		
		if (this.width == width && this.height == height)
			return;
		
		synchronized (this) {
			if (this.width == width && this.height == height)
				return;
			
			this.width = width;
			this.height = height;
			
			clear();
		}
	}
	
	/**
	 * Forget everything in this KnownMap.
	 */
	public void clear() {
		
		synchronized (this) {
			this.known = CoordPacker.ALL_WALL;
			
			this.timestamps = new short[width][height];
			this.map.clear();
			this.colors.clear();
			this.bgColors.clear();
			this.entityToCoord.clear();
			this.coordToEntities.clear();
		}
	}
	
	/**
	 * Set this KnownMap's contents to the given {@code map}. Everywhere where
	 * {@code map[x][y]} is not 0 is understood to be "known". Colors are
	 * initialized with default {@link MapUtility#generateDefaultColors(char[][])
	 * foreground} and {@link MapUtility#generateDefaultBGColors(char[][])
	 * background} colors. All cells are given the given {@code timestamp}
	 * (representing the current game-clock).
	 * <p>
	 * If this KnownMap's dimensions do not match the given {@code map}, then this
	 * method will <strong>resize</strong> this KnownMap to fit.
	 * </p>
	 * <p>
	 * If you need to initialize only <strong>part</strong> of this KnownMap, then
	 * call {@link #setMap(char[][], Color[][], Color[][], float, GreasedRegion)}.
	 * </p>
	 * 
	 * @param map
	 */
	public void setMap(char[][] map, float timestamp) {
		
		synchronized (this) {
			setMap(map, MapUtility.generateDefaultColors(map), MapUtility.generateDefaultBGColors(map), timestamp,
					new GreasedRegion(width, height).not());
		}
	}
	
	/**
	 * Set this KnownMap's contents to the given {@code map}, wherever {@code known}
	 * is {@code true} and {@code map[x][y]} is not 0. All cells are given the given
	 * {@code timestamp} (representing the current game-clock
	 * <p>
	 * If this KnownMap's dimensions do not match the given {@code map}, then this
	 * method will <strong>resize</strong> this KnownMap to fit.
	 * </p>
	 * 
	 * @param map
	 * @param timestamp
	 * @param known
	 */
	public void setMap(char[][] map, Color[][] colors, Color[][] bgColors, float timestamp, GreasedRegion known) {
		
		synchronized (this) {
			resize(map.length, map[0].length);
			
			this.known = CoordPacker.packSeveral(known);
			
			known.mask(timestamps, (short) (timestamp * 1000f));
			
			final IntSet uniqueChars = getUniqueCharacters(map, known);
			final IntSetIterator uniqueIterator = uniqueChars.iterator();
			while (uniqueIterator.hasNext) {
				final char c = (char) uniqueIterator.next();
				this.map.put(c, CoordPacker.intersectPacked(CoordPacker.pack(map, c), this.known));
			}
			
			for (Color c : getUniqueColors(colors, known))
				this.colors.put(c,
						CoordPacker.intersectPacked(CoordPacker.packSeveral(getColorRegion(colors, c)), this.known));
			
			for (Color c : getUniqueColors(colors, known))
				this.bgColors.put(c,
						CoordPacker.intersectPacked(CoordPacker.packSeveral(getColorRegion(bgColors, c)), this.known));
		}
	}
	
	/**
	 * Update this KnownMap with the known contents of the {@code other}, but only
	 * where the {@code other}'s timestamps are younger than this KnownMap's
	 * timestamps.
	 * 
	 * @param other
	 */
	public void updateMap(KnownMap other) {
		
		synchronized (this) {
			LOG.entering(KnownMap.class.getName(), "updateMap(KnownMap)");
			
			other.getKnownRegion().forEach(c -> {
				updateMap(other.getMapAt(c), other.getColorAt(c), other.getBGColorAt(c), c.x, c.y,
						other.getTimestampAt(c));
				
				other.forEntitiesAt(c, (e) -> updateMap(e, c));
			});
			
			LOG.exiting(KnownMap.class.getName(), "updateMap(KnownMap)");
		}
	}
	
	/**
	 * Update this KnownMap in the given {@code updateRegion}, but only if the
	 * individual cells' timestamps are older than the given {@code timestamp}.
	 * 
	 * @param map
	 * @param colors
	 * @param bgColors
	 * @param updateRegion
	 * @param timestamp
	 */
	public void updateMap(Map map, GreasedRegion updateRegion, float timestamp) {
		
		synchronized (this) {
			LOG.entering(KnownMap.class.getName(), "updateMap(Map,GreasedRegion,float)");
			updateRegion.forEach(c -> {
				updateMap(map.getMapAt(c), map.getColorAt(c), map.getBGColorAt(c), c.x, c.y, timestamp);
				map.forEntitiesAt(c, (e) -> updateMap(e, c));
			});
			LOG.exiting(KnownMap.class.getName(), "updateMap(Map,GreasedRegion,float)");
		}
	}
	
	/**
	 * Update this KnownMap at the given location, but only if the known cell's
	 * timestamp is older than the given {@code timestamp}.
	 * 
	 * @param ch
	 * @param mapX
	 * @param mapY
	 * @param timestamp
	 */
	public void updateMap(char ch, Color color, Color bgColor, int mapX, int mapY, float timestamp) {
		
		if (!isInMap(mapX, mapY))
			return;
		
		final short ts = convertTimestamp(timestamp);
		if (ts <= timestamps[mapX][mapY])
			return;
		
		LOG.entering(KnownMap.class.getName(), "updateMap(char,Color,Color,int,int,float)");
		synchronized (this) {
			if (!isInMap(mapX, mapY))
				return;
			
			if (ts <= timestamps[mapX][mapY])
				return;
			
			// LOG.info("Updating known-region ...");
			known = CoordPacker.insertPacked(known, mapX, mapY);
			
			// LOG.info("Updating timestamp ...");
			timestamps[mapX][mapY] = ts;
			
			// LOG.info("Inserting char ...");
			final char oldChar = getMapAt(mapX, mapY);
			final short[] oldChMap = map.getOrDefault(oldChar, CoordPacker.ALL_WALL);
			map.put(oldChar, CoordPacker.removePacked(oldChMap, mapX, mapY));
			
			final short[] newChMap = map.getOrDefault(ch, CoordPacker.ALL_WALL);
			map.put(ch, CoordPacker.insertPacked(newChMap, mapX, mapY));
			
			// LOG.info("Inserting color ...");
			final Color oldColor = getColorAtInternal(mapX, mapY);
			final short[] oldColorMap = colors.getOrDefault(oldColor, CoordPacker.ALL_WALL);
			colors.put(oldColor, CoordPacker.removePacked(oldColorMap, mapX, mapY));
			
			final short[] newColorMap = colors.getOrDefault(color, CoordPacker.ALL_WALL);
			colors.put(color, CoordPacker.insertPacked(newColorMap, mapX, mapY));
			
			// LOG.info("Inserting bg-color ...");
			final Color oldBGColor = getBGColorAtInternal(mapX, mapY);
			final short[] oldBGColorMap = bgColors.getOrDefault(oldBGColor, CoordPacker.ALL_WALL);
			bgColors.put(oldBGColor, CoordPacker.removePacked(oldBGColorMap, mapX, mapY));
			
			final short[] newBGColorMap = bgColors.getOrDefault(bgColor, CoordPacker.ALL_WALL);
			bgColors.put(bgColor, CoordPacker.insertPacked(newBGColorMap, mapX, mapY));
		}
		LOG.exiting(KnownMap.class.getName(), "updateMap(char,Color,Color,int,int,float)");
	}
	
	/**
	 * Update this KnownMap with the given {@link Entity}'s location.
	 * 
	 * @param entity
	 * @param mapX
	 * @param mapY
	 */
	public void updateMap(Entity entity, int mapX, int mapY) {
		
		LOG.entering(KnownMap.class.getName(), "updateMap(Entity,int,int)");
		updateMap(entity, Coord.get(mapX, mapY));
		LOG.exiting(KnownMap.class.getName(), "updateMap(Entity,int,int)");
	}
	
	/**
	 * Update this KnownMap with the given {@link Entity}'s location.
	 * 
	 * @param entity
	 * @param point
	 */
	public void updateMap(Entity entity, Coord point) {
		
		if (!isInMap(point))
			return;
		
		synchronized (this) {
			LOG.entering(KnownMap.class.getName(), "updateMap(Entity,Coord)");
			
			if (!isInMap(point))
				return;
			
			final Coord oldCoord = entityToCoord.get(entity);
			if (oldCoord != null) {
				if (oldCoord == point || (oldCoord.x == point.x && oldCoord.y == point.y))
					return;
				
				coordToEntities.get(oldCoord).remove(entity);
			}
			
			coordToEntities.computeIfAbsent(point, (c) -> new LinkedHashSet<>()).add(entity);
			entityToCoord.put(entity, point);
			
			LOG.exiting(KnownMap.class.getName(), "updateMap(Entity,Coord)");
		}
	}
	
	/**
	 * @param mapX
	 * @param mapY
	 * @return {@code true} if the given cell is "known", {@code false} if unknown
	 *         or outside the map-bounds
	 */
	public boolean isKnown(int mapX, int mapY) {
		
		if (!isInMap(mapX, mapY))
			return false;
		
		synchronized (this) {
			if (!isInMap(mapX, mapY))
				return false;
			
			return CoordPacker.queryPacked(known, mapX, mapY);
		}
	}
	
	/**
	 * @return a {@link GreasedRegion} representing all known cells in this KnownMap
	 */
	public GreasedRegion getKnownRegion() {
		
		synchronized (this) {
			return CoordPacker.unpackGreasedRegion(known, width, height);
		}
	}
	
	/**
	 * @param ch
	 * @return a {@link GreasedRegion} representing all known cells of the given
	 *         type
	 */
	public GreasedRegion getKnownRegion(char ch) {
		
		synchronized (this) {
			final short[] m = map.get(ch);
			if (m == null)
				return new GreasedRegion(width, height);
			
			return CoordPacker.unpackGreasedRegion(m, width, height);
		}
	}
	
	/**
	 * @return a {@code char[][]} map representing the currently-known state of this
	 *         KnownMap
	 */
	public char[][] getKnownMap() {
		
		synchronized (this) {
			final char[][] map = new char[width][height];
			for (char ch : this.map.keySet())
				CoordPacker.unpackGreasedRegion(this.map.get(ch), width, height).forEach(c -> map[c.x][c.y] = ch);
			
			return map;
		}
	}
	
	/**
	 * @return all {@link Entity Entities} held by this KnownMap
	 */
	public Collection<Entity> getKnownEntities() {
		
		synchronized (this) {
			return entityToCoord.keySet();
		}
	}
	
	/**
	 * Get the map-content at the given cell, or 0 if the cell is either not in the
	 * map or not known.
	 * 
	 * @param mapX
	 * @param mapY
	 * @return
	 */
	public char getMapAt(int mapX, int mapY) {
		
		if (!isInMap(mapX, mapY))
			return 0;
		
		synchronized (this) {
			if (!isInMap(mapX, mapY))
				return 0;
			return getCharAtInternal(mapX, mapY);
		}
	}
	
	private char getCharAtInternal(int mapX, int mapY) {
		
		for (char ch : this.map.keySet())
			if (CoordPacker.queryPacked(this.map.get(ch), mapX, mapY))
				return ch;
			
		return 0;
	}
	
	/**
	 * Get the foreground-color at the given cell, or {@code null} if the cell is
	 * either not in the map or not known.
	 * 
	 * @param mapX
	 * @param mapY
	 * @return
	 */
	@Override
	public Color getColorAt(int mapX, int mapY) {
		
		if (!isInMap(mapX, mapY))
			return null;
		
		synchronized (this) {
			if (!isInMap(mapX, mapY))
				return null;
			
			return getColorAtInternal(mapX, mapY);
		}
	}
	
	private Color getColorAtInternal(int mapX, int mapY) {
		
		for (Color c : this.colors.keySet())
			if (CoordPacker.queryPacked(this.colors.get(c), mapX, mapY))
				return c;
		return null;
	}
	
	/**
	 * Get the background-color at the given cell, or {@code null} if the cell is
	 * either not in the map or not known.
	 * 
	 * @param mapX
	 * @param mapY
	 * @return
	 */
	@Override
	public Color getBGColorAt(int mapX, int mapY) {
		
		if (!isInMap(mapX, mapY))
			return null;
		
		synchronized (this) {
			if (!isInMap(mapX, mapY))
				return null;
			
			return getBGColorAtInternal(mapX, mapY);
		}
	}
	
	private Color getBGColorAtInternal(int mapX, int mapY) {
		
		for (Color c : this.bgColors.keySet())
			if (CoordPacker.queryPacked(this.bgColors.get(c), mapX, mapY))
				return c;
		return null;
	}
	
	/**
	 * Get the {@link Entity Entities} known to be at the given position, if any
	 * 
	 * @param coord
	 * @return an empty Collection if no entity is known to be at the given location
	 */
	public Collection<Entity> getEntitiesAt(Coord point) {
		
		if (!isInMap(point))
			return Collections.emptySet();
		
		synchronized (this) {
			LOG.entering(KnownMap.class.getName(), "getEntitiesAt(Coord)");
			
			if (!isInMap(point))
				return Collections.emptySet();
			
			final Collection<Entity> result = coordToEntities.getOrDefault(point, Collections.emptySet());
			
			LOG.exiting(KnownMap.class.getName(), "getEntitiesAt(Coord)");
			return result;
		}
	}
	
	@Override
	public Collection<Entity> getEntitiesNear(Coord point, int radius) {
		
		synchronized (this) {
			LOG.entering(KnownMap.class.getName(), "getEntitiesNear(Coord,int)");
			
			// LOG.info("Building search-region ...");
			final GreasedRegion region = new GreasedRegion(getWidth(), getHeight()).insert(point).expand8way(radius);
			
			// LOG.info("Searching region ...");
			final Set<Entity> entities = region.stream()
					.filter(c -> coordToEntities.containsKey(c) && coordToEntities.get(c) != null)
					.flatMap(c -> coordToEntities.get(c).stream()).collect(Collectors.toCollection(LinkedHashSet::new));
			
			LOG.exiting(KnownMap.class.getName(), "getEntitiesNear(Coord,int)");
			return entities;
		}
	}
	
	@Override
	public Collection<Entity> getEntitiesNear(int x, int y, int radius) {
		
		return getEntitiesNear(Coord.get(x, y), radius);
	}
	
	/**
	 * Get the last-known location of the given Entity, or {@code null} if this
	 * Entity has no last-known-location.
	 * 
	 * @param entity
	 * @return
	 */
	@Override
	public Coord getEntityLocation(Entity entity) {
		
		synchronized (this) {
			return entityToCoord.get(entity);
		}
	}
	
	/**
	 * Get the "last-updated" timestamp associated with the given map-cell, or 0.0
	 * if no such timestamp has ever been recorded.
	 * 
	 * @param mapX
	 * @param mapY
	 * @return 0 if the given map-coordinates are outside of the map
	 */
	public float getTimestampAt(int mapX, int mapY) {
		
		if (!isInMap(mapX, mapY))
			return 0;
		
		synchronized (this) {
			if (!isInMap(mapX, mapY))
				return 0;
			
			return convertTimestamp(timestamps[mapX][mapY]);
		}
	}
	
	/**
	 * Get the "last-updated" timestamp associated with the given map-cell, or 0.0
	 * if no such timestamp has ever been recorded.
	 * 
	 * @param point
	 * @return
	 */
	public float getTimestampAt(Coord point) {
		
		return getTimestampAt(point.x, point.y);
	}
	
	@Override
	public int getWidth() {
		
		return width;
	}
	
	@Override
	public int getHeight() {
		
		return height;
	}
	
	private static float convertTimestamp(short ts) {
		
		return ((float) ts) / 1000f;
	}
	
	private static short convertTimestamp(float ts) {
		
		return (short) (ts * 1000f);
	}
	
	private static IntSet getUniqueCharacters(char[][] map, GreasedRegion search) {
		
		LOG.entering(KnownMap.class.getName(), "getUniqueCharacters(char[][],GreasedRegion");
		final IntSet chars = new IntSet(8);
		
		final int startX, startY, endX, endY;
		if (search == null) {
			startX = 0;
			startY = 0;
			endX = map.length - 1;
			endY = map[0].length - 1;
		} else {
			startX = search.x(true);
			startY = search.y(true);
			endX = search.x(false);
			endY = search.y(false);
		}
		// LOG.info("searching [" + startX + "," + startY + "]-[" + endX + "," + endY +
		// "]");
		
		for (int x = startX; x <= endX; x++)
			for (int y = startY; y <= endY; y++)
				if (map[x][y] != 0 && search.contains(x, y))
					chars.add((int) map[x][y]);
				
		LOG.exiting(KnownMap.class.getName(), "getUniqueCharacters(char[][],GreasedRegion");
		return chars;
	}
	
	private static Set<Color> getUniqueColors(Color[][] colors, GreasedRegion search) {
		
		LOG.entering(KnownMap.class.getName(), "getUniqueColors(Color[][],GreasedRegion");
		final Set<Color> result = new HashSet<>();
		
		final int startX, startY, endX, endY;
		if (search == null) {
			startX = 0;
			startY = 0;
			endX = colors.length - 1;
			endY = colors[0].length - 1;
		} else {
			startX = search.x(true);
			startY = search.y(true);
			endX = search.x(false);
			endY = search.y(false);
		}
		// LOG.info("searching [" + startX + "," + startY + "]-[" + endX + "," + endY +
		// "]");
		
		for (int x = startX; x <= endX; x++)
			for (int y = startY; y <= endY; y++)
				if (search.contains(x, y))
					result.add(colors[x][y]);
				
		LOG.exiting(KnownMap.class.getName(), "getUniqueColors(Color[][],GreasedRegion");
		return result;
	}
	
	private static GreasedRegion getColorRegion(Color[][] colors, Color c) {
		
		LOG.entering(KnownMap.class.getName(), "getColorRegion(Color[][],Color");
		final GreasedRegion result = new GreasedRegion(colors.length, colors[0].length);
		
		final int startX = 0, startY = 0, endX = colors.length - 1, endY = colors[0].length - 1;
		// LOG.info("searching [" + startX + "," + startY + "]-[" + endX + "," + endY +
		// "]");
		
		for (int x = startX; x <= endX; x++)
			for (int y = startY; y <= endY; y++)
				if (colors[x][y] == c)
					result.add(Coord.get(x, y));
				
		LOG.exiting(KnownMap.class.getName(), "getColorRegion(Color[][],Color");
		return result;
	}
}
