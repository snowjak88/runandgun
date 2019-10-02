/**
 * 
 */
package org.snowjak.runandgun.components;

import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.map.GlobalMap;

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
	private char[][] timestamps = null;
	
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
	
	public char[][] getTimestamps() {
		
		if (timestamps == null)
			synchronized (this) {
				final GlobalMap map = Context.get().map();
				if (timestamps == null && map != null)
					timestamps = new char[map.getWidth()][map.getHeight()];
			}
		
		return timestamps;
	}
	
	public void insertTimestamp(short[] region, float timestamp) {
		
		synchronized (this) {
			timestamps = CoordPacker.mask(getTimestamps(), CoordPacker.negatePacked(region),
					convertTimestamp(timestamp));
		}
	}
	
	public void insertTimestamps(short[] region, char[][] timestamps) {
		
		synchronized (this) {
			final char[][] ts = getTimestamps();
			CoordPacker.unpackGreasedRegion(region, ts.length, ts[0].length)
					.forEach(c -> ts[c.x][c.y] = timestamps[c.x][c.y]);
		}
	}
	
	public short[] getTimestampsOlderThan(char[][] otherTimestamps) {
		
		synchronized (this) {
			final char[][] ts = getTimestamps();
			short[] result = CoordPacker.ALL_WALL;
			for (int x = 0; x < ts.length; x++)
				for (int y = 0; y < ts[x].length; y++)
					if (otherTimestamps[x][y] >= ts[x][y])
						result = CoordPacker.insertPacked(result, x, y);
					
			return result;
		}
	}
	
	public float getTimestampAt(int x, int y) {
		
		synchronized (this) {
			return convertTimestamp(getTimestamps()[x][y]);
		}
	}
	
	private static char convertTimestamp(float timestamp) {
		
		return (char) Math.min(Math.max(timestamp, 0) * 10f, Character.MAX_VALUE);
	}
	
	private static float convertTimestamp(char timestamp) {
		
		return (float) timestamp / 10f;
	}
	
	@Override
	public void reset() {
		
		radius = 0;
		radioEquipped = false;
		seenSinceLastReported = CoordPacker.ALL_WALL;
		timestamps = null;
	}
	
}
