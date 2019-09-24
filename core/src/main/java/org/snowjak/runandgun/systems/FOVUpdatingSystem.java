/**
 * 
 */
package org.snowjak.runandgun.systems;

import org.snowjak.runandgun.components.HasFOV;
import org.snowjak.runandgun.components.HasLocation;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.map.Map;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;

/**
 * @author snowjak88
 *
 */
public class FOVUpdatingSystem extends IteratingSystem {
	
	private static final ComponentMapper<HasFOV> HAS_FOV = ComponentMapper.getFor(HasFOV.class);
	private static final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	
	public FOVUpdatingSystem() {
		
		super(Family.all(HasFOV.class, HasLocation.class).get());
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		if (!HAS_FOV.has(entity) || !HAS_LOCATION.has(entity))
			return;
		
		final HasFOV fov = HAS_FOV.get(entity);
		final HasLocation location = HAS_LOCATION.get(entity);
		
		final Map map = Context.get().map();
		if (map == null)
			return;
		
		FOV.reuseFOV(map.getVisibilityResistance(), fov.getLightLevels(), (int) location.getX(), (int) location.getY(),
				fov.getDistance(), Radius.CIRCLE);
		
		Context.get().pov().updateLightLevels(fov.getLightLevels());
	}
}
