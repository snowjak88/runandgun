/**
 * 
 */
package org.snowjak.runandgun.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Indicates that this entity can share its {@link HasMap map} with other
 * entities (e.g., on the same team).
 * 
 * @author snowjak88
 *
 */
public class CanShareMap implements Component, Poolable {
	
	private int radius;
	private boolean radioEquipped;
	
	public int getRadius() {
		
		return radius;
	}
	
	public void setRadius(int radius) {
		
		this.radius = radius;
	}
	
	public boolean isRadioEquipped() {
		
		return radioEquipped;
	}
	
	public void setRadioEquipped(boolean radioEquipped) {
		
		this.radioEquipped = radioEquipped;
	}
	
	@Override
	public void reset() {
		
		radius = 0;
		radioEquipped = false;
	}
	
}
