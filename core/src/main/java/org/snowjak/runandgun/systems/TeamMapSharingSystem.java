/**
 * 
 */
package org.snowjak.runandgun.systems;

import java.util.Collection;
import java.util.logging.Logger;

import org.snowjak.runandgun.components.CanSee;
import org.snowjak.runandgun.components.CanShareMap;
import org.snowjak.runandgun.components.HasLocation;
import org.snowjak.runandgun.components.HasMap;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.map.KnownMap;
import org.snowjak.runandgun.team.Team;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IntervalIteratingSystem;

import squidpony.squidmath.CoordPacker;

/**
 * For all entities which {@link CanShareMap can share} {@link HasMap their
 * maps}, shares their maps between all neighboring entities on the same
 * {@link Team} (out to {@link CanShareMap#getRadius()}
 * 
 * @author snowjak88
 *
 */
public class TeamMapSharingSystem extends IntervalIteratingSystem {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(TeamMapSharingSystem.class.getName());
	
	private static final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	private static final ComponentMapper<CanShareMap> CAN_SHARE_MAP = ComponentMapper.getFor(CanShareMap.class);
	private static final ComponentMapper<CanSee> CAN_SEE = ComponentMapper.getFor(CanSee.class);
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	
	public TeamMapSharingSystem() {
		
		super(Family.all(HasLocation.class, CanShareMap.class, HasMap.class).get(),
				Context.get().config().rules().entities().getMapSharingInterval());
	}
	
	@Override
	protected void processEntity(Entity entity) {
		
		final TeamManager teamManager = getEngine().getSystem(TeamManager.class);
		if (teamManager == null)
			return;
		
		final Team team = teamManager.getTeam(entity);
		if (team == null)
			return;
		
		if (!HAS_LOCATION.has(entity) || !CAN_SHARE_MAP.has(entity) || !HAS_MAP.has(entity))
			return;
		
		final KnownMap thisMap = HAS_MAP.get(entity).getMap();
		final int sharingRadius = CAN_SHARE_MAP.get(entity).getRadius();
		
		if (sharingRadius > 0) {
			
			//
			// Update nearby entities on the same team.
			//
			// LOG.info("updating team-members within " + sharingRadius + " cells ...");
			
			final Collection<Entity> nearby = HAS_MAP.get(entity).getMap()
					.getEntitiesNear(HAS_LOCATION.get(entity).get(), sharingRadius);
			
			// LOG.info("updating team-members -- found " + nearby.size() + " nearby
			// entities ...");
			
			//
			// We want to copy from neighboring team-members, *except* where we can actually
			// see for ourselves.
			final short[] seen = CAN_SEE.get(entity).getSeen();
			
			teamManager.filterEntitiesByTeam(nearby, team).forEach(e -> {
				if (e != entity)
					if (HAS_MAP.has(e)) {
						final short[] toCopy;
						if (CAN_SHARE_MAP.has(e)) {
							final short[] olderCells = CAN_SHARE_MAP.get(entity)
									.getTimestampsOlderThan(CAN_SHARE_MAP.get(e).getTimestamps());
							toCopy = CoordPacker.differencePacked(olderCells, seen);
						} else
							toCopy = CoordPacker.negatePacked(seen);
						
						thisMap.insertMap(HAS_MAP.get(e).getMap(), toCopy, true);
						CAN_SHARE_MAP.get(entity).insertTimestamps(toCopy, CAN_SHARE_MAP.get(e).getTimestamps());
						CAN_SHARE_MAP.get(entity).insertSeenSinceLastReported(thisMap.getKnown());
					}
			});
			
			// LOG.info("finished updating team-members");
		}
	}
}
