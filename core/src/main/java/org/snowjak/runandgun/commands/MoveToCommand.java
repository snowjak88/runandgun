/**
 * 
 */
package org.snowjak.runandgun.commands;

import org.snowjak.runandgun.components.NeedsMovementList;

import com.badlogic.ashley.core.Entity;

import squidpony.squidmath.Coord;

/**
 * Represents the command to move to some map location.
 * 
 * @author snowjak88
 *
 */
public class MoveToCommand implements Command {
	
	private final Coord mapPoint;
	
	public MoveToCommand(Coord mapPoint) {
		
		this.mapPoint = mapPoint;
	}
	
	public Coord getMapPoint() {
		
		return mapPoint;
	}
	
	@Override
	public boolean isEntitySpecific() {
		
		return true;
	}
	
	@Override
	public void execute() {
		
		throw new IllegalStateException();
	}
	
	@Override
	public void execute(Entity entity) {
		
		entity.add(new NeedsMovementList(mapPoint));
	}
	
}
