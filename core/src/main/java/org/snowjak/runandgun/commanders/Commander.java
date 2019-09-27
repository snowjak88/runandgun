/**
 * 
 */
package org.snowjak.runandgun.commanders;

import org.snowjak.runandgun.commands.Command;
import org.snowjak.runandgun.components.AcceptsCommands;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.Family;

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
public abstract class Commander {
	
	private final Family family;
	private final int id;
	
	/**
	 * Construct a new Commander with the given ID and Family.
	 * 
	 * @param id
	 *            this Commander's ID. Should be unique for this type.
	 * @param family
	 *            the {@link Family} of entities which at any moment are eligible
	 *            for processing. {@code null} if no restriction.
	 */
	public Commander(int id, Family family) {
		
		this.family = family;
		this.id = id;
	}
	
	/**
	 * Every Commander has an ID by which it can be distinguished, and which allows
	 * an entity to identify which Commander it should receive Commands from.
	 * 
	 * @return
	 */
	public int getID() {
		
		return id;
	}
	
	/**
	 * Only {@link Entity}s belonging to the given {@link Family} will be eligible
	 * for processing, regardless of their chosen
	 * {@link AcceptsCommands#getCommanderID() commander-ID}.
	 * 
	 * @return {@code null} if no such restrictions apply
	 */
	public Family getFamily() {
		
		return family;
	}
	
	/**
	 * Get the most-appropriate Command to execute for the given entity.
	 * 
	 * @return {@code null} if no Command can be specified
	 */
	public abstract Command getCommand(Entity entity);
}
