/**
 * 
 */
package org.snowjak.runandgun.systems;

import org.snowjak.runandgun.components.CanMove;
import org.snowjak.runandgun.components.HasGlyph;
import org.snowjak.runandgun.components.HasLocation;
import org.snowjak.runandgun.components.HasMovementList;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.events.GlyphMovedEvent;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

/**
 * For all entities that {@link HasMovementList have active movement-lists},
 * ensures that the active movement is executed.
 * <p>
 * If the entity {@link HasGlyph has a glyph} associated with it, this system
 * fires a {@link GlyphMovedEvent}.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class MovementListExecutingSystem extends IteratingSystem {
	
	private static final ComponentMapper<HasMovementList> HAS_MOVEMENT = ComponentMapper.getFor(HasMovementList.class);
	private static final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	private static final ComponentMapper<CanMove> CAN_MOVE = ComponentMapper.getFor(CanMove.class);
	private static final ComponentMapper<HasGlyph> HAS_GLYPH = ComponentMapper.getFor(HasGlyph.class);
	
	public MovementListExecutingSystem() {
		
		super(Family.all(HasMovementList.class, HasLocation.class, CanMove.class).get());
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
		if (movement.getCurrent().x == (int) location.getX() && movement.getCurrent().y == (int) location.getY()) {
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
		
		final float destinationX = movement.getCurrent().x, destinationY = movement.getCurrent().y;
		final float currentX = location.getX(), currentY = location.getY();
		
		//
		// How much of the desired distance can we cover in unit-time?
		//
		// Of course, we're actually operating in a fraction of a second.
		//
		final float totalDistance = (float) Math
				.sqrt(Math.pow(currentX - destinationX, 2) + Math.pow(currentY - destinationY, 2));
		
		final float dt = deltaTime * Context.get().clock().getSpeed();
		
		//
		// How far can we possibly go within this tick?
		final float maxDistanceThisTick = canMove.getSpeed() * dt;
		
		//
		// Calculate the fraction of the desired distance which our maximum-possible
		// distance represents.
		//
		// Note that we cap this fraction at 1.0, because there's no sense in traveling
		// past our destination.
		final float totalDistanceFraction = Math.min(maxDistanceThisTick / totalDistance, 1f);
		
		//
		// dx, dy specify the relative fractions of the total distance we can cover
		// in this tick.
		final float dx = (destinationX - currentX) * totalDistanceFraction,
				dy = (destinationY - currentY) * totalDistanceFraction;
		
		//
		// Update the location. Save the integer-valued coordinates before and after.
		//
		final int beforeX = (int) currentX, beforeY = (int) currentY;
		
		location.setX(currentX + dx);
		location.setY(currentY + dy);
		
		final int afterX = (int) location.getX(), afterY = (int) location.getY();
		
		if (beforeX != afterX || beforeY != afterY && HAS_GLYPH.has(entity))
			Context.get().eventBus()
					.post(new GlyphMovedEvent(HAS_GLYPH.get(entity).getGlyph(), beforeX, beforeY, afterX, afterY));
	}
}
