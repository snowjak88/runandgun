/**
 * 
 */
package org.snowjak.runandgun.systems;

import java.util.function.Consumer;
import java.util.logging.Logger;

import org.snowjak.runandgun.components.HasAppearance;
import org.snowjak.runandgun.components.HasGlyph;
import org.snowjak.runandgun.concurrent.BatchedUpdates;
import org.snowjak.runandgun.concurrent.ParallelRunner;
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
import com.google.common.util.concurrent.MoreExecutors;

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
	
	private static final Color GHOST_COLOR = SColor.AURORA_CLOUD;
	
	private final BatchedUpdates batchedPreUpdate = new BatchedUpdates();
	private final BatchedUpdates batchedPostUpdate = new BatchedUpdates();
	private final ParallelRunner parallelInUpdate = new ParallelRunner();
	
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
			batchedPreUpdate.add(() -> {
				try {
					Context.get().engine().getEntitiesFor(Family.all(HasGlyph.class).get())
							.forEach(e -> removeGlyph(e));
				} catch (Throwable t) {
					LOG.severe("Cannot update an entity's glyph -- " + t.getClass().getSimpleName() + ": "
							+ t.getMessage());
					t.printStackTrace(System.err);
				}
			});
	}
	
	@Override
	public void update(float deltaTime) {
		
		batchedPreUpdate.runUpdates();
		
		final KnownMap map = Context.get().displayMap();
		if (map == null)
			return;
		
		final ImmutableArray<Entity> entities = getEngine().getEntitiesFor(Family.all(HasAppearance.class).get());
		for (int i = 0; i < entities.size(); i++) {
			
			final Entity e = entities.get(i);
			final Coord location = map.getEntityLocation(e);
			
			final boolean isVisible = (location != null) && (map.isVisible(location.x, location.y));
			final boolean isKnown = (location != null) && (map.isKnown(location.x, location.y));
			final boolean hasGlyph = HAS_GLYPH.has(e) && HAS_GLYPH.get(e).getGlyph() != null;
			
			if (isVisible)
				setVisible(e, location);
			
			else if (isKnown)
				setKnown(e, location);
			
			else if (hasGlyph)
				setLastKnown(e);
			
		}
		
		parallelInUpdate.awaitAll();
		batchedPostUpdate.runUpdates();
	}
	
	private void setVisible(Entity e, Coord location) {
		
		final HasAppearance ha = HAS_APPEARANCE.get(e);
		final HasGlyph hg;
		if (HAS_GLYPH.has(e))
			hg = HAS_GLYPH.get(e);
		else
			hg = getEngine().createComponent(HasGlyph.class);
		
		if (hg.getGlyph() == null)
			createGlyph(ha.getCh(), location, ha.getColor(), (g) -> {
				batchedPreUpdate.add(() -> {
					hg.setMoveable(true);
					hg.setGlyph(g);
					e.add(hg);
				});
			});
		else {
			batchedPostUpdate.add(() -> hg.setMoveable(true));
			batchedPreUpdate.add(() -> {
				if (hg.getGlyph() != null)
					hg.getGlyph().setColor(ha.getColor());
			});
		}
		;
	}
	
	private void setKnown(Entity e, Coord location) {
		
		final HasAppearance ha = HAS_APPEARANCE.get(e);
		final HasGlyph hg;
		if (HAS_GLYPH.has(e))
			hg = HAS_GLYPH.get(e);
		else
			hg = getEngine().createComponent(HasGlyph.class);
		
		if (hg.getGlyph() == null)
			createGlyph(ha.getCh(), location, GHOST_COLOR, (g) -> {
				batchedPreUpdate.add(() -> {
					hg.setMoveable(false);
					hg.setGlyph(g);
					e.add(hg);
				});
			});
		else {
			batchedPostUpdate.add(() -> hg.setMoveable(false));
			batchedPreUpdate.add(() -> {
				if (hg.getGlyph() != null)
					hg.getGlyph().setColor(GHOST_COLOR);
			});
		}
	}
	
	private void setLastKnown(Entity e) {
		
		if (!HAS_GLYPH.has(e))
			return;
		final HasGlyph hg = HAS_GLYPH.get(e);
		
		if (hg.getGlyph() == null)
			return;
		
		batchedPostUpdate.add(() -> hg.setMoveable(false));
		batchedPreUpdate.add(() -> {
			if (hg.getGlyph() != null)
				hg.getGlyph().setColor(GHOST_COLOR);
		});
	}
	
	private void createGlyph(char ch, Coord location, Color color, Consumer<Glyph> consumer) {
		
		final ListenableFuture<Glyph> futureGlyph = Context.get().glyphControl().create(ch, color, location.x,
				location.y);
		futureGlyph.addListener(() -> {
			try {
				consumer.accept(futureGlyph.get());
			} catch (Throwable t) {
				LOG.severe("Cannot create new Glyph -- " + t.getClass().getSimpleName() + ": " + t.getMessage());
				t.printStackTrace(System.err);
			}
		}, MoreExecutors.directExecutor());
	}
	
	private void removeGlyph(Entity e) {
		
		if (!HAS_GLYPH.has(e))
			return;
		final HasGlyph hg = HAS_GLYPH.get(e);
		
		if (hg.getGlyph() != null)
			Context.get().glyphControl().remove(hg.getGlyph());
		
		batchedPostUpdate.add(() -> {
			try {
				e.remove(HasGlyph.class);
			} catch (Throwable t) {
				LOG.severe(
						"Cannot remove glyph from entity -- " + t.getClass().getSimpleName() + ": " + t.getMessage());
				t.printStackTrace(System.err);
			}
		});
	}
}
