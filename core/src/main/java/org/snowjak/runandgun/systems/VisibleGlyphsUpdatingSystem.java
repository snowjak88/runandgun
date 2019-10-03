/**
 * 
 */
package org.snowjak.runandgun.systems;

import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;

import org.snowjak.runandgun.components.HasAppearance;
import org.snowjak.runandgun.components.HasGlyph;
import org.snowjak.runandgun.components.HasLocation;
import org.snowjak.runandgun.concurrent.BatchedUpdates;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.events.CurrentTeamChangedEvent;
import org.snowjak.runandgun.map.KnownMap;
import org.snowjak.runandgun.team.Team;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.Color;
import com.google.common.eventbus.Subscribe;
import com.google.common.util.concurrent.ListenableFuture;

import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.TextCellFactory.Glyph;
import squidpony.squidmath.Coord;

/**
 * Responsible for ensuring that all {@link Entity Entities} that are visible to
 * the {@link Context#team() currently-active} {@link Team} are drawn using
 * {@link Glyph}s.
 * 
 * @author snowjak88
 *
 */
public class VisibleGlyphsUpdatingSystem extends EntitySystem {
	
	private static final Logger LOG = Logger.getLogger(VisibleGlyphsUpdatingSystem.class.getName());
	
	private static final ComponentMapper<HasGlyph> HAS_GLYPH = ComponentMapper.getFor(HasGlyph.class);
	private static final ComponentMapper<HasAppearance> HAS_APPEARANCE = ComponentMapper.getFor(HasAppearance.class);
	
	private final BatchedUpdates updates = new BatchedUpdates();
	
	public VisibleGlyphsUpdatingSystem() {
		
	}
	
	@Override
	public void addedToEngine(Engine engine) {
		
		Context.get().eventBus().register(this);
		setProcessing(Context.get().team() != null);
	}
	
	@Override
	public void removedFromEngine(Engine engine) {
		
		Context.get().eventBus().unregister(this);
	}
	
	@Subscribe
	public void receiveTeamChangeEvent(CurrentTeamChangedEvent event) {
		
		setProcessing(Context.get().team() != null);
		
		if (checkProcessing())
			//
			// If the current Team has changed, then potentially all of our HasGlyph
			// assignments are incorrect. They all need to be removed and reconstituted.
			//
			updates.add(() -> {
				LOG.info("Changing Teams -- removing all Gylphs ..");
				Context.get().engine().getEntitiesFor(Family.all(HasGlyph.class).get()).forEach(e -> {
					Context.get().glyphControl().remove(HAS_GLYPH.get(e).getGlyph());
					e.remove(HasGlyph.class);
				});
			});
	}
	
	@Override
	public void update(float deltaTime) {
		
		updates.runUpdates();
		
		final KnownMap map = Context.get().displayMap();
		if (map == null)
			return;
		
		final ImmutableArray<Entity> entities = getEngine()
				.getEntitiesFor(Family.all(HasAppearance.class, HasLocation.class).get());
		for (int i = 0; i < entities.size(); i++) {
			
			final Entity e = entities.get(i);
			final Coord location = map.getEntityLocation(e);
			if (location == null)
				continue;
			
			if (!HAS_APPEARANCE.has(e))
				continue;
			final HasAppearance ha = HAS_APPEARANCE.get(e);
			
			if (map.isVisible(location.x, location.y))
				setGlyph(e, ha.getCh(), location, ha.getColor(), true);
			else if (map.isKnown(location.x, location.y))
				setGlyph(e, ha.getCh(), location, SColor.AURORA_CLOUD, false);
			else
				removeGlyph(e);
			
		}
	}
	
	private void setGlyph(Entity entity, char ch, Coord location, Color color, boolean isMoveable) {
		
		final HasGlyph hg;
		if (!HAS_GLYPH.has(entity)) {
			hg = getEngine().createComponent(HasGlyph.class);
		} else
			hg = HAS_GLYPH.get(entity);
		
		if (hg.getGlyph() == null) {
			//
			// If we have to create a new Glyph, that necessitates a future.
			// (The Glyph has to be created on the rendering thread, which means we'll
			// get it *next* frame.)
			//
			final ListenableFuture<Glyph> futureGlyph = Context.get().glyphControl().create(ch, color, location.x,
					location.y);
			
			updates.add(() -> {
				try {
					hg.setGlyph(futureGlyph.get());
					hg.setMoveable(isMoveable);
					entity.add(hg);
					
					if (!isMoveable)
						Context.get().glyphControl().stop(hg.getGlyph());
				} catch (ExecutionException | InterruptedException exception) {
					LOG.severe("Cannot create glyph for entity -- " + exception.getClass().getSimpleName() + ": "
							+ exception.getMessage());
				}
			});
			
		} else {
			
			//
			// The Glyph already exists, so we'll just move it to the correct location and
			// update its color.
			//
			
			if (hg.getGlyph().hasActions() && !isMoveable) {
				Context.get().glyphControl().setPosition(hg.getGlyph(), location.x, location.y);
				Context.get().glyphControl().stop(hg.getGlyph());
			}
			
			hg.getGlyph().setColor(color);
			hg.setMoveable(isMoveable);
			
		}
	}
	
	private void removeGlyph(Entity e) {
		
		if (!HAS_GLYPH.has(e))
			return;
		final HasGlyph hg = HAS_GLYPH.get(e);
		
		if (hg.getGlyph() != null)
			Context.get().glyphControl().remove(hg.getGlyph());
		
		e.remove(HasGlyph.class);
	}
}
