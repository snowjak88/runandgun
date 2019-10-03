/**
 * 
 */
package org.snowjak.runandgun.systems;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.snowjak.runandgun.components.HasLocation;
import org.snowjak.runandgun.components.HasMap;
import org.snowjak.runandgun.components.HasMovementList;
import org.snowjak.runandgun.components.NeedsMovementList;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.events.CurrentMapChangedEvent;
import org.snowjak.runandgun.map.GlobalMap;
import org.snowjak.runandgun.team.Team;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IntervalIteratingSystem;

import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.Measurement;
import squidpony.squidmath.Coord;

/**
 * For all entities that {@link NeedsMovementList require a movement-list},
 * performs pathfinding to populate the requested {@link HasMovementList
 * movement-list}. Also removes {@link NeedsMovementList} from the entity;
 * 
 * @author snowjak88
 *
 */
public class PathfindingSystem extends IntervalIteratingSystem {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(PathfindingSystem.class.getName());
	
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	private static final ComponentMapper<NeedsMovementList> NEEDS_MOVEMENT = ComponentMapper
			.getFor(NeedsMovementList.class);
	private static final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	
	private DijkstraMap dijkstra;
	
	public PathfindingSystem() {
		
		super(Family.all(HasLocation.class, NeedsMovementList.class).get(),
				Context.get().config().rules().entities().getPathfindingInterval());
		
		dijkstra = new DijkstraMap();
		dijkstra.measurement = Measurement.EUCLIDEAN;
		
		setProcessing(false);
		
		if (Context.get().globalMap() != null)
			setMap(Context.get().globalMap());
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
	
	public void receiveMapChangeEvent(CurrentMapChangedEvent event) {
		
		setMap(Context.get().globalMap());
	}
	
	public void setMap(GlobalMap map) {
		
		if (map != null)
			dijkstra.initialize(map.getBareMap());
		
		setProcessing((map != null));
	}
	
	@Override
	protected void processEntity(Entity entity) {
		
		if (!NEEDS_MOVEMENT.has(entity))
			return;
		final NeedsMovementList needsMovement = NEEDS_MOVEMENT.get(entity);
		
		if (!HAS_LOCATION.has(entity))
			return;
		final HasLocation location = HAS_LOCATION.get(entity);
		
		final Coord startGoal = location.get();
		final Coord endGoal = needsMovement.getMapPoint();
		
		final Team team = getEngine().getSystem(TeamManager.class).getTeam(entity);
		
		final Collection<Coord> impassable;
		if (team != null)
			impassable = team.getMap().getKnownRegion().not();
		else if (HAS_MAP.has(entity))
			impassable = HAS_MAP.get(entity).getMap().getKnownRegion().not();
		else
			impassable = null;
		
		final List<Coord> movement = dijkstra.findPath(128, 0, impassable, null, startGoal, endGoal);
		final HasMovementList hasMovement = getEngine().createComponent(HasMovementList.class);
		hasMovement.addMovement(movement);
		
		entity.remove(NeedsMovementList.class);
		entity.add(hasMovement);
	}
}
