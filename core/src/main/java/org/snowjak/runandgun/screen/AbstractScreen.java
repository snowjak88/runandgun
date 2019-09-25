/**
 * 
 */
package org.snowjak.runandgun.screen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.utils.Disposable;

import squidpony.squidgrid.gui.gdx.SColor;

/**
 * Base class for screen-abstraction. Handles common tasks like clearing the
 * draw-buffer before rendering.
 * 
 * @author snowjak88
 *
 */
public abstract class AbstractScreen implements Disposable {
	
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
	public void render() {
		
		Gdx.gl.glClearColor(background.r, background.g, background.b, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
		
		renderScreen();
	}
	
	public abstract void resize(int width, int height);
	
	/**
	 * Render this screen's contents.
	 */
	public abstract void renderScreen();
	
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
	
	public abstract float getWorldX(int gridX);
	
	public abstract float getWorldY(int gridY);
	
	public abstract int getGridX(float worldX);
	
	public abstract int getGridY(float worldY);
}
