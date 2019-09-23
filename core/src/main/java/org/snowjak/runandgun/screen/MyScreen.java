/**
 * 
 */
package org.snowjak.runandgun.screen;

import java.util.ArrayList;

import org.snowjak.runandgun.config.Configuration;
import org.snowjak.runandgun.config.DisplayConfiguration;
import org.snowjak.runandgun.context.Context;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import squidpony.ArrayTools;
import squidpony.squidai.DijkstraMap;
import squidpony.squidgrid.Direction;
import squidpony.squidgrid.FOV;
import squidpony.squidgrid.Measurement;
import squidpony.squidgrid.Radius;
import squidpony.squidgrid.gui.gdx.FilterBatch;
import squidpony.squidgrid.gui.gdx.FloatFilters;
import squidpony.squidgrid.gui.gdx.FloatFilters.YCwCmFilter;
import squidpony.squidgrid.gui.gdx.MapUtility;
import squidpony.squidgrid.gui.gdx.PanelEffect;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SparseLayers;
import squidpony.squidgrid.gui.gdx.SquidInput;
import squidpony.squidgrid.gui.gdx.SquidMouse;
import squidpony.squidgrid.gui.gdx.TextCellFactory.Glyph;
import squidpony.squidgrid.mapping.DungeonGenerator;
import squidpony.squidgrid.mapping.DungeonUtility;
import squidpony.squidgrid.mapping.LineKit;
import squidpony.squidmath.Coord;
import squidpony.squidmath.GWTRNG;
import squidpony.squidmath.GreasedRegion;
import squidpony.squidmath.NumberTools;

public class MyScreen extends AbstractScreen {
	
	private FilterBatch batch;
	private YCwCmFilter warmMildFilter;
	private Viewport mainViewport;
	private Stage stage;
	private SparseLayers display;
	
	private SquidInput input;
	
	private Glyph pg;
	
	private DungeonGenerator dungeonGen;
	private char[][] decoDungeon, bareDungeon, lineDungeon, prunedDungeon;
	private double[][] resistance, visible;
	private Coord cursor, player;
	private GreasedRegion floors, blockage, seen, currentlySeen;
	private float[][] colors, bgColors;
	
	private ArrayList<Coord> toCursor, awaitedMoves;
	private DijkstraMap playerToCursor;
	
	private final int mapWidth = 32, mapHeight = 32;
	
	private static final float FLOAT_LIGHTING = SColor.COSMIC_LATTE.toFloatBits(),
			GRAY_FLOAT = SColor.CW_GRAY_BLACK.toFloatBits();
	
	private static final GWTRNG rng = new GWTRNG(System.currentTimeMillis());
	
