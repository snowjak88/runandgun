/**
 * 
 */
package org.snowjak.runandgun.map;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;

import squidpony.squidgrid.gui.gdx.MapUtility;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;

/**
 * Encapsulates map data.
 * <p>
 * <strong>Note</strong> on updating cells: If you update a cell's content -- to
 * remove a wall, say -- please do so via
 * {@link #setMap(char, int, int, float, float)}, to ensure that all
 * interrelated data-structures are updated appropriately.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class GlobalMap extends Map {
	
	private final int width, height;
	
	private final char[][] map, bareMap;
	private final Color[][] colors, bgColors;
	
	private double[][] visibilityResistance;
	
	private final GreasedRegion nonObstructing;
	
	private final java.util.Map<Entity, Coord> entityToCoord = new HashMap<>();
	private final java.util.Map<Coord, Collection<Entity>> coordToEntity = new HashMap<>();
	
	/**
	 * Create a new GlobalMap, providing both the decorated (i.e., non-minimal)
	 * {@code char[][]} and the "bare" {@code char[][]} (giving only "#" for walls
	 * and "." for nonObstructing).
	 * 
	 * @param map
	 * @param bareMap
	 */
	public GlobalMap(char[][] map, char[][] bareMap) {
		
		if (map.length != bareMap.length || map[0].length != bareMap[0].length)
			throw new IllegalArgumentException("map and bareMap must be equally-sized.");
		
		this.width = map.length;
		this.height = map[0].length;
		
		this.map = map;
		this.bareMap = bareMap;
		
		this.colors = MapUtility.generateDefaultColors(map);
		this.bgColors = MapUtility.generateDefaultBGColors(map);
		
		this.nonObstructing = new GreasedRegion(bareMap, '#').not();
	}
	
	/**
	 * Update the map-character at the given location.
	 * 
	 * @param ch
	 * @param x
	 * @param y
	 * @param color
	 *            the new foreground color to use
	 * @param bgColor
	 *            the new background color to use
	 */
	public void setMap(char ch, int x, int y, Color color, Color bgColor) {
		
		map[x][y] = ch;
		if (ch == '#' || ch == '.')
			bareMap[x][y] = ch;
		
		visibilityResistance = null;
		
		colors[x][y] = color;
		bgColors[x][y] = bgColor;
		
		nonObstructing.set((ch != '#'), x, y);
	}
	
	/**
	 * @return the GlobalMap's width (in cells)
	 */
	@Override
	public int getWidth() {
		
		return width;
	}
	
	/**
	 * @return the GlobalMap's height (in cells)
	 */
	@Override
	public int getHeight() {
		
		return height;
	}
	
	/**
	 * @return the "decorated" map, using '#' for walls
	 */
	public char[][] getMap() {
		
		return map;
	}
	
	@Override
	public char getMapAt(int x, int y) {
		
		return (isInMap(x, y)) ? map[x][y] : 0;
	}
	
	/**
	 * @return the "bare" map (equivalent to {@link #getMap()}, albeit with only
	 *         nonObstructing and walls included)
	 */
	public char[][] getBareMap() {
		
		return bareMap;
	}
	
	/**
	 * @return the visibility-resistance map
	 */
	public double[][] getVisibilityResistance() {
		
		if (visibilityResistance == null)
			visibilityResistance = DungeonUtility.generateResistances(map);
		
		return visibilityResistance;
	}
	
	@Override
	public Color getColorAt(int x, int y) {
		
		return (isInMap(x, y)) ? colors[x][y] : null;
	}
	
	@Override
	public Color getBGColorAt(int x, int y) {
		
		return (isInMap(x, y)) ? bgColors[x][y] : null;
	}
	
	/**
	 * @return a set of non-wall-locations
	 */
	public GreasedRegion getNonObstructing() {
		
		return nonObstructing;
	}
	
	/**
	 * Get the {@link Coord location} of the given {@link Entity}, if it is located
	 * in this {@link GlobalMap}.
	 * 
	 * @param entity
	 * @return
	 */
	@Override
	public Coord getEntityLocation(Entity entity) {
		
		return entityToCoord.get(entity);
	}
	
	/**
	 * Get the {@link Entity Entities} located at the given {@link Coord
	 * coordinates}, if any.
	 * 
	 * @param coord
	 * @return
	 */
	@Override
	public Collection<Entity> getEntitiesAt(Coord coord) {
		
		return coordToEntity.getOrDefault(coord, Collections.emptyList());
	}
	
	/**
	 * Get those {@link Entity Entities} within {@code radius} cells of the given
	 * {@link Coord point}.
	 * 
	 * @return
	 */
	public Collection<Entity> getEntitiesNear(Coord coord, int radius) {
		
		return getEntitiesIn(new GreasedRegion(width, height).insertCircle(coord, radius));
	}
	
	/**
	 * Get the {@link Entity}s located at any of the given {@link Coord
	 * coordinates}.
	 * 
	 * @param coords
	 * @return
	 */
	public Set<Entity> getEntitiesIn(Collection<Coord> coords) {
		
		return coords.parallelStream().flatMap(c -> getEntitiesAt(c).stream())
				.collect(Collectors.toCollection(HashSet::new));
	}
	
	/**
	 * @param entity
	 * @param coord
	 * @return {@code true} if the given Entity is located at the given Coord
	 */
	@Override
	public boolean isEntityAt(Entity entity, Coord coord) {
		
		return (entityToCoord.get(entity) == coord);
	}
	
	/**
	 * Sets the given {@link Entity}'s location at the given {@link Coord
	 * coordinates}. If the Entity already occurs somewhere in this GlobalMap, its
	 * location is updated.
	 * 
	 * @param entity
	 * @param coord
	 */
	public void setEntity(Entity entity, Coord coord) {
		
		if (entityToCoord.containsKey(entity) && entityToCoord.get(entity) != coord) {
			
			final Coord oldCoord = entityToCoord.get(entity);
			entityToCoord.remove(entity);
			coordToEntity.get(oldCoord).remove(entity);
			
		}
		
		entityToCoord.put(entity, coord);
		coordToEntity.computeIfAbsent(coord, (c) -> new HashSet<>()).add(entity);
	}
}
