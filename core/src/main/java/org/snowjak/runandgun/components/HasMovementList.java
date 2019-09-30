/**
 * 
 */
package org.snowjak.runandgun.components;

import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.utils.Pool.Poolable;

import squidpony.squidmath.Coord;

/**
 * Indicates that an entity has a "movement-list" -- a list of locations to move
 * into, generated by a pathfinding algorithm.
 * <p>
 * Internally, this list is defined by a {@link Queue} of {@link Coord}s, giving
 * a LIFO collection of points into which this entity should move. The
 * currently-executing movmement is always given by the head of the queue
 * ({@link #getCurrent()}).
 * </p>
 * 
 * @author snowjak88
 *
 */
public class HasMovementList implements Component, Poolable {
	
	private Queue<Coord> list = new LinkedList<>();
	
	/**
	 * Add the given {@link Coord movement-points} to this movement-list.
	 * 
	 * @param movement
	 */
	public void addMovement(List<Coord> movement) {
		
		movement.forEach(this::addMovement);
	}
	
	/**
	 * Add the given {@link Coord movement}-point to this movement-list.
	 * 
	 * @param movement
	 */
	public void addMovement(Coord movement) {
		
		list.add(movement);
	}
	
	/**
	 * Does this movement-list have any remaining entries?
	 * 
	 * @return {@code true} if this movement-list has any entries
	 */
	public boolean hasMovement() {
		
		return !list.isEmpty();
	}
	
	/**
	 * @return the currently-active {@link Coord movement}-point, or {@code null} if
	 *         this movement-list is empty
	 */
	public Coord getCurrent() {
		
		return list.peek();
	}
	
	/**
	 * Advance this movement-list by one move, dropping the current {@link Coord
	 * movement}-point.
	 * 
	 * @return {@code true} if there is at least one more movement-point remaining
	 *         in this list
	 */
	public boolean advanceList() {
		
		list.poll();
		
		return hasMovement();
	}
	
	@Override
	public void reset() {
		
		this.list.clear();
	}
}
