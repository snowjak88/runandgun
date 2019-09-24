/**
 * 
 */
package org.snowjak.runandgun.screen;

import org.snowjak.runandgun.config.DisplayConfiguration;
import org.snowjak.runandgun.context.Context;

import squidpony.squidmath.Coord;

/**
 * Provides a Point-Of-View.
 * <p>
 * Also provides methods for translating between screen- and map-coordinates.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class POV {
	
	private Coord center;
	
	public POV() {
		
		this(0, 0);
	}
	
	public POV(int centerX, int centerY) {
		
		this.center = Coord.get(centerX, centerY);
	}
	
	public void updateCenter(int x, int y) {
		
		this.center = Coord.get(x, y);
	}
	
	public void updateCenter(Coord center) {
		
		this.center = center;
	}
	
	public Coord getCenter() {
		
		return center;
	}
	
	public void reset() {
		
		this.center = Coord.get(0, 0);
	}
	
	public Coord screenToMap(int screenX, int screenY) {
		
		return screenToMap(Coord.get(screenX, screenY));
	}
	
	public Coord screenToMap(Coord screenPoint) {
		
		return Coord.get(screenToMapX(screenPoint.x), screenToMapY(screenPoint.y));
	}
	
	public int screenToMapX(int screenX) {
		
		final DisplayConfiguration dc = Context.get().config().display();
		return screenX + (center.x - dc.getColumns() / 2);
	}
	
	public int screenToMapY(int screenY) {
		
		final DisplayConfiguration dc = Context.get().config().display();
		return screenY + (center.y - dc.getRows() / 2);
	}
	
	public Coord mapToScreen(int mapX, int mapY) {
		
		return mapToScreen(Coord.get(mapX, mapY));
	}
	
	public Coord mapToScreen(Coord mapPoint) {
		
		return Coord.get(mapToScreenX(mapPoint.x), mapToScreenY(mapPoint.y));
	}
	
	public int mapToScreenX(int mapX) {
		
		final DisplayConfiguration dc = Context.get().config().display();
		return mapX - (center.x - dc.getColumns() / 2);
	}
	
	public int mapToScreenY(int mapY) {
		
		final DisplayConfiguration dc = Context.get().config().display();
		return mapY - (center.y - dc.getRows() / 2);
	}
}
