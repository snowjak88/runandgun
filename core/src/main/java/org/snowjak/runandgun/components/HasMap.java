/**
 * 
 */
package org.snowjak.runandgun.components;

import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.map.KnownMap;
import org.snowjak.runandgun.team.Team;
import org.snowjak.runandgun.map.GlobalMap;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Indicates that an entity has {@link KnownMap a copy of the map}. Normally, an
 * entity would be associated with a {@link Team}, which has its own
 * {@link Team#getMap() map} accessible to all team-members. You'd want to use
 * this only in the case of an entity which is not part of a team and which
 * still requires persistent map-knowledge.
 * 
 * @author snowjak88
 *
 */
public class HasMap implements Component, Poolable {
	
	private KnownMap map = null;
	
	public KnownMap getMap() {
		
		if (map == null)
			synchronized (this) {
				if (map == null) {
					final GlobalMap m = Context.get().globalMap();
					if (m != null)
						map = new KnownMap(m.getWidth(), m.getHeight());
				}
			}
		
		return map;
	}
	
	@Override
	public void reset() {
		
		map.clear();
	}
	
}
