/**
 * 
 */
package org.snowjak.runandgun.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Indicates that an entity is actively moving, and shouldn't start other moving
 * operations until it has finished.
 * 
 * @author snowjak88
 *
 */
public class IsMoving implements Component, Poolable {
	
	private float timeRemaining = 0;
	
	public float getTimeRemaining() {
		
		return timeRemaining;
	}
	
	public void setTimeRemaining(float timeRemaining) {
		
		this.timeRemaining = timeRemaining;
	}
	
	public boolean isComplete() {
		
		return (timeRemaining <= 0);
	}
	
	public void decreaseTimeRemaining(float delta) {
		
		timeRemaining -= delta;
	}
	
	@Override
	public void reset() {
		
		this.timeRemaining = 0;
	}
}
