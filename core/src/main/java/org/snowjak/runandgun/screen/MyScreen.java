/**
 * 
 */
package org.snowjak.runandgun.screen;

import java.util.logging.Logger;

import org.snowjak.runandgun.commanders.Commander;
import org.snowjak.runandgun.commanders.SimpleFleeingCommander;
import org.snowjak.runandgun.commanders.SimpleWanderingCommander;
import org.snowjak.runandgun.components.AcceptsCommands;
import org.snowjak.runandgun.components.CanMove;
import org.snowjak.runandgun.components.CanSee;
import org.snowjak.runandgun.components.HasAppearance;
import org.snowjak.runandgun.components.HasLocation;
import org.snowjak.runandgun.config.Configuration;
import org.snowjak.runandgun.config.DisplayConfiguration;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.map.GlobalMap;
import org.snowjak.runandgun.map.KnownMap;
import org.snowjak.runandgun.systems.TeamManager;
import org.snowjak.runandgun.systems.UniqueTagManager;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.SettableFuture;

import squidpony.squidgrid.gui.gdx.FilterBatch;
import squidpony.squidgrid.gui.gdx.FloatFilters;
import squidpony.squidgrid.gui.gdx.FloatFilters.YCwCmFilter;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SparseLayers;
import squidpony.squidgrid.gui.gdx.SquidInput;
import squidpony.squidgrid.gui.gdx.SquidMouse;
import squidpony.squidgrid.gui.gdx.TextCellFactory.Glyph;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidmath.Coord;

public class MyScreen extends AbstractScreen {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(MyScreen.class.getName());
	
	private FilterBatch batch;
	private YCwCmFilter warmMildFilter;
	private Viewport mainViewport;
	private Stage stage;
	private SparseLayers display;
	
	private SquidInput input;
	
	private DungeonGenerator dungeonGen;
	
	public final int mapWidth = 64, mapHeight = 64;
	
	private static final float FLOAT_LIGHTING = SColor.COSMIC_LATTE.toFloatBits(),
			GRAY_FLOAT = SColor.CW_GRAY_BLACK.toFloatBits();
	
