/**
 * 
 */
package org.snowjak.runandgun.screen;

import org.snowjak.runandgun.config.DisplayConfiguration;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.events.CurrentMapChangedEvent;
import org.snowjak.runandgun.map.Map;

import com.badlogic.gdx.utils.Disposable;
import com.google.common.eventbus.Subscribe;

import squidpony.squidgrid.gui.gdx.TextCellFactory.Glyph;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;

/**
 * Provides a Point-Of-View. A POV can be centered either on a specific
 * grid-location, or on a {@link Glyph} (usually associated with an entity).
 * <p>
 * Also provides methods for translating between screen- and map-coordinates.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class POV implements Disposable {
	
	private Coord center;
	private Glyph glyph;
	
	private double[][] lightLevels;
	private GreasedRegion currentlySeen, haveSeen;
	
	public POV() {
		
		this(0, 0);
	}
	
	public POV(Glyph glyph) {
		
		updateFocus(glyph);
		resize();
		
		Context.get().eventBus().register(this);
	}
	
	public POV(int centerX, int centerY) {
		
		updateFocus(centerX, centerY);
		resize();
		
		Context.get().eventBus().register(this);
	}
	
	public void updateFocus(int x, int y) {
		
		this.center = Coord.get(x, y);
		this.glyph = null;
	}
	
	public void updateFocus(Coord center) {
		
		this.center = center;
		this.glyph = null;
	}
	
	public void updateFocus(Glyph glyph) {
		
		final AbstractScreen s = Context.get().screen();
		if (s != null) {
			this.center = Coord.get(s.getGridX(glyph.getX()), s.getGridY(glyph.getY()));
		} else
			this.center = null;
		this.glyph = glyph;
	}
	
	/**
	 * Is this POV focused on a {@link Glyph}, or on a map-grid location?
	 * 
	 * @return
	 */
	public boolean isFocusGlyph() {
		
		return (glyph != null);
	}
	
	public Glyph getGlyph() {
		
		return glyph;
	}
	
	public Coord getCenter() {
		
		return center;
	}
	
	public void reset() {
		
		updateFocus(0, 0);
		lightLevels = null;
		currentlySeen.fill(false);
		haveSeen.fill(false);
	}
	
	public void resize() {
		
		final Map map = Context.get().map();
		if (map != null) {
			currentlySeen = new GreasedRegion(map.getWidth(), map.getHeight());
			haveSeen = new GreasedRegion(map.getWidth(), map.getHeight());
		} else {
			currentlySeen = new GreasedRegion();
			haveSeen = new GreasedRegion();
		}
	}
	
	/**
	 * Update this POV's "seen" fields (both {@link #getCurrentlySeen() currently}
	 * and {@link #getHaveSeen() historically} "seen") with the given light-levels.
	 * 
	 * @param lightLevels
	 */
	public void updateLightLevels(double[][] lightLevels) {
		
		this.lightLevels = lightLevels;
		currentlySeen.refill(lightLevels, 0.0).not();
		haveSeen.or(currentlySeen);
	}
	
	public double[][] getLightLevels() {
		
		return lightLevels;
	}
	
	public GreasedRegion getCurrentlySeen() {
		
		return currentlySeen;
	}
	
	public GreasedRegion getHaveSeen() {
		
		return haveSeen;
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
	
	@Subscribe
	public void receiveNewCurrentMapEvent(CurrentMapChangedEvent event) {
		
		resize();
	}
	
	@Override
	public void dispose() {
		
		Context.get().eventBus().unregister(this);
	}
}
