/**
 * 
 */
package org.snowjak.runandgun.systems;

import org.snowjak.runandgun.components.HasLocation;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.map.GlobalMap;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

/**
 * For all entities that {@link HasLocation have a location}, ensures that the
 * active {@link GlobalMap} is kept updated with their locations.
 * 
 * @author snowjak88
 *
 */
public class MapLocationUpdatingSystem extends IteratingSystem {
	
	private static final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	
	public MapLocationUpdatingSystem() {
		
		super(Family.all(HasLocation.class).get());
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		final GlobalMap map = Context.get().globalMap();
		if (map == null)
			return;
		
		if (!HAS_LOCATION.has(entity))
			return;
		
		final HasLocation location = HAS_LOCATION.get(entity);
		
		if (!map.isEntityAt(entity, location.get()))
			map.setEntity(entity, location.get());
	}
}
