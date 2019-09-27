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
public class Map {
	
	private final int width, height;
	
	private final char[][] map, bareMap;
	private final float[][] colors, bgColors;
	
	private char[][] lineMap = null;
	private double[][] visibilityResistance;
	
	private final GreasedRegion floors;
	
	private final java.util.Map<Entity, Coord> entityToCoord = new HashMap<>();
	private final java.util.Map<Coord, Collection<Entity>> coordToEntity = new HashMap<>();
	
	/**
	 * Create a new Map, providing both the decorated (i.e., non-minimal)
	 * {@code char[][]} and the "bare" {@code char[][]} (giving only "#" for walls
	 * and "." for floors).
	 * 
	 * @param map
	 * @param bareMap
	 */
	public Map(char[][] map, char[][] bareMap) {
		
		if (map.length != bareMap.length || map[0].length != bareMap[0].length)
			throw new IllegalArgumentException("map and bareMap must be equally-sized.");
		
		this.width = map.length;
		this.height = map[0].length;
		
		this.map = map;
		this.bareMap = bareMap;
		
		this.colors = MapUtility.generateDefaultColorsFloat(map);
		this.bgColors = MapUtility.generateDefaultBGColorsFloat(map);
		
		this.floors = new GreasedRegion(bareMap, '.');
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
	public void setMap(char ch, int x, int y, float color, float bgColor) {
		
		map[x][y] = ch;
		if (ch == '#' || ch == '.')
			bareMap[x][y] = ch;
		
		lineMap = null;
		visibilityResistance = null;
		
		colors[x][y] = color;
		bgColors[x][y] = bgColor;
		
		floors.set((ch == '.'), x, y);
	}
	
	/**
	 * @return the Map's width (in cells)
	 */
	public int getWidth() {
		
		return width;
	}
	
	/**
	 * @return the Map's height (in cells)
	 */
	public int getHeight() {
		
		return height;
	}
	
	/**
	 * @return the "decorated" map, using '#' for walls
	 */
	public char[][] getMap() {
		
		return map;
	}
	
	/**
	 * @return the "bare" map (equivalent to {@link #getMap()}, albeit with only
	 *         floors and walls included)
	 */
	public char[][] getBareMap() {
		
		return bareMap;
	}
	
	/**
	 * @return the "line" map (equivalent to {@link #getMap()}, albeit using
	 *         line-drawing characters for the walls)
	 */
	public char[][] getLineMap() {
		
		if (lineMap == null)
			lineMap = DungeonUtility.hashesToLines(map, true);
		
		return lineMap;
	}
	
	/**
	 * @return the visibility-resistance map
	 */
	public double[][] getVisibilityResistance() {
		
		if (visibilityResistance == null)
			visibilityResistance = DungeonUtility.generateResistances(map);
		
		return visibilityResistance;
	}
	
	/**
	 * @return the foreground-colors map
	 */
	public float[][] getColors() {
		
		return colors;
	}
	
	/**
	 * @return the background-colors map
	 */
	public float[][] getBgColors() {
		
		return bgColors;
	}
	
	/**
	 * @return a set of floor-locations
	 */
	public GreasedRegion getFloors() {
		
		return floors;
	}
	
	/**
	 * Get the {@link Coord coordinates} of the given {@link Entity}, if it is
	 * located in this {@link Map}.
	 * 
	 * @param entity
	 * @return
	 */
	public Coord getCoordFor(Entity entity) {
		
		return entityToCoord.get(entity);
	}
	
	/**
	 * Get the {@link Entity}s located at the given {@link Coord coordinates}, if
	 * any.
	 * 
	 * @param coord
	 * @return
	 */
	public Collection<Entity> getEntitiesAt(Coord coord) {
		
		return coordToEntity.getOrDefault(coord, Collections.emptyList());
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
	public boolean isEntityAt(Entity entity, Coord coord) {
		
		return (entityToCoord.get(entity) == coord);
	}
	
	/**
	 * Sets the given {@link Entity}'s location at the given {@link Coord
	 * coordinates}. If the Entity already occurs somewhere in this Map, its
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
