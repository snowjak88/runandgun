/**
 * 
 */
package org.snowjak.runandgun.context;

import org.snowjak.runandgun.systems.CommandExecutingSystem;
import org.snowjak.runandgun.systems.FOVUpdatingSystem;
import org.snowjak.runandgun.systems.IsMovingUpdatingSystem;
import org.snowjak.runandgun.systems.MapLocationUpdatingSystem;
import org.snowjak.runandgun.systems.MovementListExecutingSystem;
import org.snowjak.runandgun.systems.POVUpdatingSystem;
import org.snowjak.runandgun.systems.PathfindingSystem;

import com.badlogic.ashley.core.Engine;

/**
 * @author snowjak88
 *
 */
public class EngineBuilder {
	
	public static Engine get() {
		
		final Engine engine = new Engine();
		
		engine.addSystem(new CommandExecutingSystem());
		engine.addSystem(new FOVUpdatingSystem());
		engine.addSystem(new IsMovingUpdatingSystem());
		engine.addSystem(new MapLocationUpdatingSystem());
		engine.addSystem(new MovementListExecutingSystem());
		engine.addSystem(new PathfindingSystem());
		engine.addSystem(new POVUpdatingSystem());
		
		return engine;
	}
}
