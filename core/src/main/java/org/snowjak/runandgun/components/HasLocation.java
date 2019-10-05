/**
 * 
 */
package org.snowjak.runandgun.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

import squidpony.squidmath.Coord;

/**
 * Indicates that an entity has a physical location on the map.
 * 
 * @author snowjak88
 *
 */
public class HasLocation implements Component, Poolable {
	
	private int x, y;
	private transient Coord coord = null;
	
	public HasLocation() {
		
		reset();
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
	
	public void set(Coord position) {
		
		setX((position == null) ? 0 : position.x);
		setY((position == null) ? 0 : position.y);
		
		coord = position;
	}
	
	public Coord get() {
		
		if (coord == null)
			coord = Coord.get(x, y);
		
		return coord;
	}
	
	@Override
	public void reset() {
		
		this.x = 0;
		this.y = 0;
		this.coord = null;
	}
}
