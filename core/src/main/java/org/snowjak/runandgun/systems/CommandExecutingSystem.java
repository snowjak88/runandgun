/**
 * 
 */
package org.snowjak.runandgun.systems;

import java.util.Optional;
import java.util.logging.Logger;

import org.snowjak.runandgun.commanders.Commander;
import org.snowjak.runandgun.commands.Command;
import org.snowjak.runandgun.components.AcceptsCommands;
import org.snowjak.runandgun.context.Context;

import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.systems.IteratingSystem;

/**
 * For all entities which {@link AcceptsCommands accept Commands}, polls the
 * associated {@link Commander} for fresh {@link Command}s.
 * 
 * @author snowjak88
 *
 */
public class CommandExecutingSystem extends IteratingSystem {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(CommandExecutingSystem.class.getName());
	
	private static final ComponentMapper<AcceptsCommands> ACCEPT_COMMANDS = ComponentMapper
			.getFor(AcceptsCommands.class);
	
	public CommandExecutingSystem() {
		
		super(Family.all(AcceptsCommands.class).get());
	}
	
	@Override
	protected void processEntity(Entity entity, float deltaTime) {
		
		//
		// Attempt to retrieve the associated Commander instance.
		//
		if (!ACCEPT_COMMANDS.has(entity))
			return;
		final AcceptsCommands ac = ACCEPT_COMMANDS.get(entity);
		
		final Commander commander = Context.get().commander(ac.getCommanderID());
		if (commander == null)
			return;
			
		//
		// If the associated Commander has a fresh command for us, execute it!
		//
		final Optional<Command> command = commander.getCommand(entity);
		
		if (command.isPresent()) {
			
			if (command.get().isEntitySpecific())
				command.get().execute(entity);
			else
				command.get().execute();
			
		}
	}
	
}
