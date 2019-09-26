/**
 * 
 */
package org.snowjak.runandgun.commanders;

import java.util.Optional;

import org.snowjak.runandgun.commands.Command;
import org.snowjak.runandgun.commands.MoveToCommand;
import org.snowjak.runandgun.components.CanMove;
import org.snowjak.runandgun.components.HasLocation;
import org.snowjak.runandgun.components.HasMovementList;
import org.snowjak.runandgun.components.IsMoving;
import org.snowjak.runandgun.context.Context;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;

import squidpony.squidmath.Coord;

/**
 * A simple AI {@link Commander} which simply commands all its units to wander.
 * 
 * @author snowjak88
 *
 */
public class SimpleWanderingCommander implements Commander {
	
	private static final long serialVersionUID = -5778276341012878888L;
	
	private final ComponentMapper<CanMove> CAN_MOVE = ComponentMapper.getFor(CanMove.class);
	private final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	private final ComponentMapper<HasMovementList> HAS_MOVEMENT_LIST = ComponentMapper.getFor(HasMovementList.class);
	private final ComponentMapper<IsMoving> IS_MOVING = ComponentMapper.getFor(IsMoving.class);
	
	@Override
	public int getID() {
		
		return (int) serialVersionUID;
	}
	
	@Override
	public Optional<Command> getCommand(Entity entity) {
		
		if (!CAN_MOVE.has(entity) || !HAS_LOCATION.has(entity) || HAS_MOVEMENT_LIST.has(entity)
				|| IS_MOVING.has(entity))
			return Optional.empty();
		
		final Coord newDestination = Context.get().map().getFloors().singleRandom(Context.get().rng());
		return Optional.of(new MoveToCommand(newDestination));
	}
	
}
