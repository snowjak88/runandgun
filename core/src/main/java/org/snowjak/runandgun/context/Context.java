/**
 * 
 */
package org.snowjak.runandgun.context;

import java.util.concurrent.locks.ReentrantLock;

import org.snowjak.runandgun.config.Configuration;
import org.snowjak.runandgun.screen.AbstractScreen;

import com.badlogic.gdx.utils.Disposable;
import com.google.gson.Gson;

/**
 * Central repository for application-wide state.
 * 
 * @author snowjak88
 *
 */
public class Context implements Disposable {
	
	private static Context __INSTANCE = null;
	
	private final ReentrantLock initLock = new ReentrantLock();
	
	private Gson gson = null;
	private Configuration config = null;
	private AbstractScreen currentScreen = null;
	
	/**
	 * @return the shared Context
	 */
	public static Context get() {
		
		if (__INSTANCE == null)
			synchronized (Context.class) {
				if (__INSTANCE == null)
					__INSTANCE = new Context();
			}
		
		return __INSTANCE;
	}
	
	/**
	 * Private constructor. All access must proceed through {@link #get()}.
	 */
	private Context() {
		
	}
	
	/**
	 * @return the shared {@link Gson} instance
	 */
	public Gson gson() {
		
		if (gson == null) {
			initLock.lock();
			if (gson == null)
				gson = GsonBuilder.get();
			initLock.unlock();
		}
		
		return gson;
	}
	
	public Configuration config() {
		
		if (config == null) {
			initLock.lock();
			if (config == null)
				config = new Configuration();
			initLock.unlock();
		}
		
		return config;
	}
	
	/**
	 * @return the currently-active {@link AbstractScreen screen}
	 */
	public AbstractScreen screen() {
		
		return currentScreen;
	}
	
	/**
	 * Sets the currently-active {@link AbstractScreen screen} and activates it.
	 * Also deactivates the currently-active screen, if any.
	 * <p>
	 * This Context will handle calling {@link AbstractScreen#create() create()} and
	 * {@link AbstractScreen#dispose() dispose()} for you -- you should not be
	 * calling these methods elsewhere.
	 * </p>
	 * 
	 * @param newScreen
	 */
	public void setScreen(AbstractScreen screen) {
		
		initLock.lock();
		if (currentScreen != null)
			currentScreen.dispose();
		
		currentScreen = screen;
		
		currentScreen.create();
		initLock.unlock();
	}
	
	@Override
	public void dispose() {
		
		config().dispose();
		
		if (currentScreen != null)
			currentScreen.dispose();
	}
}
