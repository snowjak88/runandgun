/**
 * 
 */
package org.snowjak.runandgun.clock;

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
	private float progressRemaining = 0;
	private boolean isProgressing = false;
	
	/**
	 * @return the multiplier representing the game-clock's speed relative to the
	 *         real-world clock, or {@code 0.0} if {@link isPaused()}
	 */
	public float getSpeed() {
		
		synchronized (this) {
			return clockSpeed.getMultiplier();
		}
	}
	
	/**
	 * Slow the clock to the next-slower speed.
	 */
	public void slowClock() {
		
		synchronized (this) {
			clockSpeed = clockSpeed.getSlower();
		}
	}
	
	/**
	 * Accelerate the clock to the next-faster speed.
	 */
	public void accelerateClock() {
		
		synchronized (this) {
			clockSpeed = clockSpeed.getFaster();
		}
	}
	
	/**
	 * @return {@code true} if the clock is currently paused
	 */
	public boolean isPaused() {
		
		synchronized (this) {
			return this.isPaused;
		}
	}
	
	/**
	 * Toggle the clock's "is-paused" state between {@code true}/{@code false}.
	 */
	public void togglePaused() {
		
		synchronized (this) {
			isPaused = !isPaused;
		}
	}
	
	/**
	 * Set the clock's "is-paused" state.
	 * 
	 * @param isPaused
	 */
	public void setPaused(boolean isPaused) {
		
		synchronized (this) {
			this.isPaused = isPaused;
		}
	}
	
	/**
	 * @return the clock's timestamp -- the number of in-game seconds since this
	 *         clock was created
	 */
	public float getTimestamp() {
		
		synchronized (this) {
			return timestamp;
		}
	}
	
	/**
	 * Advance this clock's timestamp by the given (real-world) time-delta. This
	 * should be called once per frame.
	 * <p>
	 * If this clock-advancement carries us past a previously-set "progress-by"
	 * amount (see {@link #progressBy(float)}), then pauses this ClockControl.
	 * </p>
	 * 
	 * @param delta
	 */
	public void update(float delta) {
		
		synchronized (this) {
			final float trueDelta = delta * getSpeed();
			
			this.timestamp += trueDelta;
			
			if (isProgressing) {
				
				progressRemaining -= trueDelta;
				
				if (progressRemaining <= 0) {
					progressRemaining = 0;
					isProgressing = false;
					setPaused(true);
				}
				
			}
		}
	}
	
	/**
	 * Allow this clock to advance by {@code delta} seconds before automatically
	 * pausing. Does <em>not</em> unpause the clock.
	 * 
	 * @param delta
	 */
	public void progressBy(float delta) {
		
		synchronized (this) {
			this.progressRemaining = delta;
			this.isProgressing = true;
		}
	}
}
