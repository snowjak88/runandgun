/**
 * 
 */
package org.snowjak.runandgun.systems;

import org.snowjak.runandgun.components.CanMove;
import org.snowjak.runandgun.components.HasGlyph;
import org.snowjak.runandgun.components.HasLocation;
import org.snowjak.runandgun.components.HasMap;
import org.snowjak.runandgun.components.HasMovementList;
import org.snowjak.runandgun.components.IsMoving;
import org.snowjak.runandgun.context.Context;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

/**
 * For all entities that {@link HasMovementList have active movement-lists},
 * ensures that the active movement is executed.
 * <p>
 * If the entity {@link HasGlyph has a glyph} associated with it, this system
 * fires a {@link GlyphMoveStartEvent}.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class MovementListExecutingSystem extends IteratingSystem {
	
	private static final ComponentMapper<HasMovementList> HAS_MOVEMENT = ComponentMapper.getFor(HasMovementList.class);
	private static final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	private static final ComponentMapper<CanMove> CAN_MOVE = ComponentMapper.getFor(CanMove.class);
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	private static final ComponentMapper<HasGlyph> HAS_GLYPH = ComponentMapper.getFor(HasGlyph.class);
	
	public MovementListExecutingSystem() {
		
		super(Family.all(HasMovementList.class, HasLocation.class, CanMove.class).exclude(IsMoving.class).get());
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		if (!HAS_MOVEMENT.has(entity))
			return;
		final HasMovementList movement = HAS_MOVEMENT.get(entity);
		
		//
		// If the movement list is empty, then remove that HasMovementList
		if (!movement.hasMovement()) {
			entity.remove(HasMovementList.class);
			return;
		}
		
		if (!HAS_LOCATION.has(entity))
			return;
		final HasLocation location = HAS_LOCATION.get(entity);
		
		//
		// If our destination is identical with our current location, then advance the
		// movement list.
		while (movement.getCurrent().x == location.getX() && movement.getCurrent().y == location.getY()) {
			movement.advanceList();
			
			//
			// If that advancement has emptied the movement-list, then remove the
			// HasMovementList
			if (!movement.hasMovement()) {
				entity.remove(HasMovementList.class);
				return;
			}
		}
		
		if (!CAN_MOVE.has(entity))
			return;
		final CanMove canMove = CAN_MOVE.get(entity);
		
		//
		// If the clock-speed is 0 or currently pause (which two should be equivalent),
		// don't bother moving.
		if (Context.get().clock().isPaused() || Context.get().clock().getSpeed() == 0)
			return;
		
		final int destinationX = movement.getCurrent().x, destinationY = movement.getCurrent().y;
		final int currentX = location.getX(), currentY = location.getY();
		
		//
		// If the destination turns out to be a wall, don't move into it.
		final boolean isNavigable;
		if (HAS_MAP.has(entity)) {
			isNavigable = HAS_MAP.get(entity).getMap().getMapAt(destinationX, destinationY) != '#';
		} else
			isNavigable = Context.get().map().getBareMap()[destinationX][destinationY] != '#';
		
		if (!isNavigable) {
			movement.advanceList();
			return;
		}
		
		//
		// How fast can we cover the distance involved, given our speed and the
		// terrain-resistance?
		final float totalDistance = (float) Math
				.sqrt(Math.pow(currentX - destinationX, 2) + Math.pow(currentY - destinationY, 2));
		final float totalTime = totalDistance / canMove.getSpeed() * Context.get().clock().getSpeed();
		// TODO add reference to movement-resistance here
		
		location.setX(destinationX);
		location.setY(destinationY);
		
		final IsMoving isMoving = getEngine().createComponent(IsMoving.class);
		isMoving.setTimeRemaining(totalTime);
		entity.add(isMoving);
		
		if (HAS_GLYPH.has(entity))
			Context.get().glyphControl().move(HAS_GLYPH.get(entity).getGlyph(), currentX, currentY, destinationX,
					destinationY, totalTime);
	}
}
