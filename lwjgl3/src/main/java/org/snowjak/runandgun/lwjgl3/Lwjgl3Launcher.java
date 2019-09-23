package org.snowjak.runandgun.lwjgl3;

import com.badlogic.gdx.backends.lwjgl3.Lwjgl3Application;
import com.badlogic.gdx.backends.lwjgl3.Lwjgl3ApplicationConfiguration;
import org.snowjak.runandgun.App;
import org.snowjak.runandgun.config.DisplayConfiguration;

/** Launches the desktop (LWJGL3) application. */
public class Lwjgl3Launcher {
	
	public static void main(String[] args) {
		
		createApplication();
	}
	
	private static Lwjgl3Application createApplication() {
		
		return new Lwjgl3Application(new App(), getDefaultConfiguration());
	}
	
	private static Lwjgl3ApplicationConfiguration getDefaultConfiguration() {
		
		final DisplayConfiguration dc = DisplayConfiguration.getBootstrapConfiguration();
		final int windowWidth = dc.getColumns() * dc.getCellWidth();
		final int windowHeight = dc.getRows() * dc.getCellHeight();
		final int minWindowWidth = windowWidth / 2, minWindowHeight = windowHeight / 2;
		
		Lwjgl3ApplicationConfiguration configuration = new Lwjgl3ApplicationConfiguration();
		
		configuration.setTitle("runandgun");
		configuration.setWindowedMode(windowWidth, windowHeight);
		configuration.setWindowSizeLimits(minWindowWidth, minWindowHeight, -1, -1);
		configuration.setWindowIcon("libgdx128.png", "libgdx64.png", "libgdx32.png", "libgdx16.png");
		
		return configuration;
	}
}