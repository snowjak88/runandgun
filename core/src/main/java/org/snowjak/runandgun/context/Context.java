/**
 * 
 */
package org.snowjak.runandgun.context;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;

import org.snowjak.runandgun.clock.ClockControl;
import org.snowjak.runandgun.commanders.Commander;
import org.snowjak.runandgun.commanders.UserCommander;
import org.snowjak.runandgun.config.Configuration;
import org.snowjak.runandgun.events.CurrentMapChangedEvent;
import org.snowjak.runandgun.events.CurrentTeamChangedEvent;
import org.snowjak.runandgun.events.NewScreenActivatedEvent;
import org.snowjak.runandgun.input.LocalInput;
import org.snowjak.runandgun.map.GlobalMap;
import org.snowjak.runandgun.map.KnownMap;
import org.snowjak.runandgun.screen.AbstractScreen;
import org.snowjak.runandgun.screen.DecorationProvider;
import org.snowjak.runandgun.screen.GlyphControl;
import org.snowjak.runandgun.screen.POV;
import org.snowjak.runandgun.systems.TeamManager;
import org.snowjak.runandgun.team.Team;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.badlogic.gdx.utils.Disposable;
import com.google.common.eventbus.EventBus;
import com.google.common.util.concurrent.ListeningExecutorService;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.gson.Gson;

