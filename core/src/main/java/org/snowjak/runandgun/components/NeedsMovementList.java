/**
 * 
 */
package org.snowjak.runandgun.components;

import org.snowjak.runandgun.util.loaders.IgnoreSerialization;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

import squidpony.squidmath.Coord;

/**
 * Indicates that an entity needs a pathfinding-system to provide a
 * {@link HasMovementList movement-list}.
 * 
 * @author snowjak88
 *
 */
@IgnoreSerialization
public class NeedsMovementList implements Component, Poolable {
	
	private Coord mapPoint = null;
	
	public Coord getMapPoint() {
		
		return mapPoint;
	}
	
	public void setMapPoint(Coord mapPoint) {
		
		this.mapPoint = mapPoint;
	}
	
	@Override
	public void reset() {
		
		this.mapPoint = null;
	}
	
}
