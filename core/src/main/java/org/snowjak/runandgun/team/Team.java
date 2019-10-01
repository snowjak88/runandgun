/**
 * 
 */
package org.snowjak.runandgun.team;

import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.events.CurrentMapChangedEvent;
import org.snowjak.runandgun.map.GlobalMap;
import org.snowjak.runandgun.map.KnownMap;

import com.badlogic.gdx.utils.Disposable;
import com.google.common.eventbus.Subscribe;

import squidpony.squidmath.GreasedRegion;

/**
 * Encapsulates data and functionality relating to teams of entities.
 * 
 * @author snowjak88
 *
 */
public class Team implements Disposable {
	
	private KnownMap map;
	private GreasedRegion visible;
	
	public Team() {
		
		Context.get().eventBus().register(this);
	}
	
	public KnownMap getMap() {
		
		if (map == null) {
			synchronized (this) {
				if (map == null) {
					final GlobalMap m = Context.get().map();
					if (m != null)
						map = new KnownMap(m.getWidth(), m.getHeight());
					else
						map = null;
				}
			}
		}
		
		return map;
	}
	
	public GreasedRegion getVisible() {
		
		if (visible == null) {
			synchronized (this) {
				if (visible == null) {
					final GlobalMap m = Context.get().map();
					if (m != null && visible == null)
						visible = new GreasedRegion(m.getWidth(), m.getHeight());
					else
						visible = null;
				}
			}
		}
		
		return visible;
	}
	
	/**
	 * Reset this Team's visibility. This should be done prior to updating via
	 * {@link #update(KnownMap, GreasedRegion)}.
	 */
	public void resetVisibility() {
		
		synchronized (this) {
			getVisible().clear();
		}
	}
	
	/**
	 * Contribute a particular {@link KnownMap} and {@link GreasedRegion
	 * visibility-region} to this Team's map.
	 * 
	 * @param map
	 * @param visible
	 *            {@code null} to leave the "currently-visible" region unchanged
	 */
	public void update(KnownMap map, GreasedRegion visible) {
		
		synchronized (this) {
			if (visible != null)
				getVisible().or(visible);
			
			getMap().updateMap(map);
		}
	}
	
	@Subscribe
	public void receiveCurrentMapChangeEvent(CurrentMapChangedEvent event) {
		
		synchronized (this) {
			final GlobalMap m = Context.get().map();
			if (m != null) {
				
				if (map == null)
					map = new KnownMap(m.getWidth(), m.getHeight());
				else
					map.resize(m.getWidth(), m.getHeight());
				
				if (visible == null)
					visible = new GreasedRegion(m.getWidth(), m.getHeight());
				else
					visible.resizeAndEmpty(m.getWidth(), m.getHeight());
				
			} else {
				map = null;
				visible = null;
			}
		}
	}
	
	@Override
	public void dispose() {
		
		Context.get().eventBus().unregister(this);
	}
}
