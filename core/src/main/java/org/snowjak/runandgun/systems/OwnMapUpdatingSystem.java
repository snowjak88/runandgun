/**
 * 
 */
package org.snowjak.runandgun.systems;

import org.snowjak.runandgun.components.CanSee;
import org.snowjak.runandgun.components.HasMap;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.map.GlobalMap;
import org.snowjak.runandgun.map.KnownMap;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

/**
 * Given the entity's current {@link CanSee field-of-view}, update its
 * {@link HasMap personal} {@link KnownMap}.
 * 
 * @author snowjak88
 *
 */
public class OwnMapUpdatingSystem extends IteratingSystem {
	
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	private static final ComponentMapper<CanSee> CAN_SEE = ComponentMapper.getFor(CanSee.class);
	
	public OwnMapUpdatingSystem() {
		
		super(Family.all(HasMap.class, CanSee.class).get());
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		if (!CAN_SEE.has(entity) || !HAS_MAP.has(entity))
			return;
		
		final GlobalMap map = Context.get().globalMap();
		if (map == null)
			return;
		
		HAS_MAP.get(entity).getMap().updateEntities(map, CAN_SEE.get(entity).getSeen());
		
	}
}
