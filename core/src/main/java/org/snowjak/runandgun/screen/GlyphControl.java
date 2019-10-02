/**
 * 
 */
package org.snowjak.runandgun.screen;

import java.util.concurrent.Future;

import com.badlogic.gdx.graphics.Color;
import com.google.common.util.concurrent.ListenableFuture;

import squidpony.squidgrid.gui.gdx.TextCellFactory.Glyph;

/**
 * Provides an interface to allocate and control {@link Glyph}s.
 * 
 * @author snowjak88
 *
 */
public interface GlyphControl {
	
	/**
	 * Create a new {@link Glyph} at the given location.
	 * <p>
	 * Because creation will take place on the rendering-thread, this returns a
	 * {@link Future}. Your calling code may opt to wait, potentially blocking until
	 * the next frame, or else plan to store and use this Future sometime on the
	 * next frame.
	 * </p>
	 * 
	 * @param ch
	 * @param color
	 * @param x
	 * @param y
	 * @return
	 */
	public ListenableFuture<Glyph> create(char ch, Color color, int x, int y);
	
	/**
	 * Remove the given {@link Glyph} from the current display's list of active
	 * Glyphs.
	 * 
	 * @param glyph
	 */
	public void remove(Glyph glyph);
	
	/**
	 * Order the given {@link Glyph} to be moved from {@code [fromX,fromY]} to
	 * {@code [toX,toY]}, with the total movement lasting {@code duration} seconds.
	 * 
	 * @param glyph
	 * @param fromX
	 * @param fromY
	 * @param toX
	 * @param toY
	 * @param duration
	 */
	public void move(Glyph glyph, int fromX, int fromY, int toX, int toY, float duration);
	
	/**
	 * Reset the position of the given {@link Glyph}.
	 * 
	 * @param glyph
	 * @param x
	 * @param y
	 */
	public void setPosition(Glyph glyph, int x, int y);
	
	/**
	 * Stop all animations on the current Glyph.
	 * 
	 * @param glyph
	 */
	public void stop(Glyph glyph);
}
