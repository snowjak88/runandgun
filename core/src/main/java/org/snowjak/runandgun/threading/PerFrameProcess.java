/**
 * 
 */
package org.snowjak.runandgun.threading;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Future;

import org.snowjak.runandgun.context.Context;

/**
 * Represents a process that should update once per frame.
 * 
 * @author snowjak88
 *
 */
public abstract class PerFrameProcess {
	
	private final BlockingQueue<Float> updateQueue = new ArrayBlockingQueue<>(1);
	
	public Future<?> start() {
		
		final Future<?> f = Context.get().executor().submit(() -> {
			
			starting();
			
			try {
				
				while (!Thread.interrupted())
					processFrame(updateQueue.take());
				
			} catch (InterruptedException e) {
				
			} finally {
				
				stopping();
				
			}
			
		});
		
		return f;
	}
	
	/**
	 * Your main thread should call this every frame. Effectively, this allows this
	 * PerFrameProcess to <em>start</em> processing the current frame.
	 * <p>
	 * This method will <strong>block</strong> until the PerFrameProcess finishes
	 * processing the last frame, at which point it will be available to take the
	 * next update.
	 * </p>
	 * 
	 * @param delta
	 * @throws InterruptedException
	 */
	public void update(float delta) {
		
		try {
			updateQueue.put(delta);
		} catch (InterruptedException e) {
			
		}
	}
	
	/**
	 * Override this method to provide on-startup functionality on the
	 * PerFrameProcess thread.
	 */
	public void starting() {
		
	}
	
	/**
	 * This method is called once per frame by the PerFrameProcess thread.
	 * 
	 * @param delta
	 */
	public abstract void processFrame(float delta);
	
	/**
	 * Override this method to provide on-shutdown functionality on the
	 * PerFrameProcess thread.
	 */
	public void stopping() {
		
	}
}
