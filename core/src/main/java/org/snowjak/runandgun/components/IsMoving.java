/**
 * 
 */
package org.snowjak.runandgun.components;

import com.badlogic.ashley.core.Component;

/**
 * Indicates that an entity is actively moving, and shouldn't start other moving
 * operations until it has finished.
 * 
 * @author snowjak88
 *
 */
public class IsMoving implements Component {
	
	private float timeRemaining;
	
	public IsMoving() {
		
		this(0);
	}
	
	public IsMoving(float timeRemaining) {
		
		this.timeRemaining = timeRemaining;
	}
	
	public boolean isComplete() {
		
		return (timeRemaining <= 0);
	}
	
	public void decreaseTimeRemaining(float delta) {
		
		timeRemaining -= delta;
	}
}
