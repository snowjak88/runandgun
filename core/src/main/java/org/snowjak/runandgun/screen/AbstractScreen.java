/**
 * 
 */
package org.snowjak.runandgun.screen;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.utils.Disposable;

import squidpony.squidgrid.gui.gdx.SColor;

/**
 * Base class for screen-abstraction. Handles common tasks like clearing the
 * draw-buffer before rendering.
 * 
 * @author snowjak88
 *
 */
public abstract class AbstractScreen implements Disposable, GlyphControl, DecorationProvider {
	
	private Set<Decoration> decorations = Collections.synchronizedSet(new LinkedHashSet<>());
	
	private Color background = SColor.BLACK;
	
	public AbstractScreen() {
		
		this(SColor.BLACK);
	}
	
	public AbstractScreen(Color background) {
		
		this.background = background;
	}
	
	public abstract void create();
	
	/**
	 * Render this screen.
	 */
	public void render(float delta) {
		
		Gdx.gl.glClearColor(background.r, background.g, background.b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		renderScreen(delta);
	}
	
	/**
	 * Your rendering thread should call this <em>after</em> drawing on-screen
	 * elements but before, e.g., committing them to your active {@link Batch}.
	 */
	public void drawDecorations(float delta) {
		
		synchronized (decorations) {
			
			final Iterator<Decoration> decorationIterator = decorations.iterator();
			while (decorationIterator.hasNext()) {
				final Decoration d = decorationIterator.next();
				
				d.updateDuration(delta);
				if (d.isExpired())
					decorationIterator.remove();
				else
					d.decorate(this);
			}
		}
	}
	
	public abstract void resize(int width, int height);
	
	/**
	 * Render this screen's contents.
	 */
	public abstract void renderScreen(float delta);
	
	/**
	 * @return the assigned background color
	 */
	public Color getBackground() {
		
		return background;
	}
	
	/**
	 * Assign a new background color for this screen.
	 * 
	 * @param background
	 */
	public void setBackground(Color background) {
		
		this.background = background;
	}
	
	/**
	 * Add a {@link Decoration} to this {@link AbstractScreen screen}.
	 * 
	 * @param decoration
	 */
	public void addDecoration(Decoration decoration) {
		
		synchronized (decorations) {
			this.decorations.add(decoration);
		}
	}
	
	/**
	 * Remove the given {@link Decoration} from this {@link AbstractScreen screen}.
	 * 
	 * @param decoration
	 */
	public void removeDecoration(Decoration decoration) {
		
		synchronized (decorations) {
			this.decorations.remove(decoration);
		}
	}
	
	public abstract int getWidth();
	
	public abstract int getHeight();
	
	public abstract float getWorldX(float gridX);
	
	public abstract float getWorldY(float gridY);
	
	public abstract float getGridX(float worldX);
	
	public abstract float getGridY(float worldY);
}
