/**
 * 
 */
package org.snowjak.runandgun.team;

import java.util.Collection;
import java.util.logging.Logger;

import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.events.CurrentMapChangedEvent;
import org.snowjak.runandgun.map.GlobalMap;
import org.snowjak.runandgun.map.KnownMap;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.utils.Disposable;
import com.google.common.eventbus.Subscribe;

import squidpony.squidmath.CoordPacker;

/**
 * Encapsulates data and functionality relating to teams of entities.
 * 
 * @author snowjak88
 *
 */
public class Team implements Disposable {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(Team.class.getName());
	
	private KnownMap map;
	
	public Team() {
		
		Context.get().eventBus().register(this);
	}
	
	public KnownMap getMap() {
		
		if (map == null) {
			synchronized (this) {
				if (map == null) {
					final GlobalMap m = Context.get().globalMap();
					if (m != null)
						map = new KnownMap(m.getWidth(), m.getHeight());
					else
						map = null;
				}
			}
		}
		
		return map;
	}
	
	/**
	 * Contribute a particular {@link GlobalMap} and
	 * {@link CoordPacker#packSeveral(java.util.Collection) packed}
	 * visibility-region to this Team's map.
	 * 
	 * @param map
	 * @param visible
	 *            {@code null} to leave the "currently-visible" region unchanged
	 * @param addedEntities
	 *            a Collection which will be populated with all {@link Entity}s that
	 *            now lie within this {@link Team}'s FOV, or {@code null} if no such
	 *            results needed
	 * @param movedEntities
	 *            a Collection which will be populated with all {@link Entity}s that
	 *            have changed positions within this {@link Team}'s FOV, or
	 *            {@code null} if no such results needed
	 * @param removedEntities
	 *            a Collection which will be populated with all {@link Entity}s that
	 *            no longer lie within this {@link Team}'s FOV, or {@code null} if
	 *            no such results needed
	 */
	public void update(GlobalMap map, short[] visible, Collection<Entity> addedEntities,
			Collection<Entity> movedEntities, Collection<Entity> removedEntities) {
		
		synchronized (this) {
			
			getMap().insertMap(map, visible, visible);
			getMap().updateEntities(map, visible, addedEntities, movedEntities, removedEntities);
		}
	}
	
	public void resize(int width, int height) {
		
		if (width == map.getWidth() && height == map.getHeight())
			return;
		
		map.resize(width, height);
	}
	
	public void reset() {
		
		final GlobalMap m = Context.get().globalMap();
		if (m == null)
			map = null;
		else
			resize(m.getWidth(), m.getHeight());
	}
	
	@Subscribe
	public void receiveCurrentMapChangeEvent(CurrentMapChangedEvent event) {
		
		synchronized (this) {
			final GlobalMap m = Context.get().globalMap();
			if (m != null) {
				
				if (map == null)
					map = new KnownMap(m.getWidth(), m.getHeight());
				else
					map.resize(m.getWidth(), m.getHeight());
				
			} else
				map = null;
		}
	}
	
	@Override
	public void dispose() {
		
		Context.get().eventBus().unregister(this);
	}
	
}
