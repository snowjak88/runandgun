/**
 * 
 */
package org.snowjak.runandgun.screen;

import com.badlogic.gdx.graphics.Color;

import squidpony.squidmath.Coord;

/**
 * Provides methods for drawing decorations on an {@link AbstractScreen screen}.
 * 
 * @author snowjak88
 *
 */
public interface DecorationProvider {
	
	/**
	 * Draw a line between the given cells (expressed in screen-coordinates).
	 * 
	 * @param from
	 * @param to
	 * @param color
	 */
	public default void line(Coord from, Coord to, Color color) {
		
		line(from.x, from.y, to.x, to.y, color);
	}
	
	/**
	 * Draw a line between the given cells (expressed in screen-coordinates).
	 * 
	 * @param fromX
	 * @param fromY
	 * @param toX
	 * @param toY
	 * @param color
	 */
	public void line(int fromX, int fromY, int toX, int toY, Color color);
	
	/**
	 * Draw a hollow circle, centered at the given cell (expressed in
	 * screen-coordinates).
	 * 
	 * @param center
	 * @param radius
	 * @param color
	 */
	public default void circle(Coord center, int radius, Color color) {
		
		circle(center.x, center.y, radius, color);
	}
	
	/**
	 * Draw a hollow circle, centered at the given cell (expressed in
	 * screen-coordinates).
	 * 
	 * @param x
	 * @param y
	 * @param radius
	 * @param color
	 */
	public void circle(int x, int y, int radius, Color color);
	
	/**
	 * Fill a single cell (expressed in screen-coordinates) with the given color.
	 * 
	 * @param point
	 * @param color
	 */
	public default void point(Coord point, Color color) {
		
		point(point.x, point.y, color);
	}
	
	/**
	 * Fill a single cell (expressed in screen-coordinates) with the given color.
	 * 
	 * @param x
	 * @param y
	 * @param color
	 */
	public void point(int x, int y, Color color);
	
	/**
	 * Draw a hollow rectangle between the given points (expressed in
	 * screen-coordinates).
	 * 
	 * @param from
	 * @param to
	 * @param color
	 */
	public default void rectangle(Coord from, Coord to, Color color) {
		
		rectangle(from.x, from.y, to.x, to.y, color);
	}
	
	/**
	 * Draw a hollow rectangle between the given points (expressed in
	 * screen-coordinates).
	 * 
	 * @param fromX
	 * @param fromY
	 * @param toX
	 * @param toY
	 * @param color
	 */
	public void rectangle(int fromX, int fromY, int toX, int toY, Color color);
	
	/**
	 * Convert the given screen- to map-coordinates.
	 * 
	 * @param screen
	 * @return
	 */
	public default Coord map(Coord screen) {
		
		return Coord.get(mapX(screen.x), mapY(screen.y));
	}
	
	/**
	 * Convert the given screen- to map-coordinates.
	 * 
	 * @param screenX
	 * @return
	 */
	public int mapX(int screenX);
	
	/**
	 * Convert the given screen- to map-coordinates.
	 * 
	 * @param screenY
	 * @return
	 */
	public int mapY(int screenY);
	
	/**
	 * Convert the given map- to screen-coordinates.
	 * 
	 * @param map
	 * @return
	 */
	public default Coord screen(Coord map) {
		
		return Coord.get(screenX(map.x), screenY(map.y));
	}
	
	/**
	 * Convert the given map- to screen-coordinates.
	 * 
	 * @param mapX
	 * @return
	 */
	public int screenX(int mapX);
	
	/**
	 * Convert the given map- to screen-coordinates.
	 * 
	 * @param mapY
	 * @return
	 */
	public int screenY(int mapY);
	
	/**
	 * Get the current mouse-cursor position, in cells (expressed in
	 * screen-coordinates).
	 * 
	 * @return
	 */
	public Coord cursor();
}
