/**
 * 
 */
package org.snowjak.runandgun.commanders;

import org.snowjak.runandgun.commands.Command;
import org.snowjak.runandgun.commands.MoveToCommand;
import org.snowjak.runandgun.components.CanMove;
import org.snowjak.runandgun.components.CanSee;
import org.snowjak.runandgun.components.HasLocation;
import org.snowjak.runandgun.components.HasMovementList;
import org.snowjak.runandgun.components.IsMoving;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.systems.UniqueTagManager;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;

import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;

/**
 * @author snowjak88
 *
 */
public class SimpleFleeingCommander extends Commander {
	
	private static final long serialVersionUID = -2791218537631916770L;
	
	public static final String FLEE_FROM_TAG = "FEAR ME";
	public static final int FLEE_DISTANCE = 8;
	
	private final SimpleWanderingCommander wanderingCommander = new SimpleWanderingCommander();
	
	private static final ComponentMapper<CanSee> CAN_SEE = ComponentMapper.getFor(CanSee.class);
	private static final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	
	public SimpleFleeingCommander() {
		
		super((int) serialVersionUID,
				Family.all(HasLocation.class, CanMove.class).exclude(HasMovementList.class, IsMoving.class).get());
	}
	
	@Override
	public Command getCommand(Entity entity) {
		
		final UniqueTagManager tagManager = Context.get().engine().getSystem(UniqueTagManager.class);
		if (tagManager == null)
			return null;
		
		final CanSee canSee = CAN_SEE.get(entity);
		
		final Entity toBeFeared = tagManager.get(FLEE_FROM_TAG);
		if (toBeFeared == null)
			return null;
		
		if (!canSee.getSeenEntities().contains(toBeFeared))
			return wanderingCommander.getCommand(entity);
		
		if (!HAS_LOCATION.has(toBeFeared))
			return null;
		final HasLocation fleeFromLocation = HAS_LOCATION.get(toBeFeared);
		
		final GreasedRegion known = canSee.getKnownRegion();
		final GreasedRegion floors = canSee.getKnownRegion('#').not();
		final Coord fleeToLocation = new GreasedRegion(known.width, known.height)
				.insertCircle(fleeFromLocation.getCoord(), FLEE_DISTANCE + 3)
				.removeCircle(fleeFromLocation.getCoord(), FLEE_DISTANCE).and(floors).singleRandom(Context.get().rng());
		
		return new MoveToCommand(fleeToLocation);
	}
}
