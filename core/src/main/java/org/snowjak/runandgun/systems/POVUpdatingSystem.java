/**
 * 
 */
package org.snowjak.runandgun.systems;

import org.snowjak.runandgun.components.HasLocation;
import org.snowjak.runandgun.components.IsPOV;
import org.snowjak.runandgun.context.Context;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

/**
 * @author snowjak88
 *
 */
public class POVUpdatingSystem extends IteratingSystem {
	
	private static final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	
	public POVUpdatingSystem() {
		
		super(Family.all(IsPOV.class, HasLocation.class).get());
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		if (!HAS_LOCATION.has(entity))
			return;
		
		Context.get().pov().updateCenter(HAS_LOCATION.get(entity).getCoord());
	}
}
