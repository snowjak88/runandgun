/**
 * 
 */
package org.snowjak.runandgun.systems;

import java.util.logging.Logger;

import org.snowjak.runandgun.components.CanSee;
import org.snowjak.runandgun.concurrent.ParallelRunner;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.map.GlobalMap;
import org.snowjak.runandgun.map.KnownMap;
import org.snowjak.runandgun.team.Team;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

/**
 * Given the entity's current {@link CanSee field-of-view}, update its
 * {@link Team}'s {@link KnownMap}.
 * 
 * @author snowjak88
 *
 */
public class TeamMapUpdatingSystem extends IteratingSystem {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(TeamMapUpdatingSystem.class.getName());
	
	private static final ComponentMapper<CanSee> CAN_SEE = ComponentMapper.getFor(CanSee.class);
	
	private final ParallelRunner runner = new ParallelRunner();
	
	public TeamMapUpdatingSystem() {
		
		super(Family.all(CanSee.class).get());
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
	
	@Override
	public void update(float deltaTime) {
		
		getEngine().getSystem(TeamManager.class).getTeams().forEach(t -> t.getMap().resetVisibleRegion());
		
		super.update(deltaTime);
		
		runner.awaitAll();
		
		Context.get().setDisplayMap(Context.get().team().getMap().copy());
	}
	
	/**
	 * Update each entity's Team's map.
	 */
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		if (!CAN_SEE.has(entity))
			return;
		
		final GlobalMap map = Context.get().globalMap();
		if (map == null)
			return;
		
		final Team team = getEngine().getSystem(TeamManager.class).getTeam(entity);
		if (team == null)
			return;
		
		runner.add(() -> team.update(map, CAN_SEE.get(entity).getSeen()));
	}
}
