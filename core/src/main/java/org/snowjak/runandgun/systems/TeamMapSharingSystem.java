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

import squidpony.squidmath.GreasedRegion;

/**
 * For all entities which {@link CanShareMap can share} {@link HasMap their
 * maps}, shares their maps between:
 * <ul>
 * <li>All neighboring entities on the same {@link Team} (out to
 * {@link CanShareMap#getRadius()}</li>
 * <li>Their associated {@link Team#getMap() Team's map}</li>
 * </ul>
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
	protected void updateInterval() {
		
		getEngine().getSystem(TeamManager.class).getTeams().forEach(Team::resetVisibility);
		super.updateInterval();
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
		final boolean isRadioEquipped = CAN_SHARE_MAP.get(entity).isRadioEquipped();
		final int sharingRadius = CAN_SHARE_MAP.get(entity).getRadius();
		
		if (isRadioEquipped) {
			
			//
			// Update the Team's known-map
			//
			// LOG.info("is radio-equipped -- updating Team map ...");
			final GreasedRegion visible;
			if (CAN_SEE.has(entity))
				visible = CAN_SEE.get(entity).getSeenRegion();
			else
				visible = null;
			
			team.update(thisMap, visible);
			
		}
		
		if (sharingRadius > 0) {
			
			//
			// Update nearby entities on the same team.
			//
			// LOG.info("updating team-members within " + sharingRadius + " cells ...");
			
			final Collection<Entity> nearby = HAS_MAP.get(entity).getMap()
					.getEntitiesNear(HAS_LOCATION.get(entity).get(), sharingRadius);
			
			// LOG.info("updating team-members -- found " + nearby.size() + " nearby
			// entities ...");
			
			// for (Entity e : nearby) {
			// if (e != entity && teamManager.getTeam(e) == team) {
			//// LOG.info("nearby entity is on our team! ([" +
			// teamManager.getTeam(e).toString() + "] / ["
			//// + team.toString() + "]");
			//
			// if (HAS_MAP.has(e)) {
			//// LOG.info("nearby entity has a map to update");
			// HAS_MAP.get(e).getMap().updateMap(thisMap);
			//// LOG.info("finished updating nearby map");
			// } else {
			//// LOG.info("nearby entity does NOT have a map to update");
			// }
			// } else {
			// // LOG.info("nearby entity is NOT on our team! ([" +
			// // teamManager.getTeam(e).toString() + "] / ["
			// // + team.toString() + "]");
			// }
			// }
			teamManager.filterEntitiesByTeam(nearby, team).forEach(e -> {
				if (e != entity)
					if (HAS_MAP.has(e))
						HAS_MAP.get(e).getMap().updateMap(thisMap);
			});
			
			// LOG.info("finished updating team-members");
		}
	}
}
