/**
 * 
 */
package org.snowjak.runandgun.input;

import java.util.Iterator;
import java.util.logging.Logger;

import org.snowjak.runandgun.commands.MoveToCommand;
import org.snowjak.runandgun.components.HasMap;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.map.GlobalMap;
import org.snowjak.runandgun.screen.POV;
import org.snowjak.runandgun.systems.TeamManager;
import org.snowjak.runandgun.team.Team;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Family;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;

import squidpony.squidgrid.Direction;
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
