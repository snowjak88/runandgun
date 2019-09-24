/**
 * 
 */
package org.snowjak.runandgun.input;

import java.util.logging.Logger;

import org.snowjak.runandgun.commands.MoveToCommand;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.screen.POV;
import org.snowjak.runandgun.screen.MyScreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;

import squidpony.squidgrid.gui.gdx.SquidInput;
import squidpony.squidmath.Coord;

/**
 * Receives input from the local user (i.e., mouse-and-keyboard).
 * 
 * @author snowjak88
 *
 */
public class LocalInput extends InputAdapter implements SquidInput.KeyHandler {
	
	private static final Logger LOG = Logger.getLogger(LocalInput.class.getName());
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		
		final POV pov = Context.get().pov();
		final Coord mapPoint = pov.screenToMap(Coord.get(screenX, screenY));
		final MyScreen ms = (MyScreen) Context.get().screen();
		
		LOG.info("touchUp( " + screenX + ", " + screenY + ", " + pointer + ", " + button + " ) --> map[" + mapPoint.x
				+ ", " + mapPoint.y + "]");
		
		if (mapPoint.x < 0 || mapPoint.y < 0 || mapPoint.x >= ms.mapWidth || mapPoint.y >= ms.mapHeight)
			return false;
		
		Context.get().userCommander().addCommand(new MoveToCommand(mapPoint));
		
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
		}
	}
}
