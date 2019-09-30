/**
 * 
 */
package org.snowjak.runandgun.components;

import org.snowjak.runandgun.config.RulesConfiguration.LightingRulesConfiguration;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.map.GlobalMap;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.GreasedRegion;

/**
 * Indicates that an entity can see. It has a defined FOV, and knowledge about
 * the map.
 * 
 * @author snowjak88
 *
 */
public class CanSee implements Component, Poolable {
	
	private int distance;
	
	private int width, height;
	private short[][] packedLightLevels;
	private short[] seen;
	
	public void init() {
		
		reset();
		
		final GlobalMap m = Context.get().map();
		if (m != null)
			resize(m.getWidth(), m.getHeight());
	}
	
	/**
	 * Should the GlobalMap be resized, you should call this to resize this Component.
	 * 
	 * @param width
	 * @param height
	 */
	public void resize(int width, int height) {
		
		synchronized (this) {
			if (this.width == width && this.height == height)
				return;
			
			this.width = width;
			this.height = height;
			
			packedLightLevels = null;
			seen = CoordPacker.ALL_WALL;
		}
	}
	
	/**
	 * Update the held FOV "light-levels". This need not strictly equate to "light",
	 * but more generally to "visibility".
	 * 
	 * @param lightLevels
	 */
	public void setLightLevels(double[][] lightLevels) {
		
		synchronized (this) {
			packedLightLevels = CoordPacker.packMulti(lightLevels,
					Context.get().config().rules().lighting().getLightingLevelsForPacking());
			
			seen = CoordPacker.pack(lightLevels);
		}
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
		
		synchronized (this) {
			return CoordPacker.unpackMultiDouble(packedLightLevels, width, height,
					Context.get().config().rules().lighting().getLightingLevelsForUnpacking());
		}
	}
	
	/**
	 * Get this CanSee's "light-level" for the given map-location.
	 * 
	 * <p>
	 * Note that, with compression, light-levels will be reported in distinct
	 * {@link LightingRulesConfiguration#getLevels() levels}.
	 * </p>
	 * 
	 * @param mapX
	 * @param mapY
	 * @return
	 */
	public double getLightLevel(int mapX, int mapY) {
		
		synchronized (this) {
			final double[] lightLevels = Context.get().config().rules().lighting().getLightingLevelsForUnpacking();
			for (int i = 0; i < lightLevels.length; i++) {
				if (CoordPacker.queryPacked(packedLightLevels[i], mapX, mapY))
					return lightLevels[i];
			}
			return 0.0;
		}
	}
	
	/**
	 * @return a {@link GreasedRegion} giving the "currently-seen" parts of the map
	 */
	public GreasedRegion getSeenRegion() {
		
		synchronized (this) {
			return CoordPacker.unpackGreasedRegion(seen, width, height);
		}
	}
	
	/**
	 * Does the {@link #getLightLevel(int, int) light-level for the given square}
	 * exceed 0.0?
	 * 
	 * @param mapX
	 * @param mapY
	 * @return
	 */
	public boolean isSeen(int mapX, int mapY) {
		
		synchronized (this) {
			return CoordPacker.queryPacked(seen, mapX, mapY);
		}
	}
	
	/**
	 * @return this CanSee's given visibility-distance
	 */
	public int getDistance() {
		
		return distance;
	}
	
	/**
	 * Set this CanSee's visibility-distance.
	 * 
	 * @param distance
	 */
	public void setDistance(int distance) {
		
		this.distance = distance;
	}
	
	@Override
	public void reset() {
		
		this.distance = 32767;
		this.seen = CoordPacker.ALL_WALL;
		this.packedLightLevels = null;
	}
}
