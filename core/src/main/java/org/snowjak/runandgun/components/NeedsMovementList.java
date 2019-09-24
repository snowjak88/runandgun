/**
 * 
 */
package org.snowjak.runandgun.components;

import com.badlogic.ashley.core.Component;

import squidpony.squidmath.Coord;

/**
 * Indicates that an entity needs a pathfinding-system to provide a
 * {@link HasMovementList movement-list}.
 * 
 * @author snowjak88
 *
 */
public class NeedsMovementList implements Component {
	
	private final Coord mapPoint;
	
	public NeedsMovementList(Coord mapPoint) {
		
		this.mapPoint = mapPoint;
	}
	
	public Coord getMapPoint() {
		
		return mapPoint;
	}
}
