/**
 * 
 */
package org.snowjak.runandgun.input;

import squidpony.squidmath.Coord;

/**
 * Represents a standard input encapsulator, capable of representing a unique
 * combination of mouse- and keyboard-events.
 * 
 * @author snowjak88
 *
 */
public class InputEvent {
	
	private Coord mousePoint;
	private int mouseButton;
	
	private boolean isAlt, isCtrl, isShift;
	private char key;
	
	public InputEvent(Coord mousePoint, int mouseButton, boolean isAlt, boolean isCtrl, boolean isShift, char key) {
		
		this.mousePoint = mousePoint;
		this.mouseButton = mouseButton;
		this.isAlt = isAlt;
		this.isCtrl = isCtrl;
		this.isShift = isShift;
		this.key = key;
	}
	
	public Coord getMousePoint() {
		
		return mousePoint;
	}
	
	public int getMouseButton() {
		
		return mouseButton;
	}
	
	public boolean isAlt() {
		
		return isAlt;
	}
	
	public boolean isCtrl() {
		
		return isCtrl;
	}
	
	public boolean isShift() {
		
		return isShift;
	}
	
	public char getKey() {
		
		return key;
	}
	
}
