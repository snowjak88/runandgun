/**
 * 
 */
package org.snowjak.runandgun.threading;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.snowjak.runandgun.context.Context;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;

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
	
	public static void update(float delta, PerFrameProcess... processes) {
		
		final Collection<ListenableFuture<?>> futures = new LinkedList<>();
		
		for (PerFrameProcess p : processes) {
			
			futures.add(Context.get().executor().submit(() -> p.update(delta)));
			
		}
		
		try {
			
			Futures.whenAllComplete(futures).call(() -> {
				return null;
			}, MoreExecutors.directExecutor()).get();
			
		} catch (InterruptedException | ExecutionException e) {
			
		}
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
