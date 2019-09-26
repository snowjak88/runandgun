/**
 * 
 */
package org.snowjak.runandgun.components;

import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.map.Map;

import com.badlogic.ashley.core.Component;

import squidpony.squidmath.CoordPacker;

/**
 * Indicates that an entity has a defined Field-Of-View, outside of which
 * nothing is visible to it.
 * 
 * @author snowjak88
 *
 */
public class HasFOV implements Component {
	
	private int distance;
	
	private int width, height;
	private short[][] packedLightLevels;
	
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
		
		this.width = width;
		this.height = height;
		
		packedLightLevels = null;
	}
	
	public void setLightLevels(double[][] lightLevels) {
		
		packedLightLevels = CoordPacker.packMulti(lightLevels,
				Context.get().config().rules().lighting().getLightingLevelsForPacking());
	}
	
	/**
	 * Get the light-levels as held in this FOV.
	 * <p>
	 * <strong>Note</strong> that this incurs de-compression, and thus probably
	 * shouldn't be done too often. Instead, try to restrict yourself to
	 * {@link #isSeen(int, int)} and {@link #getLightLevel(int, int)}.
	 * </p>
	 * 
	 * @return
	 */
	public double[][] getLightLevels() {
		
		return CoordPacker.unpackMultiDouble(packedLightLevels, width, height,
				Context.get().config().rules().lighting().getLightingLevelsForUnpacking());
	}
	
	public double getLightLevel(int mapX, int mapY) {
		
		final double[] lightLevels = Context.get().config().rules().lighting().getLightingLevelsForUnpacking();
		for (int i = 0; i < lightLevels.length; i++) {
			if (CoordPacker.queryPacked(packedLightLevels[i], mapX, mapY))
				return lightLevels[i];
		}
		return 0.0;
	}
	
	public boolean isSeen(int mapX, int mapY) {
		
		return (getLightLevel(mapX, mapY) > 0.0);
	}
	
	public int getDistance() {
		
		return distance;
	}
	
	public void setDistance(int distance) {
		
		this.distance = distance;
	}
}
