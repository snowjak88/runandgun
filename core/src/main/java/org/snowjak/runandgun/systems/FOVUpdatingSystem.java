/**
 * 
 */
package org.snowjak.runandgun.systems;

import java.util.logging.Logger;

import org.snowjak.runandgun.components.CanSee;
import org.snowjak.runandgun.components.HasLocation;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.events.CurrentMapChangedEvent;
import org.snowjak.runandgun.map.GlobalMap;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.google.common.eventbus.Subscribe;

import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;

/**
 * @author snowjak88
 *
 */
public class FOVUpdatingSystem extends IteratingSystem {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(FOVUpdatingSystem.class.getName());
	
	private static final ComponentMapper<CanSee> CAN_SEE = ComponentMapper.getFor(CanSee.class);
	private static final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	
	private double[][] scratch_lightLevels;
	
	public FOVUpdatingSystem() {
		
		super(Family.all(CanSee.class, HasLocation.class).get());
		
		if (Context.get().globalMap() != null) {
			final GlobalMap m = Context.get().globalMap();
			scratch_lightLevels = new double[m.getWidth()][m.getHeight()];
		}
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		
		super.addedToEngine(engine);
		
		Context.get().eventBus().register(this);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		
		super.removedFromEngine(engine);
		
		Context.get().eventBus().unregister(this);
	}
	
	@Subscribe
	public void receiveNewMapEvent(CurrentMapChangedEvent event) {
		
		if (Context.get().globalMap() != null) {
			final GlobalMap m = Context.get().globalMap();
			scratch_lightLevels = new double[m.getWidth()][m.getHeight()];
		}
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		if (!CAN_SEE.has(entity) || !HAS_LOCATION.has(entity))
			return;
		
		final CanSee fov = CAN_SEE.get(entity);
		final HasLocation location = HAS_LOCATION.get(entity);
		
		final GlobalMap map = Context.get().globalMap();
		if (map == null)
			return;
		
		FOV.reuseFOV(map.getVisibilityResistance(), scratch_lightLevels, location.getX(), location.getY(),
				fov.getDistance(), Radius.CIRCLE);
		
		fov.setLightLevels(scratch_lightLevels);
	}
}
