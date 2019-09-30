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
import org.snowjak.runandgun.map.GlobalMap;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

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
public class PathfindingSystem extends IteratingSystem {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(PathfindingSystem.class.getName());
	
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	private static final ComponentMapper<NeedsMovementList> NEEDS_MOVEMENT = ComponentMapper
			.getFor(NeedsMovementList.class);
	private static final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	
	private DijkstraMap dijkstra;
	
	public PathfindingSystem() {
		
		super(Family.all(HasLocation.class, NeedsMovementList.class).get());
		
		dijkstra = new DijkstraMap();
		dijkstra.measurement = Measurement.EUCLIDEAN;
		
		setProcessing(false);
	}
	
	public void setMap(GlobalMap map) {
		
		if (map != null)
			dijkstra.initialize(map.getBareMap());
		
		setProcessing((map != null));
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		if (!NEEDS_MOVEMENT.has(entity))
			return;
		final NeedsMovementList needsMovement = NEEDS_MOVEMENT.get(entity);
		
		if (!HAS_LOCATION.has(entity))
			return;
		final HasLocation location = HAS_LOCATION.get(entity);
		
		final Coord startGoal = location.get();
		final Coord endGoal = needsMovement.getMapPoint();
		
		final Collection<Coord> impassable;
		if (HAS_MAP.has(entity))
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