import squidpony.squidgrid.gui.gdx.TextCellFactory.Glyph;
import squidpony.squidmath.GWTRNG;
import squidpony.squidmath.IRNG;

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
	private POV pov = null;
	private PooledEngine engine = null;
	private IRNG rng = null;
	
	private AbstractScreen currentScreen = null;
	private GlyphControl glyphMovement = null;
	private DecorationProvider decorationProvider = null;
	private GlobalMap map = null;
	private Team team = null;
	private KnownMap currentMap = null;
	
	private final ListeningExecutorService executor = MoreExecutors.listeningDecorator(Executors.newCachedThreadPool());
	private final EventBus eventBus = new EventBus();
	private final ClockControl clockControl = new ClockControl();
	
	private final UserCommander userCommander = new UserCommander();
	private final Map<Integer, Commander> commanderRegistry = new HashMap<>();
	{
		commanderRegistry.put(userCommander.getID(), userCommander);
	}
	
	private LocalInput localInput = null;
	
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
	 * @return the shared {@link ListeningExecutorService} instance
	 */
	public ListeningExecutorService executor() {
		
		return executor;
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
	 * <p>
	 * Note that, because {@link AbstractScreen} implements a number of interfaces,
	 * this also enables {@link #glyphControl()} and {@link #decorationProvider()}.
	 * </p>
	 * 
	 * @param newScreen
	 */
	public void setScreen(AbstractScreen screen) {
		
		initLock.lock();
		if (currentScreen != null)
			currentScreen.dispose();
		
		currentScreen = screen;
		glyphMovement = screen;
		decorationProvider = screen;
		
		currentScreen.create();
		initLock.unlock();
		
		eventBus().post(new NewScreenActivatedEvent());
	}
	
	/**
	 * @return {@link GlyphControl an interface} allowing you to initiate
	 *         {@link Glyph} movements
	 */
	public GlyphControl glyphControl() {
		
		return glyphMovement;
	}
	
	/**
	 * @return {@link DecorationProvider an interface} allowing you to draw on the
	 *         currently-active {@link AbstractScreen screen}
	 */
	public DecorationProvider decorationProvider() {
		
		return decorationProvider;
	}
	
	/**
	 * @return the current {@link GlobalMap}
	 */
	public GlobalMap globalMap() {
		
		return map;
	}
	
	/**
	 * Update the current {@link GlobalMap}. Fires a {@link CurrentMapChangedEvent}
	 */
	public void setGlobalMap(GlobalMap map) {
		
		initLock.lock();
		this.map = map;
		initLock.unlock();
		
		eventBus().post(new CurrentMapChangedEvent());
	}
	
	/**
	 * @return the current {@link Team}
	 */
	public Team team() {
		
		return team;
	}
	
	/**
	 * Update the currently-focused {@link Team}. Fires a
	 * {@link CurrentTeamChangedEvent}.
	 * 
	 * @param team
	 */
	public void setTeam(Team team) {
		
		initLock.lock();
		this.team = team;
		initLock.unlock();
		
		eventBus().post(new CurrentTeamChangedEvent());
	}
	
	/**
	 * @return the {@link KnownMap} representing the last-updated {@link Team} map
	 */
	public KnownMap displayMap() {
		
		return currentMap;
	}
	
	/**
	 * Set the {@link KnownMap} representing the last-updated {@link Team} map
	 * 
	 * @param map
	 */
	public void setDisplayMap(KnownMap map) {
		
		initLock.lock();
		currentMap = map;
		initLock.unlock();
	}
	
	/**
	 * @return the shared {@link POV} instance
	 */
	public POV pov() {
		
		if (pov == null) {
			initLock.lock();
			if (pov == null)
				pov = new POV();
			initLock.unlock();
		}
		return pov;
	}
	
	/**
	 * @return the shared {@link ClockControl} instance
	 */
	public ClockControl clock() {
		
		return clockControl;
	}
	
	/**
	 * @return the shared {@link EventBus} instance
	 */
	public EventBus eventBus() {
		
		return eventBus;
	}
	
	/**
	 * @return the shared {@link Engine} instance
	 */
	public PooledEngine engine() {
		
		if (engine == null) {
			initLock.lock();
			if (engine == null) {
				engine = EngineBuilder.get();
				
				try {
					EngineBuilder.resume();
				} catch (IOException e) {
					e.printStackTrace(System.err);
				}
				
				if (engine.getEntities().size() < 1)
					EngineBuilder.populateDefaultEntities();
				
				final Team playerTeam = engine.getSystem(TeamManager.class).getTeam("player");
				setTeam(playerTeam);
				setDisplayMap(playerTeam.getMap().copy());
			}
			initLock.unlock();
		}
		return engine;
	}
	
	public IRNG rng() {
		
		if (rng == null) {
			initLock.lock();
			if (rng == null)
				rng = new GWTRNG(config().rules().getSeed());
			initLock.unlock();
		}
		return rng;
	}
	
	/**
	 * Get a registered {@link Commander} by its ID, or {@code null} if that
	 * Commander is not registered.
	 * 
	 * @param id
	 * @return
	 */
	public Commander commander(int id) {
		
		return commanderRegistry.get(id);
	}
	
	/**
	 * Register the given {@link Commander}, so {@link Entity}s can refer to it via
	 * {@link #commander(int)}.
	 * <p>
	 * Note that {@link #userCommander()} is already registered at start-up.
	 * </p>
	 * 
	 * @param commander
	 */
	public void register(Commander commander) {
		
		commanderRegistry.put(commander.getID(), commander);
	}
	
	public void unregisterCommander(Commander commander) {
		
		unregisterCommander(commander.getID());
	}
	
	public void unregisterCommander(int id) {
		
		commanderRegistry.remove(id);
	}
	
	/**
	 * @return the shared {@link UserCommander} instance
	 */
	public UserCommander userCommander() {
		
		return userCommander;
	}
	
	/**
	 * @return the shared {@link LocalInput} instance
	 */
	public LocalInput getLocalInput() {
		
		if (localInput == null) {
			initLock.lock();
			if (localInput == null)
				localInput = new LocalInput();
			initLock.unlock();
		}
		
		return localInput;
	}
	
	@Override
	public void dispose() {
		
		EngineBuilder.persist();
		
		config().dispose();
		executor().shutdownNow();
		
		if (currentScreen != null)
			currentScreen.dispose();
	}
}
