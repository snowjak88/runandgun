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
import org.snowjak.runandgun.components.CanShareMap;
import org.snowjak.runandgun.components.HasGlyph;
import org.snowjak.runandgun.components.HasLocation;
import org.snowjak.runandgun.components.HasMap;
import org.snowjak.runandgun.config.Configuration;
import org.snowjak.runandgun.config.DisplayConfiguration;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.events.GlyphMoveStartEvent;
import org.snowjak.runandgun.map.GlobalMap;
import org.snowjak.runandgun.map.KnownMap;
import org.snowjak.runandgun.systems.PathfindingSystem;
import org.snowjak.runandgun.systems.TeamManager;
import org.snowjak.runandgun.systems.UniqueTagManager;
import org.snowjak.runandgun.team.Team;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
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
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GWTRNG;
import squidpony.squidmath.GreasedRegion;

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
		dungeonGen.addDoors(15, false).addGrass(33).addWater(10);
		Context.get().setMap(new GlobalMap(dungeonGen.generate(), dungeonGen.getBareDungeon()));
		
		setBackground(SColor.CW_GRAY_BLACK);
		
		input = new SquidInput(Context.get().getLocalInput(),
				new SquidMouse(dc.getCellWidth(), dc.getCellHeight(), Context.get().getLocalInput()));
		
		Gdx.input.setInputProcessor(new InputMultiplexer(stage, input));
		
		stage.addActor(display);
		
		//
		//
		//
		
		final Engine e = Context.get().engine();
		
		e.getSystem(PathfindingSystem.class).setMap(Context.get().map());
		
		final Commander aiCommander = new SimpleWanderingCommander();
		Context.get().register(aiCommander);
		
		for (int i = 0; i < 16; i++) {
			final Entity wanderer = e.createEntity();
			final Coord position = Context.get().map().getNonObstructing().singleRandom(Context.get().rng());
			
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
			cs.setDistance(5);
			wanderer.add(cs);
			
			final HasMap hm = e.createComponent(HasMap.class);
			hm.init();
			wanderer.add(hm);
			
			final CanShareMap csm = e.createComponent(CanShareMap.class);
			csm.setRadioEquipped(i % 2 == 0);
			// csm.setRadioEquipped(true);
			csm.setRadius(32);
			wanderer.add(csm);
			
			final AcceptsCommands ac = e.createComponent(AcceptsCommands.class);
			ac.setCommanderID(aiCommander.getID());
			wanderer.add(ac);
			
			final HasGlyph hg = e.createComponent(HasGlyph.class);
			hg.setGlyph(display.glyph('&', SColor.ELECTRIC_PURPLE, position.x, position.y));
			wanderer.add(hg);
			
			e.addEntity(wanderer);
			e.getSystem(TeamManager.class).add("wanderers", wanderer);
		}
		
		final Coord playerPosition = Context.get().map().getNonObstructing().singleRandom(rng);
		
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
		
		final HasMap hm = e.createComponent(HasMap.class);
		hm.init();
		player.add(hm);
		
		final CanShareMap csm = e.createComponent(CanShareMap.class);
		csm.setRadioEquipped(true);
		csm.setRadius(5);
		player.add(csm);
		
		final AcceptsCommands ac = e.createComponent(AcceptsCommands.class);
		ac.setCommanderID(Context.get().userCommander().getID());
		player.add(ac);
		
		final HasGlyph hg = e.createComponent(HasGlyph.class);
		hg.setGlyph(display.glyph('@', SColor.SAFETY_ORANGE, playerPosition.x, playerPosition.y));
		player.add(hg);
		e.addEntity(player);
		
		e.getSystem(TeamManager.class).add("player", player);
		
		Context.get().setTeam(e.getSystem(TeamManager.class).getTeam("wanderers"));
		Context.get().pov().updateFocus(Coord.get(mapWidth / 2, mapHeight / 2));
		// Context.get().engine().getSystem(UniqueTagManager.class).set(POV.POV_ENTITY_TAG,
		// player);
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
	
	@Subscribe
	public void receiveGlyphMovedEvent(GlyphMoveStartEvent event) {
		
		display.slide(event.getGlyph(), event.getFromX(), event.getFromY(), event.getToX(), event.getToY(),
				event.getDuration(), null);
	}
	
	public void putMap() {
		
		LOG.entering(MyScreen.class.getName(), "putMap()");
		
		final DisplayConfiguration dc = Context.get().config().display();
		
		final POV pov = Context.get().pov();
		final GlobalMap map = Context.get().map();
		
		final int startX = Math.max(0, pov.screenToMapX(-1));
		final int startY = Math.max(0, pov.screenToMapY(-1));
		final int endX = Math.min(map.getWidth(), pov.screenToMapX(dc.getColumns() + 1));
		final int endY = Math.min(map.getHeight(), pov.screenToMapY(dc.getRows() + 1));
		
		// LOG.info("Rendering map from [" + startX + "," + startY + "] to [" + endX +
		// "," + endY + "]");
		
		// final CanSee fov = pov.getFOV();
		// final KnownMap known = pov.getMap();
		final Team team = Context.get().team();
		
		final GreasedRegion visible;
		final KnownMap known;
		
		if (team == null) {
			// LOG.info("No assigned team -- using POV ...");
			visible = pov.getFOV().getSeenRegion();
			known = pov.getMap();
		} else {
			// LOG.info("Assigned team -- using Team ...");
			known = team.getMap();
			visible = team.getVisibleRegion();
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
				
				if (visible.contains(x, y)) {
					
					// LOG.info("[" + x + "," + y + "] is visible ...");
					// final double lightLevel = pov.getFOV().getLightLevel(x, y);
					display.putWithConsistentLight(x, y, mapCh, mapColor, mapBGColor, FLOAT_LIGHTING, 1.0);
					// display.putWithConsistentLight(x, y, mapCh, mapColor, mapBGColor,
					// FLOAT_LIGHTING, lightLevel);
					
				} else if (known == null || known.isKnown(x, y)) {
					
					// LOG.info("[" + x + "," + y + "] is known ...");
					display.put(x, y, mapCh, mapColor, SColor.lerpFloatColors(mapBGColor, GRAY_FLOAT, 0.75f));
				} else {
					
					// LOG.info("[" + x + "," + y + "] is unknown ...");
					// display.clear(x, y);
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
	public void dispose() {
		
		Context.get().eventBus().unregister(this);
		
		stage.dispose();
	}
}
