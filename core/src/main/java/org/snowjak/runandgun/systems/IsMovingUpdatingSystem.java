/**
 * 
 */
package org.snowjak.runandgun.systems;

import org.snowjak.runandgun.components.IsMoving;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

/**
 * Updates entities which {@link IsMoving are moving} -- specifically, updates
 * their "time-remaining".
 * 
 * @author snowjak88
 *
 */
public class IsMovingUpdatingSystem extends IteratingSystem {
	
	private static final ComponentMapper<IsMoving> IS_MOVING = ComponentMapper.getFor(IsMoving.class);
	
	public IsMovingUpdatingSystem() {
		
		super(Family.all(IsMoving.class).get());
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		if (!IS_MOVING.has(entity))
			return;
		final IsMoving isMoving = IS_MOVING.get(entity);
		
		if (isMoving.isComplete())
			entity.remove(IsMoving.class);
		else
			isMoving.decreaseTimeRemaining(deltaTime);
	}
}
