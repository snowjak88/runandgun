/**
 * 
 */
package org.snowjak.runandgun.systems;

import org.snowjak.runandgun.components.HasGlyph;
import org.snowjak.runandgun.components.HasLocation;
import org.snowjak.runandgun.components.IsPOV;
import org.snowjak.runandgun.context.Context;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

/**
 * @author snowjak88
 *
 */
public class POVUpdatingSystem extends IteratingSystem {
	
	private static final ComponentMapper<HasLocation> HAS_LOCATION = ComponentMapper.getFor(HasLocation.class);
	private static final ComponentMapper<HasGlyph> HAS_GLYPH = ComponentMapper.getFor(HasGlyph.class);
	
	public POVUpdatingSystem() {
		
		super(Family.all(IsPOV.class, HasLocation.class).get());
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		if (!HAS_LOCATION.has(entity))
			return;
		
		if (HAS_GLYPH.has(entity) && HAS_GLYPH.get(entity).getGlyph() != null)
			Context.get().pov().updateFocus(HAS_GLYPH.get(entity).getGlyph());
		else
			Context.get().pov().updateFocus(HAS_LOCATION.get(entity).getCoord());
	}
}
