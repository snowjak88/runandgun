/**
 * 
 */
package org.snowjak.runandgun.context;

import org.snowjak.runandgun.systems.CommandExecutingSystem;
import org.snowjak.runandgun.systems.FOVUpdatingSystem;
import org.snowjak.runandgun.systems.IsMovingUpdatingSystem;
import org.snowjak.runandgun.systems.MapLocationUpdatingSystem;
import org.snowjak.runandgun.systems.MovementListExecutingSystem;
import org.snowjak.runandgun.systems.OwnMapUpdatingSystem;
import org.snowjak.runandgun.systems.PathfindingSystem;
import org.snowjak.runandgun.systems.TeamManager;
import org.snowjak.runandgun.systems.TeamMapUpdatingSystem;
import org.snowjak.runandgun.systems.UniqueTagManager;
import org.snowjak.runandgun.systems.VisibleGlyphsUpdatingSystem;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.PooledEngine;

/**
 * @author snowjak88
 *
 */
public class EngineBuilder {
	
	public static Engine get() {
		
		final Engine engine = new PooledEngine();
		
		engine.addSystem(new CommandExecutingSystem());
		engine.addSystem(new PathfindingSystem());
		engine.addSystem(new MovementListExecutingSystem());
		engine.addSystem(new MapLocationUpdatingSystem());
		engine.addSystem(new IsMovingUpdatingSystem());
		engine.addSystem(new FOVUpdatingSystem());
		engine.addSystem(new OwnMapUpdatingSystem());
		engine.addSystem(new TeamMapUpdatingSystem());
		engine.addSystem(new VisibleGlyphsUpdatingSystem());
		engine.addSystem(new TeamManager());
		engine.addSystem(new UniqueTagManager());
		
		return engine;
	}
}