	@Override
	public void create() {
		
		final DisplayConfiguration displayConfig = Context.get().config().display();
		
		warmMildFilter = new FloatFilters.YCwCmFilter(0.875f, 0.6f, 0.6f);
		batch = new FilterBatch(warmMildFilter);
		
		mainViewport = new StretchViewport(displayConfig.getColumns() * displayConfig.getCellWidth(),
				displayConfig.getRows() * displayConfig.getCellHeight());
		
		mainViewport.setScreenBounds(0, 0, displayConfig.getColumns() * displayConfig.getCellWidth(),
				displayConfig.getRows() * displayConfig.getCellHeight());
		
		stage = new Stage(mainViewport, batch);
		
		display = new SparseLayers(mapWidth, mapHeight, displayConfig.getCellWidth(), displayConfig.getCellHeight(),
				displayConfig.fonts.world.getTextCellFactory());
		
		display.setPosition(0f, 0f);
		
		dungeonGen = new DungeonGenerator(mapWidth, mapHeight, rng);
		decoDungeon = dungeonGen.generate();
		bareDungeon = dungeonGen.getBareDungeon();
		lineDungeon = DungeonUtility.hashesToLines(decoDungeon);
		resistance = DungeonUtility.generateResistances(decoDungeon);
		visible = new double[mapWidth][mapHeight];
		
		cursor = Coord.get(-1, -1);
		
		floors = new GreasedRegion(bareDungeon, '.');
		player = floors.singleRandom(rng);
		
		FOV.reuseFOV(resistance, visible, player.x, player.y, 9.0, Radius.CIRCLE);
		blockage = new GreasedRegion(visible, 0.0);
		seen = blockage.not().copy();
		currentlySeen = seen.copy();
		blockage.fringe8way();
		
		prunedDungeon = ArrayTools.copy(lineDungeon);
		LineKit.pruneLines(lineDungeon, seen, LineKit.lightAlt, prunedDungeon);
		
		toCursor = new ArrayList<>(200);
		awaitedMoves = new ArrayList<>(200);
		playerToCursor = new DijkstraMap(bareDungeon, Measurement.EUCLIDEAN);
		playerToCursor.setGoal(player);
		playerToCursor.setGoal(player);
		playerToCursor.partialScan(13, blockage);
		
		// setBackground(SColor.DARK_SLATE_GRAY);
		setBackground(SColor.DB_GRAPHITE);
		SColor.LIMITED_PALETTE[3] = SColor.DB_GRAPHITE;
		colors = MapUtility.generateDefaultColorsFloat(decoDungeon);
		bgColors = MapUtility.generateDefaultBGColorsFloat(decoDungeon);
		
		pg = display.glyph('@', SColor.SAFETY_ORANGE, player.x, player.y);
		
		input = new SquidInput(new SquidInput.KeyHandler() {
			
			@Override
			public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
				
				switch (key) {
				case SquidInput.UP_ARROW:
				case 'w':
				case 'W': {
					toCursor.clear();
					// -1 is up on the screen
					awaitedMoves.add(player.translate(0, -1));
					break;
				}
				case SquidInput.DOWN_ARROW:
				case 's':
				case 'S': {
					toCursor.clear();
					// +1 is down on the screen
					awaitedMoves.add(player.translate(0, 1));
					break;
				}
				case SquidInput.LEFT_ARROW:
				case 'a':
				case 'A': {
					toCursor.clear();
					awaitedMoves.add(player.translate(-1, 0));
					break;
				}
				case SquidInput.RIGHT_ARROW:
				case 'd':
				case 'D': {
					toCursor.clear();
					awaitedMoves.add(player.translate(1, 0));
					break;
				}
				case 'Q':
				case 'q':
				case SquidInput.ESCAPE: {
					Gdx.app.exit();
					break;
				}
				case 'c':
				case 'C': {
					seen.fill(true);
					break;
				}
				}
			}
		}, new SquidMouse(displayConfig.getCellWidth(), displayConfig.getCellHeight(), displayConfig.getColumns(),
				displayConfig.getRows(), 0, 0, new InputAdapter() {
					
					@Override
					public boolean touchUp(int screenX, int screenY, int pointer, int button) {
						
						screenX += player.x - (displayConfig.getColumns() / 2);
						screenY += player.y - (displayConfig.getRows() / 2);
						
						if (screenX < 0 || screenY < 0 || screenX >= mapWidth || screenY >= mapHeight)
							return false;
						if (awaitedMoves.isEmpty()) {
							
							if (toCursor.isEmpty()) {
								cursor = Coord.get(screenX, screenY);
								toCursor.clear();
								playerToCursor.findPathPreScanned(toCursor, cursor);
								if (!toCursor.isEmpty())
									toCursor.remove(0);
							}
							
							awaitedMoves.addAll(toCursor);
						}
						return true;
					}
					
					@Override
					public boolean touchDragged(int screenX, int screenY, int pointer) {
						
						return mouseMoved(screenX, screenY);
					}
					
					@Override
					public boolean mouseMoved(int screenX, int screenY) {
						
						if (!awaitedMoves.isEmpty())
							return false;
						
						screenX += player.x - (displayConfig.getColumns() / 2);
						screenY += player.y - (displayConfig.getRows() / 2);
						
						if (screenX < 0 || screenY < 0 || screenX >= mapWidth || screenY >= mapHeight
								|| (cursor.x == screenX && cursor.y == screenY)) {
							return false;
						}
						cursor = Coord.get(screenX, screenY);
						toCursor.clear();
						playerToCursor.findPathPreScanned(toCursor, cursor);
						if (!toCursor.isEmpty())
							toCursor.remove(0);
						return false;
					}
				}));
		
