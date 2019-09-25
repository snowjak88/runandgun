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
	
	private int x, y;
	private Coord coord = null;
	
	public HasLocation() {
		
		this(0, 0);
	}
	
	public HasLocation(int x, int y) {
		
		this.x = x;
		this.y = y;
	}
	
	public int getX() {
		
		return x;
	}
	
	public void setX(int x) {
		
		coord = null;
		this.x = x;
	}
	
	public int getY() {
		
		return y;
	}
	
	public void setY(int y) {
		
		coord = null;
		this.y = y;
	}
	
	public Coord getCoord() {
		
		if (coord == null)
			coord = Coord.get(x, y);
		
		return coord;
	}
	
}
