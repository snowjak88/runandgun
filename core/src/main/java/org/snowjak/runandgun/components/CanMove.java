/**
 * 
 */
package org.snowjak.runandgun.components;

import com.badlogic.ashley.core.Component;

/**
 * Indicates that an entity can move.
 * 
 * @author snowjak88
 *
 */
public class CanMove implements Component {
	
	private float speed;
	private boolean ignoresTerrain;
	
	/**
	 * Initializes a default {@link CanMove} with speed (2.5 cells per second) and
	 * ignores-terrain (false).
	 */
	public CanMove() {
		
		this(2.5f, false);
	}
	
	public CanMove(float speed, boolean ignoresTerrain) {
		
		this.speed = speed;
		this.ignoresTerrain = ignoresTerrain;
	}
	
	public float getSpeed() {
		
		return speed;
	}
	
	public void setSpeed(float speed) {
		
		this.speed = speed;
	}
	
	public boolean isIgnoresTerrain() {
		
		return ignoresTerrain;
	}
	
	public void setIgnoresTerrain(boolean ignoresTerrain) {
		
		this.ignoresTerrain = ignoresTerrain;
	}
	
}
