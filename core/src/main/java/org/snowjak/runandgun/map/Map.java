/**
 * 
 */
package org.snowjak.runandgun.map;

import java.util.Collection;
import java.util.function.Consumer;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;

import squidpony.squidmath.Coord;
import squidpony.squidmath.GreasedRegion;

/**
 * Encapsulates knowledge about the world.
 * 
 * @author snowjak88
 *
 */
public abstract class Map {
	
	public char getMapAt(Coord point) {
		
		return getMapAt(point.x, point.y);
	}
	
	public abstract char getMapAt(int x, int y);
	
	public Color getColorAt(Coord point) {
		
		return getColorAt(point.x, point.y);
	}
	
	public abstract Color getColorAt(int x, int y);
	
	public Color getBGColorAt(Coord point) {
		
		return getBGColorAt(point.x, point.y);
	}
	
	public abstract Color getBGColorAt(int x, int y);
	
	public abstract Collection<Entity> getEntitiesAt(Coord point);
	
	public Collection<Entity> getEntitiesAt(int x, int y) {
		
		return getEntitiesAt(Coord.get(x, y));
	}
	
	public abstract Collection<Entity> getEntitiesNear(Coord point, int radius);
	
	public abstract Collection<Entity> getEntitiesNear(int x, int y, int radius);
	
	/**
	 * Execute the given {@link Consumer} against every {@link Entity} at the given
	 * {@link Coord point}. Use this if you have problems enforcing synchronization.
	 * 
	 * @param point
	 * @param consumer
	 */
	public void forEntitiesAt(Coord point, Consumer<Entity> consumer) {
		
		synchronized (this) {
			getEntitiesAt(point).forEach(consumer);
		}
	}
	
	/**
	 * Execute the given {@link Consumer} against every {@link Entity} at the given
	 * point. Use this if you have problems enforcing synchronization.
	 * 
	 * @param x
	 * @param y
	 * @param consumer
	 */
	public void forEntitiesAt(int x, int y, Consumer<Entity> consumer) {
		
		forEntitiesAt(Coord.get(x, y), consumer);
	}
	
	public void forEntitiesNear(Coord point, int radius, Consumer<Entity> consumer) {
		
		synchronized (this) {
			getEntitiesNear(point, radius).forEach(consumer);
		}
	}
	
	public void forEntitiesNear(int x, int y, int radius, Consumer<Entity> consumer) {
		
		forEntitiesNear(Coord.get(x, y), radius, consumer);
	}
	
	public boolean isEntityAt(Entity entity, Coord point) {
		
		synchronized (this) {
			return (getEntitiesAt(point).contains(entity));
		}
	}
	
	public boolean isEntityAt(Entity entity, int x, int y) {
		
		synchronized (this) {
			return (getEntitiesAt(x, y).contains(entity));
		}
	}
	
	public boolean isEntityNear(Entity entity, Coord point, int radius) {
		
		synchronized (this) {
			return new GreasedRegion(getWidth(), getHeight()).insertCircle(point, radius).parallelStream()
					.anyMatch(c -> isEntityAt(entity, c));
		}
	}
	
	public boolean isEntityNear(Entity entity, int x, int y, int radius) {
		
		return isEntityNear(entity, Coord.get(x, y), radius);
	}
	
	public abstract Coord getEntityLocation(Entity entity);
	
	public abstract int getWidth();
	
	public abstract int getHeight();
	
	public boolean isInMap(Coord point) {
		
		return isInMap(point.x, point.y);
	}
	
	public boolean isInMap(int x, int y) {
		
		return !(x < 0 || y < 0 || x >= getWidth() || y >= getHeight());
	}
}
