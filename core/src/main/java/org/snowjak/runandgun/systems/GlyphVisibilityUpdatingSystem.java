/**
 * 
 */
package org.snowjak.runandgun.systems;

import org.snowjak.runandgun.components.HasGlyph;
import org.snowjak.runandgun.components.HasLocation;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.team.Team;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

import squidpony.squidgrid.gui.gdx.TextCellFactory.Glyph;
import squidpony.squidmath.Coord;
import squidpony.squidmath.CoordPacker;

/**
 * If an entity {@link HasGlyph has a glyph}, this system will control its
 * visibility. (Basically, if the active {@link Context#team()} can see it, it
 * is visible.)
 * 
 * @author snowjak88
 *
 */
public class GlyphVisibilityUpdatingSystem extends IteratingSystem {
	
	private static final ComponentMapper<HasGlyph> HAS_GLYPH = ComponentMapper.getFor(HasGlyph.class);
	
	public GlyphVisibilityUpdatingSystem() {
		
		super(Family.all(HasGlyph.class, HasLocation.class).get());
	}
	
	@Override
	public void update(float deltaTime) {
		
		if (Context.get().team() == null)
			return;
		
		super.update(deltaTime);
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		//
		//
		if (!HAS_GLYPH.has(entity))
			return;
		
		final Team team = Context.get().team();
		if (team == null)
			return;
		
		final Coord location = team.getMap().getEntityLocation(entity);
		final boolean isLocationVisible = (location == null) ? false
				: CoordPacker.queryPacked(team.getVisible(), location.x, location.y);
		final Glyph glyph = HAS_GLYPH.get(entity).getGlyph();
		
		if (glyph.isVisible() != isLocationVisible)
			glyph.setVisible(isLocationVisible);
	}
}
