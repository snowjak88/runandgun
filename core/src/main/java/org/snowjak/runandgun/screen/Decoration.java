/**
 * 
 */
package org.snowjak.runandgun.screen;

import java.util.function.Consumer;

/**
 * A "decoration" is a short-lived graphical add-on to a {@link AbstractScreen
 * screen}.
 * 
 * @author snowjak88
 *
 */
public class Decoration {
	
	private final Consumer<DecorationProvider> decoration;
	private float duration;
	private final boolean hasDuration;
	
	/**
	 * Create a new {@link Decoration} which will last until it is explicitly
	 * removed.
	 * 
	 * @param decoration
	 */
	public Decoration(Consumer<DecorationProvider> decoration) {
		
		this(decoration, 0, false);
	}
	
	/**
	 * Create a new {@link Decoration} that should be expired after {@code duration}
	 * seconds.
	 * 
	 * @param decoration
	 * @param duration
	 */
	public Decoration(Consumer<DecorationProvider> decoration, float duration) {
		
		this(decoration, duration, true);
	}
	
	private Decoration(Consumer<DecorationProvider> decoration, float duration, boolean hasDuration) {
		
		this.decoration = decoration;
		this.duration = duration;
		this.hasDuration = hasDuration;
	}
	
	public void decorate(DecorationProvider provider) {
		
		this.decoration.accept(provider);
	}
	
	/**
	 * This method should be called every frame, to ensure that this Decoration's
	 * duration is updated properly.
	 * 
	 * @param delta
	 */
	public void updateDuration(float delta) {
		
		if (hasDuration)
			duration -= delta;
	}
	
	/**
	 * @return true if this Decoration has a duration, and if it's expired
	 */
	public boolean isExpired() {
		
		return (hasDuration && duration <= 0);
	}
	
	public float getDuration() {
		
		return duration;
	}
	
	public boolean hasDuration() {
		
		return hasDuration;
	}
}
