/**
 * 
 */
package org.snowjak.runandgun.components;

import com.badlogic.ashley.core.Component;

import squidpony.squidmath.Coord;

/**
 * Indicates that an entity has a physical location on the map.
 * 
 * @author snowjak88
 *
 */
public class HasLocation implements Component {
	
	private float x, y;
	private Coord coord = null;
	
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
		
		coord = null;
		this.x = x;
	}
	
	public float getY() {
		
		return y;
	}
	
	public void setY(float y) {
		
		coord = null;
		this.y = y;
	}
	
	public Coord getCoord() {
		
		if (coord == null)
			coord = Coord.get((int) x, (int) y);
		
		return coord;
	}
	
}
