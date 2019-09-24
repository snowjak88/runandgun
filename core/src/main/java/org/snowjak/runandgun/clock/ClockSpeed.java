/**
 * 
 */
package org.snowjak.runandgun.clock;

/**
 * Specifies the clock-multipliers available to the game.
 * 
 * @author snowjak88
 *
 */
public enum ClockSpeed {
	EIGTHTH(0.125f, 0, 1), QUARTER(0.25f, 0, 2), HALF(0.5f, 1, 3), ONE(1.0f, 2, 4), DOUBLE(2.0f, 3, 5), QUADRUPLE(4.0f,
			4, 6), OCTUPLE(8.0f, 5, 6);
	
	private final float multiplier;
	private final int slowerIndex, fasterIndex;
	
	ClockSpeed(float multiplier, int lowerIndex, int higherIndex) {
		
		this.multiplier = multiplier;
		this.slowerIndex = lowerIndex;
		this.fasterIndex = higherIndex;
	}
	
	public float getMultiplier() {
		
		return multiplier;
	}
	
	public ClockSpeed getSlower() {
		
		return ClockSpeed.values()[slowerIndex];
	}
	
	public ClockSpeed getFaster() {
		
		return ClockSpeed.values()[fasterIndex];
	}
}
