/**
 * 
 */
package org.snowjak.runandgun.systems;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;

/**
 * A system which allows you to associate individual entities with tags, for
 * easy referral to important entities.
 * <p>
 * Note that, while any tag may refer to only one entity at a time, an entity
 * may be referred-to by multiple tags.
 * </p>
 * 
 * @author snowjak88
 *
 */
public class UniqueTagManager extends EntitySystem {
	
	private final Map<String, Entity> tagToEntity = Collections.synchronizedMap(new HashMap<>());
	
	/**
	 * @return the set of active tags
	 */
	public Set<String> getTags() {
		
		return tagToEntity.keySet();
	}
	
	/**
	 * Associate the given Entity with the given tag.
	 * 
	 * @param tag
	 * @param entity
	 */
	public void set(String tag, Entity entity) {
		
		tagToEntity.put(tag, entity);
	}
	
	/**
	 * Remove the association for the given tag from this manager.
	 * 
	 * @param tag
	 */
	public void unset(String tag) {
		
		tagToEntity.remove(tag);
	}
	
	/**
	 * @param tag
	 * @return {@code true} if this manager contains the given tag
	 */
	public boolean has(String tag) {
		
		return tagToEntity.containsKey(tag);
	}
	
	/**
	 * Get the {@link Entity} associated with the given tag, or {@code null} if no
	 * such association has been created.
	 * 
	 * @param tag
	 * @return
	 */
	public Entity get(String tag) {
		
		return tagToEntity.get(tag);
	}
}
