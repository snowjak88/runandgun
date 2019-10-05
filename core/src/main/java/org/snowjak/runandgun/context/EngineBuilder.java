/**
 * 
 */
package org.snowjak.runandgun.context;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Logger;

import org.snowjak.runandgun.systems.CommandExecutingSystem;
import org.snowjak.runandgun.systems.EntityRefManager;
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

import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.Gdx;

/**
 * @author snowjak88
 *
 */
public class EngineBuilder {
	
	private static final Logger LOG = Logger.getLogger(EngineBuilder.class.getName());
	
	public static final String PERSISTENCE_FILE_NAME = "world-persistence.json";
	
	public static PooledEngine get() {
		
		PooledEngine engine = new PooledEngine();
		
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
		engine.addSystem(new EntityRefManager());
		
		return engine;
	}
	
	/**
	 * Clear this Engine's state and recreate it from the persistence-file.
	 * 
	 * @see #PERSISTENCE_FILE_NAME
	 */
	public static void resume() throws IOException {
		
		final File persistenceFile = Gdx.files.local(PERSISTENCE_FILE_NAME).file();
		if (persistenceFile.exists())
			try (FileReader fr = new FileReader(persistenceFile)) {
				
				LOG.info("Resuming world from [" + persistenceFile.getPath() + "] ...");
				Context.get().gson().fromJson(fr, PooledEngine.class);
				
				LOG.info("Resolving references between loaded entities ...");
				Context.get().engine().getSystem(EntityRefManager.class).resolveReferences();
				
				LOG.info("Done resuming world.");
			}
	}
	
	/**
	 * Save the current state of the {@link Context#engine() shared}
	 * {@link PooledEngine} instance to the persistence-file.
	 * 
	 * @param engine
	 * @see #PERSISTENCE_FILE_NAME
	 */
	public static void persist() {
		
		final File persistenceFile = Gdx.files.local(PERSISTENCE_FILE_NAME).file();
		
		LOG.info("Persisting world to [" + persistenceFile.getPath() + "] ...");
		
		try (FileWriter fw = new FileWriter(persistenceFile)) {
			Context.get().gson().toJson(Context.get().engine(), PooledEngine.class, fw);
		} catch (IOException e) {
			LOG.severe("Could not persist world -- unexpected exception!");
			e.printStackTrace(System.err);
		}
		
		LOG.info("Done persisting world.");
	}
}