	@Override
	public void create() {
		
		final DisplayConfiguration dc = Context.get().config().display();
		
		warmMildFilter = new FloatFilters.YCwCmFilter(0.875f, 0.6f, 0.6f);
		batch = new FilterBatch(warmMildFilter);
		
		mainViewport = new StretchViewport(dc.getColumns() * dc.getCellWidth(), dc.getRows() * dc.getCellHeight());
		
		mainViewport.setScreenBounds(0, 0, dc.getColumns() * dc.getCellWidth(), dc.getRows() * dc.getCellHeight());
		
		stage = new Stage(mainViewport, batch);
		
		display = new SparseLayers(mapWidth, mapHeight, dc.getCellWidth(), dc.getCellHeight(),
				dc.fonts.world.getTextCellFactory());
		
		display.setPosition(0f, 0f);
		
		dungeonGen = new DungeonGenerator(mapWidth, mapHeight, Context.get().rng());
		dungeonGen.addDoors(15, false).addGrass(33).addWater(10);
		Context.get().setGlobalMap(new GlobalMap(dungeonGen.generate(), dungeonGen.getBareDungeon()));
		
		setBackground(SColor.CW_GRAY_BLACK);
		
		input = new SquidInput(Context.get().getLocalInput(),
				new SquidMouse(dc.getCellWidth(), dc.getCellHeight(), Context.get().getLocalInput()));
		
		Gdx.input.setInputProcessor(new InputMultiplexer(stage, input));
		
		stage.addActor(display);
		
		//
		//
		//
		
		final Engine e = Context.get().engine();
		
		final Commander ai = new SimpleWanderingCommander();
		Context.get().register(ai);
		
		for (int i = 0; i < 32; i++) {
			final Entity wanderer = e.createEntity();
			final Coord position = Context.get().globalMap().getNonObstructing().singleRandom(Context.get().rng());
			
			final HasLocation hl = e.createComponent(HasLocation.class);
			hl.set(position);
			wanderer.add(hl);
			
			final CanMove cm = e.createComponent(CanMove.class);
			cm.init();
			cm.setSpeed(2);
			cm.setIgnoresTerrain(false);
			wanderer.add(cm);
			
			final CanSee cs = e.createComponent(CanSee.class);
			cs.init();
			cs.setDistance(9);
			wanderer.add(cs);
			
			final AcceptsCommands ac = e.createComponent(AcceptsCommands.class);
			ac.setCommanderID(ai.getID());
			wanderer.add(ac);
			
			final HasAppearance ha = e.createComponent(HasAppearance.class);
			ha.setCh('&');
			ha.setColor((i % 2 == 0) ? SColor.ELECTRIC_PURPLE : SColor.AURORA_ABSINTHE);
			wanderer.add(ha);
			
			e.addEntity(wanderer);
			e.getSystem(TeamManager.class).add((i % 2 == 0) ? "wanderers-1" : "wanderers-2", wanderer);
		}
		
		final Coord playerPosition = Context.get().globalMap().getNonObstructing().singleRandom(Context.get().rng());
		
		final Entity player = e.createEntity();
		
		final HasLocation hl = e.createComponent(HasLocation.class);
		hl.set(playerPosition);
		player.add(hl);
		
		final CanMove cm = e.createComponent(CanMove.class);
		cm.init();
		cm.setSpeed(4);
		player.add(cm);
		
		final CanSee cs = e.createComponent(CanSee.class);
		cs.init();
		cs.setDistance(9);
		player.add(cs);
		
		final AcceptsCommands ac = e.createComponent(AcceptsCommands.class);
		ac.setCommanderID(Context.get().userCommander().getID());
		player.add(ac);
		
		final HasAppearance ha = e.createComponent(HasAppearance.class);
		ha.setCh('@');
		ha.setColor(SColor.SAFETY_ORANGE);
		player.add(ha);
		
		e.addEntity(player);
		
		final TeamManager tm = e.getSystem(TeamManager.class);
		tm.add("player", player);
		
		Context.get().setTeam(tm.getTeam("player"));
		Context.get().pov().updateFocus(playerPosition);
		Context.get().engine().getSystem(UniqueTagManager.class).set(SimpleFleeingCommander.FLEE_FROM_TAG, player);
		
		Context.get().eventBus().register(this);
	}
	
	@Override
	public void resize(int width, int height) {
		
		final Configuration config = Context.get().config();
		
		final float newCellWidth = (float) width / config.display().getColumns();
		final float newCellHeight = (float) height / config.display().getRows();
		
		final int mouseOffsetX = (config.display().getColumns() & 1) * (int) (newCellWidth * -0.5f);
		final int mouseOffsetY = (config.display().getRows() & 1) * (int) (newCellHeight * -0.5f);
		
		input.getMouse().reinitialize(newCellWidth, newCellHeight, config.display().getColumns(),
				config.display().getRows(), mouseOffsetX, mouseOffsetY);
		
		stage.getViewport().update(width, height, false);
		stage.getViewport().setScreenBounds(0, 0, width, height);
		
		Context.get().config().display().setCellWidth((int) newCellWidth);
		Context.get().config().display().setCellHeight((int) newCellHeight);
	}
	
	@Override
	public void renderScreen() {
		
		final POV pov = Context.get().pov();
		
		pov.update(Gdx.graphics.getDeltaTime());
		
		stage.getCamera().position.x = getWorldX(pov.getCenterX());
		stage.getCamera().position.y = getWorldY(pov.getCenterY());
		
		putMap();
		
		if (input.hasNext())
			input.next();
		
		stage.act();
		stage.getViewport().apply(false);
		
		batch.setProjectionMatrix(stage.getCamera().combined);
		
		batch.begin();
		
		stage.getRoot().draw(batch, 1);
		
		batch.end();
		
		Gdx.graphics.setTitle("FPS: " + Gdx.graphics.getFramesPerSecond());
	}
	
