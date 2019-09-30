/**
 * 
 */
package org.snowjak.runandgun.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Indicates that an entity can move.
 * 
 * @author snowjak88
 *
 */
public class CanMove implements Component, Poolable {
	
	private float speed;
	private boolean ignoresTerrain;
	
	public void init() {
		
		reset();
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
	
	@Override
	public void reset() {
		
		this.speed = 2.5f;
		this.ignoresTerrain = false;
	}
	
}
