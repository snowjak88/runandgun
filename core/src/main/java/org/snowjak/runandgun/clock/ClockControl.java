/**
 * 
 */
package org.snowjak.runandgun.clock;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Handles the logic whereby the game-clock can be accelerated and deccelerated.
 * 
 * @author snowjak88
 *
 */
public class ClockControl {
	
	private ClockSpeed clockSpeed = ClockSpeed.ONE;
	private boolean isPaused = false;
	
	private float timestamp = 0;
	
	private final ReentrantLock lock = new ReentrantLock();
	
	/**
	 * @return the multiplier representing the game-clock's speed relative to the
	 *         real-world clock, or {@code 0.0} if {@link isPaused()}
	 */
	public float getSpeed() {
		
		final float speed;
		lock.lock();
		speed = clockSpeed.getMultiplier();
		lock.unlock();
		return speed;
	}
	
	/**
	 * Slow the clock to the next-slower speed.
	 */
	public void slowClock() {
		
		lock.lock();
		clockSpeed = clockSpeed.getSlower();
		lock.unlock();
	}
	
	/**
	 * Accelerate the clock to the next-faster speed.
	 */
	public void accelerateClock() {
		
		lock.lock();
		clockSpeed = clockSpeed.getFaster();
		lock.unlock();
	}
	
	/**
	 * @return {@code true} if the clock is currently paused
	 */
	public boolean isPaused() {
		
		final boolean isPaused;
		lock.lock();
		isPaused = this.isPaused;
		lock.unlock();
		return isPaused;
	}
	
	/**
	 * Toggle the clock's "is-paused" state between {@code true}/{@code false}.
	 */
	public void togglePaused() {
		
		lock.lock();
		isPaused = !isPaused;
		lock.unlock();
	}
	
	/**
	 * Set the clock's "is-paused" state.
	 * 
	 * @param isPaused
	 */
	public void setPaused(boolean isPaused) {
		
		lock.lock();
		this.isPaused = isPaused;
		lock.unlock();
	}
	
	/**
	 * @return the clock's timestamp -- the number of in-game seconds since this
	 *         clock was created
	 */
	public float getTimestamp() {
		
		return timestamp;
	}
	
	/**
	 * Advance this clock's timestamp by the given (real-world) time-delta.
	 * 
	 * @param delta
	 */
	public void update(float delta) {
		
		lock.lock();
		this.timestamp += delta * getSpeed();
		lock.unlock();
	}
}
