/**
 * 
 */
package org.snowjak.runandgun.components;

import org.snowjak.runandgun.screen.AbstractScreen;

import com.badlogic.ashley.core.Component;

import squidpony.squidgrid.gui.gdx.TextCellFactory.Glyph;

/**
 * Indicates that an entity has a {@link Glyph} obtained from the active
 * {@link AbstractScreen screen}.
 * 
 * @author snowjak88
 *
 */
public class HasGlyph implements Component {
	
	private Glyph glyph;
	
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
	
}
