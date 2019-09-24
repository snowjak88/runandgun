/**
 * 
 */
package org.snowjak.runandgun.input;

/**
 * Allows us to match input-events against key-mappings (as loaded from a
 * configuration-file).
 * 
 * @author snowjak88
 *
 */
public class KeyMapping {
	
	private boolean isMouse;
	
	private boolean isAlt, isCtrl, isShift;
	private char key;
	
	public KeyMapping(char key, boolean isAlt, boolean isCtrl, boolean isShift) {
		
		this.key = key;
		this.isAlt = isAlt;
		this.isCtrl = isCtrl;
		this.isShift = isShift;
	}
	
	public KeyMapping(String descriptor) {
		
	}
}
