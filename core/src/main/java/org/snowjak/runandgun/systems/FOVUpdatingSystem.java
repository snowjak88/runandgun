/**
 * 
 */
package org.snowjak.runandgun.systems;

import java.util.LinkedList;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import org.snowjak.runandgun.components.CanSee;
import org.snowjak.runandgun.components.CanShareMap;
import org.snowjak.runandgun.components.HasLocation;
import org.snowjak.runandgun.components.HasMap;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.events.CurrentMapChangedEvent;
import org.snowjak.runandgun.map.GlobalMap;
import org.snowjak.runandgun.map.KnownMap;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Radius;

/**
 * @author snowjak88
 *
 */
public class FOVUpdatingSystem extends IteratingSystem {
	
	private static final Logger LOG = Logger.getLogger(FOVUpdatingSystem.class.getName());
	
	private static final ComponentMapper<CanSee> CAN_SEE = ComponentMapper.getFor(CanSee.class);
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	private static final ComponentMapper<CanShareMap> CAN_SHARE_MAP = ComponentMapper.getFor(CanShareMap.class);
	private static final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	
	private double[][] scratch_lightLevels;
	
	private final LinkedList<ListenableFuture<?>> updates = new LinkedList<>();
	
	public FOVUpdatingSystem() {
		
		super(Family.all(CanSee.class, HasLocation.class).get());
		
		if (Context.get().map() != null) {
			final GlobalMap m = Context.get().map();
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
		
		if (Context.get().map() != null) {
			final GlobalMap m = Context.get().map();
			scratch_lightLevels = new double[m.getWidth()][m.getHeight()];
		}
	}
	
	@Override
	public void update(float deltaTime) {
		
		updates.clear();
		
		super.update(deltaTime);
		
		try {
			Futures.whenAllComplete(updates).run(() -> {
			}, MoreExecutors.directExecutor()).get();
		} catch (ExecutionException | InterruptedException e) {
			LOG.severe("Unexpected exception while updating entities' FOV -- " + e.getClass().getSimpleName() + ": "
					+ e.getMessage());
		}
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		if (!CAN_SEE.has(entity) || !HAS_LOCATION.has(entity))
			return;
		
		final CanSee fov = CAN_SEE.get(entity);
		final HasLocation location = HAS_LOCATION.get(entity);
		
		final GlobalMap map = Context.get().map();
		if (map == null)
			return;
		
		FOV.reuseFOV(map.getVisibilityResistance(), scratch_lightLevels, location.getX(), location.getY(),
				fov.getDistance(), Radius.CIRCLE);
		
		fov.setLightLevels(scratch_lightLevels);
		
		if (HAS_MAP.has(entity)) {
			final KnownMap knownMap = HAS_MAP.get(entity).getMap();
			final short[] seenRegion = fov.getSeen();
			
			updates.add(Context.get().executor().submit(() -> {
				//
				// Update the known map from the global map, but only for what we can see
				knownMap.insertMap(map, seenRegion);
				
				//
				// If this entity can share its map, we need to make sure that we update the
				// record of "map-locations seen since last upload"
				if (CAN_SHARE_MAP.has(entity)) {
					final CanShareMap csm = CAN_SHARE_MAP.get(entity);
					csm.insertSeenSinceLastReported(seenRegion);
					csm.insertTimestamp(seenRegion, Context.get().clock().getTimestamp());
				}
			}));
		}
		
	}
}
