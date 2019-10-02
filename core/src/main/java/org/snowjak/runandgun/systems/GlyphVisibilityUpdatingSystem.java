/**
 * 
 */
package org.snowjak.runandgun.systems;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.function.Consumer;
import java.util.logging.Logger;

import org.snowjak.runandgun.components.HasAppearance;
import org.snowjak.runandgun.components.HasGlyph;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.team.Team;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;
import com.badlogic.gdx.graphics.Color;
import com.google.common.util.concurrent.ListenableFuture;

import squidpony.squidgrid.gui.gdx.SColor;
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
	
	private static final Logger LOG = Logger.getLogger(GlyphVisibilityUpdatingSystem.class.getName());
	
	private static final ComponentMapper<HasAppearance> HAS_APPERANCE = ComponentMapper.getFor(HasAppearance.class);
	private static final ComponentMapper<HasGlyph> HAS_GLYPH = ComponentMapper.getFor(HasGlyph.class);
	
	private BlockingQueue<Runnable> updates = new LinkedBlockingQueue<>();
	
	public GlyphVisibilityUpdatingSystem() {
		
		super(Family.all(HasAppearance.class).get());
	}
	
	@Override
	public void update(float deltaTime) {
		
		while (!updates.isEmpty()) {
			final Runnable update = updates.poll();
			if (update != null)
				update.run();
		}
		
		if (Context.get().team() == null)
			return;
		
		super.update(deltaTime);
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		//
		//
		if (!HAS_APPERANCE.has(entity))
			return;
		
		final Team team = Context.get().team();
		if (team == null)
			return;
		
		final Coord location = team.getMap().getEntityLocation(entity);
		final boolean isLocationKnown = (location != null);
		final boolean isLocationVisible = (isLocationKnown
				&& CoordPacker.queryPacked(team.getVisible(), location.x, location.y));
		
		final HasAppearance appearance = HAS_APPERANCE.get(entity);
		
		//
		// OK -- the location is directly visible. We assume that the reported Entity
		// location here is accurate, and that its appearance should be drawn
		// completely.
		//
		if (isLocationVisible) {
			
			if (!HAS_GLYPH.has(entity) || HAS_GLYPH.get(entity).getGlyph() == null) {
				//
				// Instantiate a new Glyph on this Entity, as per its configured Appearance
				//
				
				createGlyph(appearance.getCh(), appearance.getColor(), location.x, location.y, (g) -> {
					final HasGlyph hasGlyph;
					if (HAS_GLYPH.has(entity))
						hasGlyph = HAS_GLYPH.get(entity);
					else
						hasGlyph = getEngine().createComponent(HasGlyph.class);
					hasGlyph.setGlyph(g);
					entity.add(hasGlyph);
				});
				
			} else {
				//
				// Ensure that the existing Glyph as the correct color
				//
				HAS_GLYPH.get(entity).getGlyph().setColor(appearance.getColor());
			}
			
		}
		//
		// OK -- the location is not directly visible, but represents the "last-best"
		// location for this Entity. We should draw this Entity "ghosted".
		//
		else if (isLocationKnown) {
			
			if (!HAS_GLYPH.has(entity) || HAS_GLYPH.get(entity).getGlyph() == null) {
				//
				// Instantiate a new Glyph on this Entity, as per its configured Appearance
				//
				createGlyph(appearance.getCh(), SColor.AURORA_CLOUD, location.x, location.y, (g) -> {
					final HasGlyph hasGlyph;
					if (HAS_GLYPH.has(entity))
						hasGlyph = HAS_GLYPH.get(entity);
					else
						hasGlyph = getEngine().createComponent(HasGlyph.class);
					hasGlyph.setGlyph(g);
					entity.add(hasGlyph);
				});
			} else {
				
				//
				// Ensure that this glyph is "ghosted" and locked in place
				//
				HAS_GLYPH.get(entity).getGlyph().setColor(SColor.AURORA_CLOUD);
				Context.get().glyphControl().setPosition(HAS_GLYPH.get(entity).getGlyph(), location.x, location.y);
			}
			
		}
		//
		// OK -- the location is not visible at all. We should remove this Entity's
		// glyph, if it exists.
		//
		else {
			
			if (HAS_GLYPH.has(entity)) {
				final Glyph g = HAS_GLYPH.get(entity).getGlyph();
				if (g != null) {
					Context.get().glyphControl().stop(g);
					Context.get().glyphControl().remove(g);
				}
				
				entity.remove(HasGlyph.class);
			}
			
		}
	}
	
	private void createGlyph(char ch, Color color, int x, int y, Consumer<Glyph> afterCreation) {
		
		final ListenableFuture<Glyph> createdGlyph = Context.get().glyphControl().create(ch, color, x, y);
		
		createdGlyph.addListener(() -> updates.add(() -> {
			try {
				
				final Glyph g = createdGlyph.get();
				if (afterCreation != null)
					afterCreation.accept(g);
				
			} catch (InterruptedException | ExecutionException e) {
				LOG.severe("Could not create on-screen glyph for entity -- " + e.getClass().getSimpleName() + ": "
						+ e.getMessage());
				return;
			}
		}), Context.get().executor());
	}
}
