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
import org.snowjak.runandgun.map.KnownMap;
import org.snowjak.runandgun.team.Team;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IntervalIteratingSystem;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

import squidpony.squidmath.CoordPacker;

/**
 * For all entities which {@link CanShareMap can share} {@link HasMap their
 * maps}, updates their {@link KnownMap}s from the {@link Team}'s KnownMap.
 * 
 * @author snowjak88
 *
 */
public class TeamMapDownloadingSystem extends IntervalIteratingSystem {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(TeamMapDownloadingSystem.class.getName());
	
	private static final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	private static final ComponentMapper<CanShareMap> CAN_SHARE_MAP = ComponentMapper.getFor(CanShareMap.class);
	private static final ComponentMapper<CanSee> CAN_SEE = ComponentMapper.getFor(CanSee.class);
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	
	public final LinkedList<ListenableFuture<?>> updates = new LinkedList<>();
	
	public TeamMapDownloadingSystem() {
		
		super(Family.all(HasLocation.class, CanShareMap.class, HasMap.class).get(),
				Context.get().config().rules().entities().getMapSharingInterval());
	}
	
	@Override
	protected void updateInterval() {
		
		updates.clear();
		
		super.updateInterval();
		
		try {
			Futures.whenAllComplete(updates).run(() -> {
			}, MoreExecutors.directExecutor()).get();
		} catch (ExecutionException | InterruptedException e) {
			LOG.severe("Unexpected exception while download team-maps -- " + e.getClass().getSimpleName() + ": "
					+ e.getMessage());
		}
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
		
		if (CAN_SHARE_MAP.get(entity).isRadioEquipped()) {
			
			//
			// Update from the Team's known-map
			//
			
			//
			// We only want to download those map-locations which we cannot directly see,
			// and for which we don't have un-uploaded updates.
			//
			short[] toDownload = CAN_SHARE_MAP.get(entity).getSeenSinceLastReported();
			
			if (CAN_SEE.has(entity)) {
				final short[] unseenRegion = CoordPacker.negatePacked(CAN_SEE.get(entity).getSeen());
				toDownload = CoordPacker.unionPacked(toDownload, unseenRegion);
			}
			
			thisMap.setMap(team, toDownload);
		}
	}
}
