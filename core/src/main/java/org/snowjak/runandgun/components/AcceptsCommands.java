/**
 * 
 */
package org.snowjak.runandgun.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

/**
 * Indicates that an entity can accept orders from something.
 * 
 * @author snowjak88
 *
 */
public class AcceptsCommands implements Component, Poolable {
	
	private int commanderID;
	
	public AcceptsCommands() {
		
	}
	
	public int getCommanderID() {
		
		return commanderID;
	}
	
	public void setCommanderID(int commanderID) {
		
		this.commanderID = commanderID;
	}
	
	@Override
	public void reset() {
		
		commanderID = 0;
	}
}
