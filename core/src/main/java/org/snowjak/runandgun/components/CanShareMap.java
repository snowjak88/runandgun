/**
 * 
 */
package org.snowjak.runandgun.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.GreasedRegion;

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
	private short[] seenSinceLastReported = CoordPacker.ALL_WALL;
	
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
	
	public short[] getSeenSinceLastReported() {
		
		synchronized (this) {
			if (seenSinceLastReported == null)
				seenSinceLastReported = CoordPacker.ALL_WALL;
			
			return seenSinceLastReported;
		}
	}
	
	public GreasedRegion getSeenSinceLastReportedRegion(int width, int height) {
		
		synchronized (this) {
			return CoordPacker.unpackGreasedRegion(seenSinceLastReported, width, height);
		}
	}
	
	public void insertSeenSinceLastReported(short[] visible) {
		
		synchronized (this) {
			seenSinceLastReported = CoordPacker.unionPacked(seenSinceLastReported, visible);
		}
	}
	
	public void insertSeenSinceLastReported(GreasedRegion region) {
		
		synchronized (this) {
			seenSinceLastReported = CoordPacker.insertSeveralPacked(seenSinceLastReported, region);
		}
	}
	
	public void clearSeenSinceLastReported() {
		
		synchronized (this) {
			seenSinceLastReported = CoordPacker.ALL_WALL;
		}
	}
	
	@Override
	public void reset() {
		
		radius = 0;
		radioEquipped = false;
		seenSinceLastReported = CoordPacker.ALL_WALL;
	}
	
}
