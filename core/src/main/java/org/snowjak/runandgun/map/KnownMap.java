/**
 * 
 */
package org.snowjak.runandgun.map;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.snowjak.runandgun.team.Team;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.IntSet;
import com.badlogic.gdx.utils.IntSet.IntSetIterator;

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
	private short[] known = CoordPacker.ALL_WALL, visible = CoordPacker.ALL_WALL;
	
	private final java.util.Map<Character, short[]> map = new HashMap<>();
	private final java.util.Map<Color, short[]> colors = new HashMap<>();
	private final java.util.Map<Color, short[]> bgColors = new HashMap<>();
	
	private final java.util.Map<Coord, Set<Entity>> coordToEntities = new HashMap<>();
	private final java.util.Map<Entity, Coord> entityToCoord = new HashMap<>();
	
	public KnownMap(int width, int height) {
		
		resize(width, height);
	}
	
	public KnownMap(int width, int height, short[] known, java.util.Map<Character, short[]> map,
			java.util.Map<Color, short[]> colors, java.util.Map<Color, short[]> bgColors) {
		
		this.width = width;
		this.height = height;
		this.known = known;
		this.map.putAll(map);
		this.colors.putAll(colors);
		this.bgColors.putAll(bgColors);
	}
	
	public void addKnownEntity(Entity entity, Coord coord) {
		
		synchronized (this) {
			coordToEntities.computeIfAbsent(coord, c -> new LinkedHashSet<>()).add(entity);
			entityToCoord.put(entity, coord);
		}
	}
	
	public KnownMap copy() {
		
		final KnownMap result = new KnownMap(getWidth(), getHeight());
		result.insertMap(this, this.known);
		return result;
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
			this.visible = CoordPacker.ALL_WALL;
			
			this.map.clear();
			this.colors.clear();
			this.bgColors.clear();
			this.entityToCoord.clear();
			this.coordToEntities.clear();
		}
	}
	
	/**
	 * Set this KnownMap's contents to copy the given {@link Team}'s KnownMap,
	 * overriding whatever was here before.
	 * 
	 * @param map
	 * @param timestamp
	 */
	public void setMap(Team team, short[] onlyThese) {
		
		synchronized (this) {
			final KnownMap teamMap = team.getMap();
			resize(teamMap.getWidth(), teamMap.getHeight());
			
			final short[] update;
			if (onlyThese == null)
				update = CoordPacker.ALL_ON;
			else
				update = onlyThese;
			
			this.known = CoordPacker.intersectPacked(teamMap.known, update);
			this.visible = CoordPacker.intersectPacked(teamMap.visible, update);
			
			this.map.clear();
			for (Character c : teamMap.map.keySet())
				this.map.put(c, CoordPacker.intersectPacked(teamMap.map.get(c), update));
			
			this.colors.clear();
			for (Color c : teamMap.colors.keySet())
				this.colors.put(c, CoordPacker.intersectPacked(teamMap.colors.get(c), update));
			
			this.bgColors.clear();
			for (Color c : teamMap.bgColors.keySet())
				this.bgColors.put(c, CoordPacker.intersectPacked(teamMap.bgColors.get(c), update));
			
			this.coordToEntities.clear();
			this.entityToCoord.clear();
			
			teamMap.coordToEntities.entrySet().stream()
					.filter(e -> CoordPacker.queryPacked(update, e.getKey().x, e.getKey().y))
					.forEach(e -> this.coordToEntities.put(e.getKey(), e.getValue()));
			teamMap.entityToCoord.entrySet().stream()
					.filter(e -> CoordPacker.queryPacked(update, e.getValue().x, e.getValue().y))
					.forEach(e -> this.entityToCoord.put(e.getKey(), e.getValue()));
		}
	}
	
	/**
	 * Inserts a portion of the given {@link GlobalMap} into this KnownMap.
	 * 
	 * @param map
	 * @param updateWithin
	 *            the region to update, or {@code null} to insert everything
	 * @param visible
	 *            the region to be regarded as "currently visible", or {@code null}
	 *            to assume that everything is currently visible
	 */
	public void insertMap(GlobalMap map, GreasedRegion updateWithin, GreasedRegion visible) {
		
		insertMap(map, (updateWithin != null) ? CoordPacker.packSeveral(updateWithin) : null,
				(visible != null) ? CoordPacker.packSeveral(visible) : null);
	}
	
	/**
	 * Inserts a portion of the given {@link GlobalMap} into this KnownMap.
	 * 
	 * @param map
	 * @param updateWithin
	 *            the region to update, or {@code null} to insert everything
	 * @param visible
	 *            the region to be regarded as "currently visible", or {@code null}
	 *            to assume that everything is currently visible
	 */
	public void insertMap(GlobalMap map, short[] updateWithin, short[] visible) {
		
		synchronized (this) {
			resize(map.getWidth(), map.getHeight());
			
			final short[] update;
			if (updateWithin == null)
				update = CoordPacker.ALL_ON;
			else
				update = updateWithin;
			
			this.known = CoordPacker.unionPacked(this.known, update);
			this.visible = CoordPacker.unionPacked(this.visible, (visible == null) ? CoordPacker.ALL_ON : visible);
			
			final GreasedRegion updateRegion = CoordPacker.unpackGreasedRegion(update, getWidth(), getHeight());
			
			final IntSetIterator uniqueChars = getUniqueCharacters(map.getMap(), updateRegion).iterator();
			while (uniqueChars.hasNext) {
				final char c = (char) uniqueChars.next();
				final short[] existing = this.map.getOrDefault(c, CoordPacker.ALL_WALL);
				final short[] updated = CoordPacker.pack(map.getMap(), c);
				this.map.put(c, CoordPacker.unionPacked(existing, updated));
			}
			
			for (Color c : getUniqueColors(map.getColors(), updateRegion)) {
				final short[] existing = this.colors.getOrDefault(c, CoordPacker.ALL_WALL);
				final short[] updated = CoordPacker.packSeveral(getColorRegion(map.getColors(), c));
				this.colors.put(c, CoordPacker.unionPacked(existing, updated));
			}
			
			for (Color c : getUniqueColors(map.getBGColors(), updateRegion)) {
				final short[] existing = this.bgColors.getOrDefault(c, CoordPacker.ALL_WALL);
				final short[] updated = CoordPacker.packSeveral(getColorRegion(map.getBGColors(), c));
				this.bgColors.put(c, CoordPacker.unionPacked(existing, updated));
			}
		}
	}
	
	/**
	 * Update the entity-knowledge held within this KnownMap, but only within the
	 * given region.
	 * <p>
	 * If you want to retrieve lists of entities added, moved, and removed from this
	 * KnownMap by this update, use
	 * {@link #updateEntities(GlobalMap, short[], Collection, Collection, Collection)}
	 * </p>
	 * 
	 * @param map
	 * @param updateWithin
	 * @param noRemove
	 *            if {@link Entity Entities} should not be considered for removal
	 *            from this map, only added and moved
	 */
	public void updateEntities(GlobalMap map, short[] updateWithin, boolean noRemove) {
		
		updateEntities(map, updateWithin, noRemove, null, null, null);
	}
	
	/**
	 * Update the entity-knowledge held within this KnownMap, but only within the
	 * given region.
	 * 
	 * @param map
	 * @param updateWithin
	 * @param noRemove
	 *            if {@link Entity Entities} should not be considered for removal
	 *            from this map, only added and moved
	 * @param entitiesAdded
	 *            used to return the set of {@link Entity Entities} that were added
	 *            to this KnownMap within {@updateWithin}, or {@code null} if no
	 *            return needed
	 * @param entitiesMoved
	 *            used to return the set of {@link Entity Entities} that were moved
	 *            on this KnownMap within {@updateWithin}, or {@code null} if no
	 *            return needed
	 * @param entitiesRemoved
	 *            used to return the set of {@link Entity Entities} that were
	 *            removed from this KnownMap within {@updateWithin}, or {@code null}
	 *            if no return needed
	 */
	public void updateEntities(GlobalMap map, short[] updateWithin, boolean noRemove, Collection<Entity> entitiesAdded,
			Collection<Entity> entitiesMoved, Collection<Entity> entitiesRemoved) {
		
		synchronized (this) {
			
			final short[] update;
			if (updateWithin == null)
				update = CoordPacker.ALL_ON;
			else
				update = updateWithin;
				
			//
			// Update entities in the "to-update" region
			//
			Iterator<Entity> entityIterator = null;
			
			for (Coord c : CoordPacker.unpackGreasedRegion(update, getWidth(), getHeight())) {
				
				entityIterator = this.coordToEntities.getOrDefault(c, Collections.emptySet()).iterator();
				while (entityIterator.hasNext()) {
					final Entity e = entityIterator.next();
					final Coord globalLocation = map.getEntityLocation(e);
					
					if (!noRemove && globalLocation == null) {
						
						if (entitiesRemoved != null)
							entitiesRemoved.add(e);
						
						entityIterator.remove();
						entityToCoord.remove(e);
						
					} else if (globalLocation.x != c.x || globalLocation.y != c.y) {
						
						if (entitiesMoved != null)
							entitiesMoved.add(e);
						
						entityIterator.remove();
						coordToEntities.computeIfAbsent(globalLocation, (x) -> new LinkedHashSet<>()).add(e);
						entityToCoord.put(e, globalLocation);
						
					}
				}
				
				for (Entity e : map.getEntitiesAt(c)) {
					final Coord thisLocation = entityToCoord.get(e);
					
					if (thisLocation == null) {
						
						if (entitiesAdded != null)
							entitiesAdded.add(e);
						
						coordToEntities.computeIfAbsent(c, (x) -> new LinkedHashSet<>()).add(e);
						entityToCoord.put(e, c);
						
					}
				}
			}
		}
	}
	
	/**
	 * Insert the given KnownMap's contents into this KnownMap, but only if the
	 * other map's {@link #getLastSynchronizedTimestamp() timestamp} is
	 * equal-or-newer than this map's timestamp.
	 * 
	 * @param map
	 */
	public void insertMap(KnownMap map) {
		
		insertMap(map, CoordPacker.ALL_ON);
	}
	
	/**
	 * Insert the given KnownMap's contents into this KnownMap, but only if the
	 * other map's {@link #getLastSynchronizedTimestamp() timestamp} is
	 * equal-or-newer than this map's timestamp.
	 * 
	 * @param map
	 * @param updateWithin
	 *            a {@link GreasedRegion region} to update, or {@code null} to
	 *            update everything
	 */
	public void insertMap(KnownMap map, GreasedRegion updateWithin) {
		
		insertMap(map, (updateWithin == null) ? CoordPacker.ALL_ON : CoordPacker.packSeveral(updateWithin));
	}
	
	/**
	 * Insert the given KnownMap's contents into this KnownMap.
	 * 
	 * @param map
	 * @param updateWithin
	 *            a {@link CoordPacker#packSeveral(Collection) packed} region to
	 *            update, or {@code null} to update everything
	 */
	public void insertMap(KnownMap map, short[] updateWithin) {
		
		synchronized (this) {
			
			final short[] update;
			if (updateWithin == null)
				update = CoordPacker.ALL_ON;
			else
				update = updateWithin;
			
			this.known = CoordPacker.unionPacked(this.known, CoordPacker.intersectPacked(map.known, update));
			this.visible = CoordPacker.unionPacked(this.visible,
					CoordPacker.intersectPacked(map.visible, updateWithin));
			
			for (Character c : map.map.keySet()) {
				final short[] existingRegion = this.map.getOrDefault(c, CoordPacker.ALL_WALL);
				final short[] updatedRegion = CoordPacker.intersectPacked(map.map.getOrDefault(c, CoordPacker.ALL_WALL),
						update);
				this.map.put(c, CoordPacker.unionPacked(existingRegion, updatedRegion));
			}
			
			for (Color c : map.colors.keySet()) {
				final short[] existingRegion = this.colors.getOrDefault(c, CoordPacker.ALL_WALL);
				final short[] updatedRegion = CoordPacker
						.intersectPacked(map.colors.getOrDefault(c, CoordPacker.ALL_WALL), update);
				this.colors.put(c, CoordPacker.unionPacked(existingRegion, updatedRegion));
			}
			
			for (Color c : map.bgColors.keySet()) {
				final short[] existingRegion = this.bgColors.getOrDefault(c, CoordPacker.ALL_WALL);
				final short[] updatedRegion = CoordPacker
						.intersectPacked(map.bgColors.getOrDefault(c, CoordPacker.ALL_WALL), update);
				this.bgColors.put(c, CoordPacker.unionPacked(existingRegion, updatedRegion));
			}
			
			//
			// Update entities in the "to-update" region
			//
			for (Coord c : CoordPacker.unpackGreasedRegion(update, getWidth(), getHeight()))
				
				//
				// If there are any entities in the other map at the given coord
				if (map.coordToEntities.containsKey(c))
					//
					// Process each entity at the coord
					for (Entity e : map.coordToEntities.getOrDefault(c, Collections.emptySet())) {
						
						//
						// If this map already has that entity, and needs to be updated
						if (this.entityToCoord.containsKey(e) && this.entityToCoord.get(e) != c) {
							final Coord prevCoord = this.entityToCoord.get(e);
							this.coordToEntities.get(prevCoord).remove(e);
						}
						
						//
						// Store the entity at the new coord
						this.entityToCoord.put(e, c);
						this.coordToEntities.computeIfAbsent(c, (x) -> new LinkedHashSet<>()).add(e);
					}
		}
	}
	
	/**
	 * @param map
	 * @return {@code true} if the given cell is "known", {@code false} if unknown
	 *         or outside the map-bounds
	 */
	public boolean isKnown(Coord map) {
		
		return isKnown(map.x, map.y);
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
	 * @return the {@link CoordPacker#packSeveral(Collection) packed} version of
	 *         {@link #getKnownRegion()}
	 */
	public short[] getKnown() {
		
		synchronized (this) {
			return known;
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
		
		return CoordPacker.unpackGreasedRegion(getKnown(ch), width, height);
	}
	
	/**
	 * The {@link CoordPacker#packSeveral(Collection) packed} version of
	 * {@link #getKnownRegion(char)}.
	 * 
	 * @param ch
	 * @return
	 */
	public short[] getKnown(char ch) {
		
		synchronized (this) {
			return map.getOrDefault(ch, CoordPacker.ALL_WALL);
		}
	}
	
	/**
	 * The packed locations of all instances of the given {@link Color} on the map.
	 * 
	 * @param color
	 * @param isBackground
	 *            {@code true} to query the background-color map, {@code false} to
	 *            query the foreground-color map
	 * @return
	 */
	public short[] getKnown(Color color, boolean isBackground) {
		
		synchronized (this) {
			return (isBackground ? colors : bgColors).getOrDefault(color, CoordPacker.ALL_WALL);
		}
	}
	
	/**
	 * @return the set of all {@link Character chars} known to this map
	 */
	public Set<Character> getUniqueKnownChars() {
		
		return map.keySet();
	}
	
	public Set<Color> getUniqueKnownColors() {
		
		return colors.keySet();
	}
	
	public Set<Color> getUniqueKnownBGColors() {
		
		return bgColors.keySet();
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
	 * Return {@code true} if the given location is flagged as "visible".
	 * 
	 * @param map
	 * @return
	 */
	public boolean isVisible(Coord map) {
		
		return isVisible(map.x, map.y);
	}
	
	/**
	 * Return {@code true} if the given location is flagged as "visible".
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	public boolean isVisible(int x, int y) {
		
		synchronized (this) {
			return CoordPacker.queryPacked(visible, x, y);
		}
	}
	
	/**
	 * @return the {@link CoordPacker#packSeveral(Collection) packed} version of
	 *         {@link #getVisibleRegion()}.
	 */
	public short[] getVisible() {
		
		synchronized (this) {
			return visible;
		}
	}
	
	/**
	 * @return a {@link GreasedRegion region} describing those parts of this map
	 *         tagged as "visible"
	 */
	public GreasedRegion getVisibleRegion() {
		
		synchronized (this) {
			return CoordPacker.unpackGreasedRegion(visible, getWidth(), getHeight());
		}
	}
	
	/**
	 * Remove all "is-visible" regions from this map.
	 */
	public void resetVisibleRegion() {
		
		synchronized (this) {
			visible = CoordPacker.ALL_WALL;
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
	 * Remove an Entity from this KnownMap.
	 * 
	 * @param entity
	 */
	public void forgetEntity(Entity entity) {
		
		synchronized (this) {
			final Coord c = entityToCoord.get(entity);
			if (c == null)
				return;
			
			if (coordToEntities.containsKey(c))
				coordToEntities.get(c).remove(entity);
			entityToCoord.remove(entity);
		}
	}
	
	@Override
	public int getWidth() {
		
		return width;
	}
	
	@Override
	public int getHeight() {
		
		return height;
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
