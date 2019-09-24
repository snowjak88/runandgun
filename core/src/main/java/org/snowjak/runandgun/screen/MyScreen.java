/**
 * 
 */
package org.snowjak.runandgun.screen;

import java.util.logging.Logger;

import org.snowjak.runandgun.components.AcceptsCommands;
import org.snowjak.runandgun.components.CanMove;
import org.snowjak.runandgun.components.HasFOV;
import org.snowjak.runandgun.components.HasGlyph;
import org.snowjak.runandgun.components.HasLocation;
import org.snowjak.runandgun.components.IsPOV;
import org.snowjak.runandgun.config.Configuration;
import org.snowjak.runandgun.config.DisplayConfiguration;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.events.GlyphMovedEvent;
import org.snowjak.runandgun.map.Map;
import org.snowjak.runandgun.systems.PathfindingSystem;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.google.common.eventbus.Subscribe;

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
import squidpony.squidmath.GWTRNG;
import squidpony.squidmath.NumberTools;

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
	
	public final int mapWidth = 32, mapHeight = 32;
	
	private static final float FLOAT_LIGHTING = SColor.COSMIC_LATTE.toFloatBits(),
			GRAY_FLOAT = SColor.CW_GRAY_BLACK.toFloatBits();
	
	private static final GWTRNG rng = new GWTRNG(System.currentTimeMillis());
	
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
		
		dungeonGen = new DungeonGenerator(mapWidth, mapHeight, rng);
		Context.get().setMap(new Map(dungeonGen.generate(), dungeonGen.getBareDungeon()));
		
		final Coord playerPosition = Context.get().map().getFloors().singleRandom(rng);
		Context.get().pov().updateCenter(playerPosition);
		
		setBackground(SColor.CW_GRAY_BLACK);
		
		final Glyph pg = display.glyph('@', SColor.SAFETY_ORANGE, playerPosition.x, playerPosition.y);
		
		input = new SquidInput(Context.get().getLocalInput(),
				new SquidMouse(dc.getCellWidth(), dc.getCellHeight(), Context.get().getLocalInput()));
		
		Gdx.input.setInputProcessor(new InputMultiplexer(stage, input));
		
		stage.addActor(display);
		
		//
		//
		//
		
		final Engine e = Context.get().engine();
		
		e.getSystem(PathfindingSystem.class).setMap(Context.get().map());
		
		final Entity player = e.createEntity();
		player.add(new HasLocation(playerPosition.x, playerPosition.y));
		player.add(new CanMove(2.5f, false));
		player.add(new HasFOV(9));
		player.add(new AcceptsCommands(Context.get().userCommander().getID()));
		player.add(new HasGlyph(pg));
		player.add(new IsPOV());
		e.addEntity(player);
		
		Context.get().eventBus().register(this);
	}
	
	@Override
	public void resize(int width, int height) {
		
		final Configuration config = Context.get().config();
		
		float currentZoomX = (float) width / config.display().getColumns();
		float currentZoomY = (float) height / config.display().getRows();
		
		final int mouseOffsetX = (config.display().getColumns() & 1) * (int) (currentZoomX * -0.5f);
		final int mouseOffsetY = (config.display().getRows() & 1) * (int) (currentZoomY * -0.5f);
		input.getMouse().reinitialize(currentZoomX, currentZoomY, config.display().getColumns(),
				config.display().getRows(), mouseOffsetX, mouseOffsetY);
		
		stage.getViewport().update(width, height, false);
		stage.getViewport().setScreenBounds(0, 0, width, height);
	}
	
	@Override
	public void renderScreen() {
		
		final POV pov = Context.get().pov();
		stage.getCamera().position.x = display.worldX(pov.getCenter().x);
		stage.getCamera().position.y = display.worldY(pov.getCenter().y);
		
		putMap();
		
		if (input.hasNext())
			input.next();
		
		stage.act();
		stage.getViewport().apply(false);
		
		batch.setProjectionMatrix(stage.getCamera().combined);
		
		batch.begin();
		
		stage.getRoot().draw(batch, 1);
		
		batch.end();
	}
	
	@Subscribe
	public void receiveGlyphMovedEvent(GlyphMovedEvent event) {
		
		display.slide(event.getGlyph(), event.getFromX(), event.getFromY(), event.getToX(), event.getToY(), 0.1f, null);
	}
	
	public void putMap() {
		
		final DisplayConfiguration dc = Context.get().config().display();
		
		warmMildFilter.cwMul = NumberTools.swayRandomized(123456789L,
				(System.currentTimeMillis() & 0x1FFFFFL) * 0x1.2p-10f) * 1.75f;
		
		final POV pov = Context.get().pov();
		
		final int startX = Math.max(0, pov.screenToMapX(-1));
		final int startY = Math.max(0, pov.screenToMapY(-1));
		final int endX = Math.min(mapWidth, pov.screenToMapX(dc.getColumns() + 1));
		final int endY = Math.min(mapHeight, pov.screenToMapY(dc.getRows() + 1));
		
		final Map map = Context.get().map();
		
		for (int x = startX; x < endX; x++) {
			for (int y = startY; y < endY; y++) {
				
				if (pov.getCurrentlySeen().contains(x, y)) {
					
					final double lightLevel;
					if (pov.getLightLevels() != null)
						lightLevel = pov.getLightLevels()[x][y];
					else
						lightLevel = 0.01;
					
					display.putWithConsistentLight(x, y, map.getMap()[x][y], map.getColors()[x][y],
							map.getBgColors()[x][y], FLOAT_LIGHTING, lightLevel);
					
				} else if (pov.getHaveSeen().contains(x, y))
					
					display.put(x, y, map.getMap()[x][y], map.getColors()[x][y],
							SColor.lerpFloatColors(map.getBgColors()[x][y], GRAY_FLOAT, 0.45f));
			}
		}
	}
	
	@Override
	public void dispose() {
		
		Context.get().eventBus().unregister(this);
		
		stage.dispose();
	}
}
