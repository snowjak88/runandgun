/**
 * 
 */
package org.snowjak.runandgun.events;

import squidpony.squidgrid.gui.gdx.TextCellFactory.Glyph;

/**
 * Indicates that some {@link Glyph}, somewhere, has moved.
 * 
 * @author snowjak88
 *
 */
public class GlyphMoveStartEvent {
	
	private final Glyph glyph;
	private final float duration;
	private final int fromX, fromY, toX, toY;
	
	public GlyphMoveStartEvent(Glyph glyph, float duration, int fromX, int fromY, int toX, int toY) {
		
		this.glyph = glyph;
		this.duration = duration;
		this.fromX = fromX;
		this.fromY = fromY;
		this.toX = toX;
		this.toY = toY;
	}
	
	public Glyph getGlyph() {
		
		return glyph;
	}
	
	public float getDuration() {
		
		return duration;
	}
	
	public int getFromX() {
		
		return fromX;
	}
	
	public int getFromY() {
		
		return fromY;
	}
	
	public int getToX() {
		
		return toX;
	}
	
	public int getToY() {
		
		return toY;
	}
	
}
