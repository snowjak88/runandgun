/**
 * 
 */
package org.snowjak.runandgun.config;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Logger;

import org.snowjak.runandgun.context.Context;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Disposable;
import com.google.gson.JsonIOException;
import com.google.gson.JsonParseException;

/**
 * Top-level configuration repository.
 * 
 * @author snowjak88
 *
 */
public class Configuration implements Disposable {
	
	private static final Logger LOG = Logger.getLogger(Configuration.class.getName());
	
	/**
	 * All config-files will be stored in the given folder.
	 */
	public static final String CONFIG_FOLDER = "config";
	
	private DisplayConfiguration display = null;
	
	private final ReentrantLock lock = new ReentrantLock();
	
	public DisplayConfiguration display() {
		
		if (display == null) {
			lock.lock();
			if (display == null)
				display = loadExternalConfiguration(DisplayConfiguration.class, DisplayConfiguration.CONFIG_FILENAME);
			lock.unlock();
		}
		
		return display;
	}
	
	private synchronized <T> T loadExternalConfiguration(Class<T> clazz, String filename) {
		
		final String filePath = CONFIG_FOLDER + File.separator + filename;
		
		//
		// Attempt to load from the local file-provider.
		//
		try {
			final File configFile = Gdx.files.local(filePath).file();
			try (FileReader fr = new FileReader(configFile)) {
				
				return Context.get().gson().fromJson(fr, clazz);
				
			} catch (JsonIOException e) {
				LOG.severe("Cannot load configuration-file [" + filename + "] -- cannot read file: "
						+ e.getClass().getSimpleName() + ": " + e.getMessage());
			} catch (JsonParseException e) {
				LOG.severe("Cannot load configuration-file [" + filename + "] -- file cannot be parsed: "
						+ e.getClass().getSimpleName() + ": " + e.getMessage());
			}
		} catch (Throwable t) {
			
			LOG.info(
					"Cannot load configuration-file [" + filename + "], attempting to fall-back to internal asset ...");
			
		}
		
		//
		// Attempt to load from the internal/local file-provider.
		//
		try {
			final File configFile = Gdx.files.local(filePath).file();
			try (FileReader fr = new FileReader(configFile)) {
				
				return Context.get().gson().fromJson(fr, clazz);
				
			}
		} catch (Throwable t) {
			throw new RuntimeException(
					"Cannot load configuration-file [" + filename + "], as even the fallback asset failed.", t);
		}
	}
	
	@Override
	public void dispose() {
		
		//
		// Create the config directory, if it doesn't exist.
		//
		Gdx.files.local(CONFIG_FOLDER).mkdirs();
		
		//
		// Persist the DisplayConfiguration.
		//
		
		final File displayFile = Gdx.files.local(CONFIG_FOLDER + File.separator + DisplayConfiguration.CONFIG_FILENAME)
				.file();
		try (FileWriter fw = new FileWriter(displayFile)) {
			
			Context.get().gson().toJson(display, fw);
			
		} catch (JsonIOException e) {
			LOG.severe("Cannot persist display configuration -- unexpected exception while writing JSON ["
					+ e.getClass().getSimpleName() + "]: " + e.getMessage());
		} catch (IOException e) {
			LOG.severe("Cannot persist display configuration -- unexpected I/O exception ["
					+ e.getClass().getSimpleName() + "]: " + e.getMessage());
		}
	}
}
