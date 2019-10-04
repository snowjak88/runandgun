/**
 * 
 */
package org.snowjak.runandgun.components;

import org.snowjak.runandgun.screen.AbstractScreen;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

import squidpony.squidgrid.gui.gdx.TextCellFactory.Glyph;

/**
 * Indicates that an entity has a {@link Glyph} obtained from the active
 * {@link AbstractScreen screen}.
 * 
 * @author snowjak88
 *
 */
public class HasGlyph implements Component, Poolable {
	
	private Glyph glyph;
	private boolean moveable;
	
	public HasGlyph() {
		
	}
	
	public HasGlyph(Glyph glyph) {
		
		this.glyph = glyph;
	}
	
	public Glyph getGlyph() {
		
		return glyph;
	}
	
	public void setGlyph(Glyph glyph) {
		
		this.glyph = glyph;
	}
	
	public boolean isMoveable() {
		
		return moveable;
	}
	
	public void setMoveable(boolean moveable) {
		
		this.moveable = moveable;
	}
	
	@Override
	public void reset() {
		
		this.glyph = null;
		moveable = false;
	}
}