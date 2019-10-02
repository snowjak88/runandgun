/**
 * 
 */
package org.snowjak.runandgun.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Indicates that an entity has an appearance, and can be drawn on-screen.
 * 
 * @author snowjak88
 *
 */
public class HasAppearance implements Component, Poolable {
	
	private char ch = 0;
	private Color color = Color.CLEAR;
	
	public char getCh() {
		
		return ch;
	}
	
	public void setCh(char ch) {
		
		this.ch = ch;
	}
	
	public Color getColor() {
		
		return color;
	}
	
	public void setColor(Color color) {
		
		this.color = color;
	}
	
	@Override
	public void reset() {
		
		ch = 0;
		color = Color.CLEAR;
	}
}
