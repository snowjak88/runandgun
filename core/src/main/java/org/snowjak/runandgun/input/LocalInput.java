/**
 * 
 */
package org.snowjak.runandgun.input;

import java.util.logging.Logger;

import org.snowjak.runandgun.commands.MoveToCommand;
import org.snowjak.runandgun.components.CanMove;
import org.snowjak.runandgun.components.CanSee;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.map.Map;
import org.snowjak.runandgun.screen.POV;
import org.snowjak.runandgun.systems.UniqueTagManager;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
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
	
	private static final ComponentMapper<CanSee> CAN_SEE = ComponentMapper.getFor(CanSee.class);
	
	@Override
	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
		
		final POV pov = Context.get().pov();
		final Coord mapPoint = pov.screenToMap(Coord.get(screenX, screenY));
		final Map m = Context.get().map();
		
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
//		case ' ': {
//			final UniqueTagManager tagManager = Context.get().engine().getSystem(UniqueTagManager.class);
//			if (tagManager != null) {
//				final ImmutableArray<Entity> entities = Context.get().engine()
//						.getEntitiesFor(Family.all(CanMove.class, CanSee.class).get());
//				final Entity entity = entities.get(Context.get().rng().nextInt(entities.size()));
//				tagManager.set(POV.POV_ENTITY_TAG, entity);
//			}
//			break;
//		}
		case 'c':
		case 'C': {
			Context.get().engine().getEntitiesFor(Family.all(CanSee.class).get()).forEach(e -> {
				CAN_SEE.get(e).forget();
			});
			break;
		}
		}
	}
}
