/**
 * 
 */
package org.snowjak.runandgun.commands;

import com.badlogic.ashley.core.Entity;

/**
 * Represents an order to execute some action.
 * 
 * @author snowjak88
 *
 */
public interface Command {
	
	/**
	 * @return {@code true} if this Command expects to be called via
	 *         {@link #execute(Entity)}, {@code false} if {@link #execute()}
	 */
	public boolean isEntitySpecific();
	
	/**
	 * Execute this Command.
	 */
	public void execute();
	
	/**
	 * Execute this Command against the specified {@link Entity}.
	 */
	public void execute(Entity entity);
	
}
