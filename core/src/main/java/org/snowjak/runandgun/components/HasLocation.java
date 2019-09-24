/**
 * 
 */
package org.snowjak.runandgun.components;

import com.badlogic.ashley.core.Component;

/**
 * Indicates that an entity has a physical location on the map.
 * 
 * @author snowjak88
 *
 */
public class HasLocation implements Component {
	
	private float x, y;
	
	public HasLocation() {
		
		this(0, 0);
	}
	
	public HasLocation(float x, float y) {
		
		this.x = x;
		this.y = y;
	}
	
	public float getX() {
		
		return x;
	}
	
	public void setX(float x) {
		
		this.x = x;
	}
	
	public float getY() {
		
		return y;
	}
	
	public void setY(float y) {
		
		this.y = y;
	}
	
}
