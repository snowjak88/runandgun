/**
 * 
 */
package org.snowjak.runandgun.components;

import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.map.KnownMap;
import org.snowjak.runandgun.map.GlobalMap;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Indicates that an entity has {@link KnownMap a copy of the map}.
 * 
 * @author snowjak88
 *
 */
public class HasMap implements Component, Poolable {
	
	private KnownMap map;
	
	public void init() {
		
		final GlobalMap m = Context.get().globalMap();
		if (m != null)
			setSize(m.getWidth(), m.getHeight());
	}
	
	public void setSize(int width, int height) {
		
		if (this.map == null)
			this.map = new KnownMap(width, height);
		else
			this.map.resize(width, height);
	}
	
	public KnownMap getMap() {
		
		return map;
	}
	
	@Override
	public void reset() {
		
		map.clear();
	}
	
}