		Gdx.input.setInputProcessor(new InputMultiplexer(stage, input));
		
		stage.addActor(display);
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
		
		stage.getCamera().position.x = pg.getX();
		stage.getCamera().position.y = pg.getY();
		
		putMap();
		
		if (!awaitedMoves.isEmpty()) {
			
			if (!display.hasActiveAnimations()) {
				
				Coord m = awaitedMoves.remove(0);
				if (!toCursor.isEmpty())
					toCursor.remove(0);
				
				move(m.x - player.x, m.y - player.y);
				
				if (awaitedMoves.isEmpty()) {
					
					playerToCursor.clearGoals();
					playerToCursor.resetMap();
					
					playerToCursor.setGoal(player);
					
					playerToCursor.partialScan(13, blockage);
				}
			}
		} else if (input.hasNext())
			input.next();
		
		stage.act();
		stage.getViewport().apply(false);
		
		batch.setProjectionMatrix(stage.getCamera().combined);
		
		batch.begin();
		
		stage.getRoot().draw(batch, 1);
		
		batch.end();
	}
	
	private void move(final int xmod, final int ymod) {
		
		final Configuration config = Context.get().config();
		
		int newX = player.x + xmod, newY = player.y + ymod;
		
		if (newX >= 0 && newY >= 0 && newX < mapWidth && newY < mapWidth && bareDungeon[newX][newY] != '#') {
			
			display.slide(pg, player.x, player.y, newX, newY, 0.12f, null);
			
			player = player.translate(xmod, ymod);
			
			FOV.reuseFOV(resistance, visible, player.x, player.y, 9.0, Radius.CIRCLE);
			
			blockage.refill(visible, 0.0);
			seen.or(currentlySeen.remake(blockage.not()));
			blockage.fringe8way();
			
			LineKit.pruneLines(lineDungeon, seen, LineKit.lightAlt, prunedDungeon);
		} else {
			
			display.bump(pg, Direction.getRoughDirection(xmod, ymod), 0.25f);
			
			display.addAction(new PanelEffect.PulseEffect(display, 1f, currentlySeen, player, 3,
					new float[] { SColor.CW_FADED_PURPLE.toFloatBits() }));
			
			final int hitWallX = player.x + xmod, hitWallY = player.y + ymod;
			display.recolor(0f, hitWallX, hitWallY, 0, SColor.DB_BLOOD.toFloatBits(), 0.4f, new Runnable() {
				
				@Override
				public void run() {
					
					colors[hitWallX][hitWallY] = SColor.DB_BLOOD.toFloatBits();
				}
			});
		}
	}
	
	public void putMap() {
		
		final Configuration config = Context.get().config();
		
		warmMildFilter.cwMul = NumberTools.swayRandomized(123456789L,
				(System.currentTimeMillis() & 0x1FFFFFL) * 0x1.2p-10f) * 1.75f;
		
		for (int x = Math.max(0, player.x - (config.display().getColumns() / 2) - 1), i = 0; x < mapWidth
				&& i < config.display().getColumns() + 2; x++, i++) {
			for (int y = Math.max(0, player.y - (config.display().getRows() / 2) - 1), j = 0; y < mapHeight
					&& j < config.display().getRows() + 2; y++, j++) {
				
				if (visible[x][y] > 0.0) {
					
					display.putWithConsistentLight(x, y, prunedDungeon[x][y], colors[x][y], bgColors[x][y],
							FLOAT_LIGHTING, visible[x][y]);
					
				} else if (seen.contains(x, y))
					
					display.put(x, y, prunedDungeon[x][y], colors[x][y],
							SColor.lerpFloatColors(bgColors[x][y], GRAY_FLOAT, 0.45f));
			}
		}
		
		Coord pt;
		for (int i = 0; i < toCursor.size(); i++) {
			pt = toCursor.get(i);
			display.put(pt.x, pt.y, SColor.lightenFloat(bgColors[pt.x][pt.y], 0.85f));
		}
	}
	
	@Override
	public void dispose() {
		
		stage.dispose();
	}
}
