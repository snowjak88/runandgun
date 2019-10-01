/**
 * 
 */
package org.snowjak.runandgun.team;

import java.util.logging.Logger;

import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.events.CurrentMapChangedEvent;
import org.snowjak.runandgun.map.GlobalMap;
import org.snowjak.runandgun.map.KnownMap;

import com.badlogic.gdx.utils.Disposable;
import com.google.common.eventbus.Subscribe;

import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.GreasedRegion;

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
	private short[] visible = CoordPacker.ALL_WALL;
	
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
	
	public short[] getVisible() {
		
		return visible;
	}
	
	public GreasedRegion getVisibleRegion() {
		
		synchronized (this) {
			return CoordPacker.unpackGreasedRegion(visible, map.getWidth(), map.getHeight());
		}
	}
	
	/**
	 * Reset this Team's visibility. This should be done prior to updating via
	 * {@link #update(KnownMap, GreasedRegion)}.
	 */
	public void resetVisibility() {
		
		synchronized (this) {
			visible = CoordPacker.ALL_WALL;
		}
	}
	
	/**
	 * Contribute a particular {@link KnownMap} and
	 * {@link CoordPacker#packSeveral(java.util.Collection) packed}
	 * visibility-region to this Team's map.
	 * 
	 * @param map
	 * @param visible
	 *            {@code null} to leave the "currently-visible" region unchanged
	 */
	public void update(KnownMap map, short[] visible) {
		
		synchronized (this) {
			if (visible != null)
				this.visible = CoordPacker.unionPacked(this.visible, visible);
			
			getMap().insertMap(map, null, true);
			getMap().setLastSynchronizedTimestamp(Context.get().clock().getTimestamp());
		}
	}
	
	/**
	 * Contribute a particular {@link KnownMap} and
	 * {@link CoordPacker#packSeveral(java.util.Collection) packed}
	 * visibility-region to this Team's map, but selecting only the given
	 * {@code updateOnly} region (to limit the scope of the actual update).
	 * 
	 * @param map
	 * @param visible
	 *            current-visibility region to add to this Team's
	 *            current-visibility, or {@code null} if none to add
	 * @param updateOnly
	 *            region to restrict our updates from {@code map} and
	 *            {@code visible}, or {@code null} if no restriction
	 */
	public void update(KnownMap map, short[] visible, short[] updateOnly) {
		
		synchronized (this) {
			final short[] update = (updateOnly == null) ? CoordPacker.ALL_ON : updateOnly;
			
			if (visible != null)
				this.visible = CoordPacker.unionPacked(this.visible, CoordPacker.intersectPacked(visible, update));
			
			getMap().insertMap(map, update, true);
			
			getMap().setLastSynchronizedTimestamp(Context.get().clock().getTimestamp());
		}
	}
	
	public void resize(int width, int height) {
		
		if (width == map.getWidth() && height != map.getHeight())
			return;
		
		map.resize(width, height);
		visible = CoordPacker.ALL_WALL;
	}
	
	public void reset() {
		
		final GlobalMap m = Context.get().map();
		if (m == null)
			map = null;
		else
			resize(m.getWidth(), m.getHeight());
		
		visible = CoordPacker.ALL_WALL;
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
				
			} else
				map = null;
			
			visible = CoordPacker.ALL_WALL;
		}
	}
	
	@Override
	public void dispose() {
		
		Context.get().eventBus().unregister(this);
	}
	
}
