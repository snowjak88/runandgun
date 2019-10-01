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
	
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	
	public SimpleFleeingCommander() {
		
		super((int) serialVersionUID,
				Family.all(HasLocation.class, CanMove.class).exclude(HasMovementList.class, IsMoving.class).get());
	}
	
	@Override
	public Command getCommand(Entity entity) {
		
		final UniqueTagManager tagManager = Context.get().engine().getSystem(UniqueTagManager.class);
		if (tagManager == null)
			return null;
		
		final HasMap hasMap = HAS_MAP.get(entity);
		
		final Entity toBeFeared = tagManager.get(FLEE_FROM_TAG);
		if (toBeFeared == null)
			return null;
		
		final Coord fearfulSpot = hasMap.getMap().getEntityLocation(toBeFeared);
		if (fearfulSpot == null)
			return wanderingCommander.getCommand(entity);
		
		final GreasedRegion known = hasMap.getMap().getKnownRegion(),
				floors = hasMap.getMap().getKnownRegion('#').not();
		final GreasedRegion fleeToRegion = new GreasedRegion(known.width, known.height).insert(fearfulSpot)
				.expand8way(FLEE_DISTANCE + 3);
		final GreasedRegion fleeFromRegion = new GreasedRegion(known.width, known.height).insert(fearfulSpot)
				.expand8way(FLEE_DISTANCE);
		final Coord fleeToLocation = fleeToRegion.and(floors).andNot(fleeFromRegion).singleRandom(Context.get().rng());
		
		return new MoveToCommand(fleeToLocation);
	}
}
