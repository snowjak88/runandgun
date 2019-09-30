/**
 * 
 */
package org.snowjak.runandgun.commanders;

import org.snowjak.runandgun.commands.Command;
import org.snowjak.runandgun.commands.MoveToCommand;
import org.snowjak.runandgun.components.CanMove;
import org.snowjak.runandgun.components.HasLocation;
import org.snowjak.runandgun.components.HasMap;
import org.snowjak.runandgun.components.HasMovementList;
import org.snowjak.runandgun.components.IsMoving;
import org.snowjak.runandgun.context.Context;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;

import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;

/**
 * A simple AI {@link Commander} which simply commands all its units to wander.
 * 
 * @author snowjak88
 *
 */
public class SimpleWanderingCommander extends Commander {
	
	private static final long serialVersionUID = -5778276341012878888L;
	
	public static final int WANDER_DISTANCE = 3;
	
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	private final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	
	public SimpleWanderingCommander() {
		
		super((int) serialVersionUID,
				Family.all(CanMove.class, HasLocation.class).exclude(HasMovementList.class, IsMoving.class).get());
	}
	
	@Override
	public Command getCommand(Entity entity) {
		
		final GreasedRegion floors;
		if (HAS_MAP.has(entity))
			floors = HAS_MAP.get(entity).getMap().getKnownRegion('#').not();
		else
			floors = Context.get().map().getNonObstructing();
		
		final GreasedRegion wanderable = new GreasedRegion(floors.width, floors.height)
				.insertCircle(HAS_LOCATION.get(entity).get(), WANDER_DISTANCE).and(floors);
		
		final Coord newDestination = wanderable.singleRandom(Context.get().rng());
		
		return new MoveToCommand(newDestination);
	}
	
}
