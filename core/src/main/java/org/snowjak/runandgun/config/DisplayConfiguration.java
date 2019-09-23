/**
 * 
 */
package org.snowjak.runandgun.config;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

import org.snowjak.runandgun.context.Context;

/**
 * Holds configuration-items relating to the game's display.
 * 
 * @author snowjak88
 *
 */
public class DisplayConfiguration {
	
	private static final Logger LOG = Logger.getLogger(DisplayConfiguration.class.getName());
	
	/**
	 * This config's JSON-file will have this name.
	 */
	public static final String CONFIG_FILENAME = "display.json";
	
	private int rows = 24;
	private int columns = 80;
	
	private int cellWidth = 16;
	private int cellHeight = 16;
	
	public final Fonts fonts = new Fonts();
	
	public static class Fonts {
		
		/**
		 * All "world" characters should be rendered in this font. ("World" characters
		 * are subject to scaling via {@link DisplayConfiguration#getCellWidth()
		 * cell-width} and {@link DisplayConfiguration#getCellHeight() -height}.)
		 */
		public final FontConfiguration world = new FontConfiguration("Inconsolata-LGC-Square-distance.fnt",
				"Inconsolata-LGC-Square-distance.png", null);
		
		/**
		 * All non-"world" characters should be rendered in this font. Typically, this
		 * is used to render text that need not be square or monospaced --
		 * communications, announcements, etc. -- and that are not part of the game
		 * world.
		 */
		public final FontConfiguration text = new FontConfiguration("Gentium-distance.fnt", "Gentium-distance.png",
				null);
	}
	
	/**
	 * When the game first boots, we need to provide an initial
	 * display-configuration simply to bootstrap the application-window. This method
	 * attempts to load the configuration-file and, if it's not found, simply goes
	 * with the hardcoded defaults.
	 * 
	 * @return
	 */
	public static DisplayConfiguration getBootstrapConfiguration() {
		
		final File configDirectory = new File(Configuration.CONFIG_FOLDER);
		if (!configDirectory.exists() || !configDirectory.isDirectory()) {
			LOG.info("Bootstrapping: No config directory exists, using hardcoded defaults.");
			return new DisplayConfiguration();
		}
		
		final File configFile = new File(configDirectory, DisplayConfiguration.CONFIG_FILENAME);
		if (!configFile.exists() || !configFile.isFile()) {
			LOG.info("Bootstrapping: No config file exists, using hardcoded defaults.");
			return new DisplayConfiguration();
		}
		
		try (FileReader fr = new FileReader(configFile)) {
			LOG.info("Bootstrapping: Loading from config-file.");
			return Context.get().gson().fromJson(fr, DisplayConfiguration.class);
		} catch (IOException e) {
			LOG.info("Bootstrapping: Could not load config-file, using hardcoded defaults.");
			return new DisplayConfiguration();
		}
	}
	
	public int getRows() {
		
		return rows;
	}
	
	public void setRows(int rows) {
		
		this.rows = rows;
	}
	
	public int getColumns() {
		
		return columns;
	}
	
	public void setColumns(int columns) {
		
		this.columns = columns;
	}
	
	public int getCellWidth() {
		
		return cellWidth;
	}
	
	public void setCellWidth(int cellWidth) {
		
		this.cellWidth = cellWidth;
	}
	
	public int getCellHeight() {
		
		return cellHeight;
	}
	
	public void setCellHeight(int cellHeight) {
		
		this.cellHeight = cellHeight;
	}
	
}
