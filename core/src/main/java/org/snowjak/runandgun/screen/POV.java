/**
 * 
 */
package org.snowjak.runandgun.screen;

import org.snowjak.runandgun.components.HasGlyph;
import org.snowjak.runandgun.components.HasLocation;
import org.snowjak.runandgun.config.DisplayConfiguration;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.map.GlobalMap;
import org.snowjak.runandgun.systems.UniqueTagManager;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Disposable;

import squidpony.squidgrid.Direction;
import squidpony.squidgrid.gui.gdx.TextCellFactory;
import squidpony.squidgrid.gui.gdx.TextCellFactory.Glyph;
import squidpony.squidmath.Coord;

/**
 * Provides a Point-Of-View. A POV can be set up in one of several ways:
 * <ul>
 * <li>explicitly, centered on a grid-cell</li>
 * <li>explicitly, centered on a specific {@link TextCellFactory.Glyph}</li>
 * <li>implicitly, centered on a specific {@link Entity}</li>
 * </ul>
 * <strong>Implicit Centering</strong>
 * <p>
 * This is assumed to be the more powerful form. With {@link #update(float)
 * every frame}, this POV will check the {@link UniqueTagManager} for the tag
 * {@link #POV_ENTITY_TAG}. If no such entity exists, then this POV's center is
 * left alone. <em>However</em>, if an entity is tagged, then this POV is
 * updated to the first available coordinate:
 * <ol>
 * <li>the entity's {@link HasGlyph glyph} location</li>
 * <li>the entity's {@link HasLocation map-grid} location</li>
 * </ol>
 * </p>
 * <strong>Explicit Centering</strong>
 * <p>
 * By calling either {@link #updateFocus(Coord)},
 * {@link #updateFocus(float, float)}, or {@link #updateFocus(Glyph)}, you can
 * instruct this POV to center on the specified grid-square or Glyph. It is
 * important to note that any explicitly-set center will be overridden by an
 * implicit center.
 * </p>
 * <p>
 * Also provides methods for translating between screen- and map-coordinates.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class POV implements Disposable {
	
	public static final String POV_ENTITY_TAG = POV.class.getName();
	
	private static final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	private static final ComponentMapper<HasGlyph> HAS_GLYPH = ComponentMapper.getFor(HasGlyph.class);
	
	private float centerX, centerY;
	private Glyph glyph;
	
	private Direction shift = null;
	
	public POV() {
		
		this(0, 0);
	}
	
	public POV(Glyph glyph) {
		
		updateFocus(glyph);
	}
	
	public POV(float centerX, float centerY) {
		
		updateFocus(centerX, centerY);
	}
	
	/**
	 * You should call this with every frame, to ensure that the POV is updated
	 * appropriately.
	 * 
	 * @param deltaTime
	 */
	public void update(float deltaTime) {
		
		final UniqueTagManager tagManager = Context.get().engine().getSystem(UniqueTagManager.class);
		final Entity povEntity = tagManager.get(POV_ENTITY_TAG);
		if (povEntity != null) {
			
			if (HAS_GLYPH.has(povEntity))
				updateFocus(HAS_GLYPH.get(povEntity).getGlyph());
			else if (HAS_LOCATION.has(povEntity))
				updateFocus(HAS_LOCATION.get(povEntity).get());
			
		}
		
		if (isFocusGlyph()) {
			final AbstractScreen s = Context.get().screen();
			if (s != null) {
				centerX = s.getGridX(glyph.getX());
				centerY = s.getGridY(glyph.getY());
			}
		}
		
		if (shift != null) {
			
			final int dx = shift.deltaX, dy = shift.deltaY;
			
			final float shiftSpeed = Context.get().config().input().mouse().getScrollSpeed();
			final GlobalMap m = Context.get().globalMap();
			
			if (dx != 0) {
				final float shiftX = (float) dx * shiftSpeed * deltaTime;
				centerX = Math.max(Math.min(centerX + shiftX, m.getWidth()), 0);
			}
			if (dy != 0) {
				final float shiftY = (float) dy * shiftSpeed * deltaTime;
				centerY = Math.max(Math.min(centerY + shiftY, m.getHeight()), 0);
			}
		}
	}
	
	public void updateFocus(float x, float y) {
		
		this.centerX = x;
		this.centerY = y;
		this.glyph = null;
	}
	
	public void updateFocus(Coord center) {
		
		this.centerX = center.x;
		this.centerY = center.y;
		
		this.glyph = null;
	}
	
	public void updateFocus(Glyph glyph) {
		
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
		
		return Coord.get((int) getCenterX(), (int) getCenterY());
	}
	
	public float getCenterX() {
		
		return centerX;
	}
	
	public float getCenterY() {
		
		return centerY;
	}
	
	/**
	 * Shift this POV in the given {@link Direction direction}. If this POV is
	 * currently attached to a {@link Glyph}, this method "detaches" it from that
	 * Glyph and begins drifting from the Glyph's last-known position.
	 * <p>
	 * Note that if this POV is implicitly centered (via an entity tagged with
	 * {@link #POV_ENTITY_TAG}), this method will <em>only</em> shift this POV by 1
	 * frame's worth, until the entity is no longer tagged.
	 * </p>
	 * 
	 * @param direction
	 */
	public void shift(Direction direction) {
		
		if (direction != null && isFocusGlyph()) {
			final AbstractScreen s = Context.get().screen();
			if (s != null) {
				updateFocus(s.getGridX(glyph.getX()), s.getGridY(glyph.getY()));
			}
		}
		
		shift = direction;
	}
	
	public void reset() {
		
		updateFocus(0, 0);
	}
	
	public Coord screenToMap(int screenX, int screenY) {
		
		return screenToMap(Coord.get(screenX, screenY));
	}
	
	public Coord screenToMap(Coord screenPoint) {
		
		return Coord.get(screenToMapX(screenPoint.x), screenToMapY(screenPoint.y));
	}
	
	public int screenToMapX(int screenX) {
		
		final DisplayConfiguration dc = Context.get().config().display();
		return screenX + ((int) getCenterX() - dc.getColumns() / 2);
	}
	
	public int screenToMapY(int screenY) {
		
		final DisplayConfiguration dc = Context.get().config().display();
		return screenY + ((int) getCenterY() - dc.getRows() / 2);
	}
	
	public Coord mapToScreen(int mapX, int mapY) {
		
		return mapToScreen(Coord.get(mapX, mapY));
	}
	
	public Coord mapToScreen(Coord mapPoint) {
		
		return Coord.get(mapToScreenX(mapPoint.x), mapToScreenY(mapPoint.y));
	}
	
	public int mapToScreenX(int mapX) {
		
		final DisplayConfiguration dc = Context.get().config().display();
		return mapX - ((int) getCenterX() - dc.getColumns() / 2);
	}
	
	public int mapToScreenY(int mapY) {
		
		final DisplayConfiguration dc = Context.get().config().display();
		return mapY - ((int) getCenterY() - dc.getRows() / 2);
	}
	
	@Override
	public void dispose() {
		
	}
}
