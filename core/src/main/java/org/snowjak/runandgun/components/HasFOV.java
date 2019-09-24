/**
 * 
 */
package org.snowjak.runandgun.components;

import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.map.Map;

import com.badlogic.ashley.core.Component;

import squidpony.squidmath.GreasedRegion;

/**
 * Indicates that an entity has a defined Field-Of-View, outside of which
 * nothing is visible to it.
 * 
 * @author snowjak88
 *
 */
public class HasFOV implements Component {
	
	private int distance;
	private GreasedRegion currentlySeen;
	private double[][] lightLevels;
	
	public HasFOV() {
		
		this(32767);
	}
	
	public HasFOV(int distance) {
		
		this.distance = distance;
		
		final Map map = Context.get().map();
		if (map != null)
			resize(map.getWidth(), map.getHeight());
	}
	
	/**
	 * Should the Map be resized, you should call this to resize this Component.
	 * 
	 * @param width
	 * @param height
	 */
	public void resize(int width, int height) {
		
		lightLevels = new double[width][height];
		currentlySeen = new GreasedRegion(width, height);
	}
	
	public double[][] getLightLevels() {
		
		return lightLevels;
	}
	
	public GreasedRegion getCurrentlySeen() {
		
		return currentlySeen;
	}
	
	public GreasedRegion recalculateCurrentlySeen() {
		
		return currentlySeen.refill(lightLevels, 0.0).not();
	}
	
	public int getDistance() {
		
		return distance;
	}
	
	public void setDistance(int distance) {
		
		this.distance = distance;
	}
}
