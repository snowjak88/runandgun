/**
 * 
 */
package org.snowjak.runandgun.components;

import com.badlogic.ashley.core.Component;

/**
 * Indicates that an entity can accept orders from something.
 * 
 * @author snowjak88
 *
 */
public class AcceptsCommands implements Component {
	
	private int commanderID;
	
	public AcceptsCommands() {
		
	}
	
	public AcceptsCommands(int commanderID) {
		
		this.commanderID = commanderID;
	}
	
	public int getCommanderID() {
		
		return commanderID;
	}
	
	public void setCommanderID(int commanderID) {
		
		this.commanderID = commanderID;
	}
}