	public void putMap() {
		
		LOG.entering(MyScreen.class.getName(), "putMap()");
		
		final DisplayConfiguration dc = Context.get().config().display();
		
		final POV pov = Context.get().pov();
		final GlobalMap map = Context.get().globalMap();
		
		final int startX = Math.max(0, pov.screenToMapX(-1));
		final int startY = Math.max(0, pov.screenToMapY(-1));
		final int endX = Math.min(map.getWidth(), pov.screenToMapX(dc.getColumns() + 1));
		final int endY = Math.min(map.getHeight(), pov.screenToMapY(dc.getRows() + 1));
		
		// LOG.info("Rendering map from [" + startX + "," + startY + "] to [" + endX +
		// "," + endY + "]");
		
		final KnownMap known = Context.get().displayMap();
		
		if (known == null) {
			LOG.severe("No current map!");
			return;
		}
		
		for (int x = startX; x < endX; x++) {
			for (int y = startY; y < endY; y++) {
				
				final char mapCh;
				final float mapColor, mapBGColor;
				
				if (known != null) {
					
					mapCh = known.getMapAt(x, y);
					
					final Color knownColor = known.getColorAt(x, y), knownBGColor = known.getBGColorAt(x, y);
					if (knownColor == null)
						mapColor = map.getColorAt(x, y).toFloatBits();
					else
						mapColor = knownColor.toFloatBits();
					
					if (knownBGColor == null)
						mapBGColor = map.getBGColorAt(x, y).toFloatBits();
					else
						mapBGColor = knownBGColor.toFloatBits();
					
				} else {
					
					mapCh = map.getMap()[x][y];
					
					mapColor = map.getColorAt(x, y).toFloatBits();
					mapBGColor = map.getBGColorAt(x, y).toFloatBits();
				}
				
				if (known.isVisible(x, y)) {
					
					display.putWithConsistentLight(x, y, mapCh, mapColor, mapBGColor, FLOAT_LIGHTING, 1.0);
					
				} else if (known == null || known.isKnown(x, y)) {
					
					display.put(x, y, mapCh, SColor.lerpFloatColors(mapColor, GRAY_FLOAT, 0.45f),
							SColor.lerpFloatColors(mapBGColor, GRAY_FLOAT, 0.45f));
				} else {
					
					// display.clear(x, y);
					display.put(x, y, '?', GRAY_FLOAT, getBackground().toFloatBits());
					
				}
			}
		}
		
		LOG.exiting(MyScreen.class.getName(), "putMap()");
	}
	
	@Override
	public int getWidth() {
		
		return stage.getViewport().getScreenWidth();
	}
	
	@Override
	public int getHeight() {
		
		return stage.getViewport().getScreenHeight();
	}
	
	@Override
	public float getWorldX(float gridX) {
		
		// return display.worldX(gridX);
		return display.getX() + gridX * display.font.actualCellWidth;
	}
	
	@Override
	public float getWorldY(float gridY) {
		
		// return display.worldY(gridY);
		return display.getY() + (display.gridHeight - gridY) * display.font.actualCellHeight;
	}
	
	@Override
	public float getGridX(float worldX) {
		
		// return display.gridX(worldX);
		return (worldX - display.getX()) / display.font.actualCellWidth;
	}
	
	@Override
	public float getGridY(float worldY) {
		
		// return display.gridY(worldY);
		return (display.getY() - worldY) / display.font.actualCellHeight + (float) display.gridHeight;
	}
	
	@Override
	public ListenableFuture<Glyph> create(char ch, Color color, int x, int y) {
		
		final SettableFuture<Glyph> result = SettableFuture.create();
		Gdx.app.postRunnable(() -> result.set(display.glyph(ch, color, x, y)));
		return result;
	}
	
	@Override
	public void remove(Glyph glyph) {
		
		Gdx.app.postRunnable(() -> {
			display.removeGlyph(glyph);
			glyph.remove();
		});
	}
	
	@Override
	public void move(Glyph glyph, int fromX, int fromY, int toX, int toY, float duration) {
		
		Gdx.app.postRunnable(() -> display.slide(glyph, fromX, fromY, toX, toY, duration, null));
	}
	
	@Override
	public void setPosition(Glyph glyph, int x, int y) {
		
		Gdx.app.postRunnable(() -> {
			glyph.clearActions();
			glyph.setOrigin(display.worldX(x), display.worldY(y));
		});
	}
	
	@Override
	public void stop(Glyph glyph) {
		
		Gdx.app.postRunnable(() -> glyph.clearActions());
	}
	
	@Override
	public void dispose() {
		
		Context.get().eventBus().unregister(this);
		
		stage.dispose();
	}
}
