/**
 * 
 */
package org.snowjak.runandgun.input;

import java.util.Iterator;
import java.util.logging.Logger;

import org.snowjak.runandgun.commands.MoveToCommand;
import org.snowjak.runandgun.components.HasLocation;
import org.snowjak.runandgun.components.HasMap;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.events.NewScreenActivatedEvent;
import org.snowjak.runandgun.map.GlobalMap;
import org.snowjak.runandgun.screen.Decoration;
import org.snowjak.runandgun.screen.POV;
import org.snowjak.runandgun.systems.TeamManager;
import org.snowjak.runandgun.team.Team;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.graphics.Color;
import com.google.common.eventbus.Subscribe;

import squidpony.squidgrid.Direction;
import squidpony.squidgrid.gui.gdx.SColor;
import squidpony.squidgrid.gui.gdx.SquidInput;
import squidpony.squidmath.Coord;

/**
 * Receives input from the local user (i.e., mouse-and-keyboard).
 * 
 * @author snowjak88
 *
 */
public class LocalInput extends InputAdapter implements SquidInput.KeyHandler {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(LocalInput.class.getName());
	
	private static final ComponentMapper<HasMap> HAS_MAP = ComponentMapper.getFor(HasMap.class);
	
	private Iterator<Team> teamIterator;
	
	private Coord cursor = Coord.get(0, 0);
	
	private static final Color MOVE_LINE_COLOR = SColor.colorFromFloat(SColor.translucentColor(SColor.IVORY, 0.5f));
	private static final Decoration MOVE_LINE_DECORATION = new Decoration((dp) -> {
		
		try {
			final Team playerTeam = Context.get().engine().getSystem(TeamManager.class).getTeam("player");
			if (playerTeam == null)
				return;
			
			final Coord playerPosition = Context.get().engine().getSystem(TeamManager.class).getEntities(playerTeam)
					.iterator().next().getComponent(HasLocation.class).get();
			
			dp.line(dp.map(dp.cursor()), playerPosition, MOVE_LINE_COLOR);
		} catch (Throwable t) {
			t.printStackTrace(System.err);
		}
	});
	
	public LocalInput() {
		
		super();
		
		if (Context.get().screen() != null)
			Context.get().screen().addDecoration(MOVE_LINE_DECORATION);
		
		Context.get().eventBus().register(this);
	}
	
	@Subscribe
	public void receiveScreenChangeEvent(NewScreenActivatedEvent event) {
		
		Context.get().screen().addDecoration(MOVE_LINE_DECORATION);
	}
	
	/**
	 * @return the last-known mouse-cursor position, in cells (expressed in
	 *         screen-coordinates)
	 */
	public Coord getCursor() {
		
		return cursor;
	}
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		
		final POV pov = Context.get().pov();
		final Coord mapPoint = pov.screenToMap(Coord.get(screenX, screenY));
		final GlobalMap m = Context.get().globalMap();
		
		if (mapPoint.x < 0 || mapPoint.y < 0 || mapPoint.x >= m.getWidth() || mapPoint.y >= m.getHeight())
			return false;
		
		Context.get().userCommander().addCommand(new MoveToCommand(mapPoint));
		
		return true;
	}
	
	@Override
	public boolean mouseMoved(int screenX, int screenY) {
		
		cursor = Coord.get(screenX, screenY);
		
		final int mouseBufferSizeX = Context.get().config().input().mouse().getScrollZoneX();
		final int mouseBufferSizeY = Context.get().config().input().mouse().getScrollZoneY();
		final int screenWidth = Context.get().config().display().getColumns();
		final int screenHeight = Context.get().config().display().getRows();
		
		boolean shiftUp = false, shiftDown = false, shiftLeft = false, shiftRight = false;
		
		if (screenX < mouseBufferSizeX)
			shiftLeft = true;
		if (screenY < mouseBufferSizeY)
			shiftUp = true;
		if (screenX >= (screenWidth - mouseBufferSizeX))
			shiftRight = true;
		if (screenY >= (screenHeight - mouseBufferSizeY))
			shiftDown = true;
		
		if (!(shiftLeft || shiftRight || shiftUp || shiftDown)) {
			Context.get().pov().shift(null);
			return true;
		}
		
		final Direction d;
		if (shiftUp && shiftLeft)
			d = Direction.UP_LEFT;
		else if (shiftUp && shiftRight)
			d = Direction.UP_RIGHT;
		else if (shiftUp)
			d = Direction.UP;
		else if (shiftDown && shiftLeft)
			d = Direction.DOWN_LEFT;
		else if (shiftDown && shiftRight)
			d = Direction.DOWN_RIGHT;
		else if (shiftDown)
			d = Direction.DOWN;
		else if (shiftLeft)
			d = Direction.LEFT;
		else
			d = Direction.RIGHT;
		
		Context.get().pov().shift(d);
		
		return true;
	}
	
	@Override
	public void handle(char key, boolean alt, boolean ctrl, boolean shift) {
		
		switch (key) {
		case 'Q':
		case 'q':
		case SquidInput.ESCAPE: {
			Gdx.app.exit();
			break;
		}
		case 'c':
		case 'C': {
			Context.get().engine().getEntitiesFor(Family.all(HasMap.class).get()).forEach(e -> {
				HAS_MAP.get(e).getMap().clear();
			});
			Context.get().team().getMap().clear();
			break;
		}
		case 't':
		case 'T': {
			if (teamIterator == null || !teamIterator.hasNext())
				teamIterator = Context.get().engine().getSystem(TeamManager.class).getTeams().iterator();
			Context.get().setTeam(teamIterator.next());
			break;
		}
		case ' ': {
			Context.get().clock().togglePaused();
			break;
		}
		}
	}
}
