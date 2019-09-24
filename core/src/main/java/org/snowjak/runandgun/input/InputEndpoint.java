/**
 * 
 */
package org.snowjak.runandgun.input;

import java.util.function.Consumer;

import org.snowjak.runandgun.clock.ClockControl;
import org.snowjak.runandgun.commanders.UserCommander;
import org.snowjak.runandgun.commands.MoveToCommand;
import org.snowjak.runandgun.context.Context;

/**
 * Gives the several "endpoints" that inputs can be mapped to.
 * 
 * @author snowjak88
 *
 */
public enum InputEndpoint {
	/**
	 * Issue a {@link MoveToCommand} toward the current mouse-cursor, to the
	 * {@link UserCommander}.
	 */
	MOVE_TO(e -> Context.get().userCommander().addCommand(new MoveToCommand(e.getMousePoint()))),
	/**
	 * Order the {@link ClockControl} to toggle "is-paused".
	 */
	TOGGLE_PAUSE_TIME(e -> Context.get().clock().togglePaused()),
	/**
	 * Order the {@link ClockControl} to slow the clock-speed.
	 */
	SLOW_CLOCK(e -> Context.get().clock().slowClock()),
	/**
	 * Order the {@link ClockControl} to accelerate the clock-speed.
	 */
	ACCELERATE_CLOCK(e -> Context.get().clock().accelerateClock());
	
	private final Consumer<InputEvent> inputConumser;
	
	InputEndpoint(Consumer<InputEvent> inputConsumer) {
		
		this.inputConumser = inputConsumer;
	}
	
	public void consume(InputEvent inputEvent) {
		
		this.inputConumser.accept(inputEvent);
	}
}
