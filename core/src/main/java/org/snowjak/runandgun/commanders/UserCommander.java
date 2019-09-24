/**
 * 
 */
package org.snowjak.runandgun.commanders;

import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.logging.Logger;

import org.snowjak.runandgun.commands.Command;

import com.badlogic.ashley.core.Entity;

/**
 * Implements a user-powered (i.e., mouse-and-keyboard) {@link Commander}.
 * 
 * @author snowjak88
 *
 */
public class UserCommander implements Commander {
	
	private static final long serialVersionUID = 7548189928616383368L;
	
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(UserCommander.class.getName());
	
	private final Queue<Command> commands = new LinkedList<>();
	
	@Override
	public int getID() {
		
		return (int) serialVersionUID;
	}
	
	/**
	 * Add this {@link Command} to the user's list of on-going commands.
	 * 
	 * @param command
	 */
	public void addCommand(Command command) {
		
		commands.add(command);
	}
	
	@Override
	public Optional<Command> getCommand(Entity entity) {
		
		return Optional.ofNullable(commands.poll());
	}
}
