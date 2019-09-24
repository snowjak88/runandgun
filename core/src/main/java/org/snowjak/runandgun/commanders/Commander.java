/**
 * 
 */
package org.snowjak.runandgun.commanders;

import java.util.Optional;

import org.snowjak.runandgun.commands.Command;

import com.badlogic.ashley.core.Entity;

/**
 * A Commander is an object which is capable of generating {@link Command}s for
 * entities.
 * <p>
 * Commanders fall under 2 main types:
 * <ul>
 * <li><strong>user-powered</strong> -- the user generates commands using mouse
 * and keyboard</li>
 * <li><strong>AI-powered</strong> -- an AI generates commands using a script,
 * FSM, decision-tree, or some other methodology</li>
 * </ul>
 * </p>
 * 
 * @author snowjak88
 *
 */
public interface Commander {
	
	/**
	 * Every Commander has an ID by which it can be distinguished, and which allows
	 * an entity to identify which Commander it should receive Commands from.
	 * 
	 * @return
	 */
	public int getID();
	
	/**
	 * Get the most-appropriate Command to execute for the given entity.
	 * 
	 * @return
	 */
	public Optional<Command> getCommand(Entity entity);
}
