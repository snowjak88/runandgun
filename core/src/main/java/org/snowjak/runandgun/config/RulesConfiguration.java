/**
 * 
 */
package org.snowjak.runandgun.config;

import java.util.concurrent.locks.ReentrantLock;

import org.snowjak.runandgun.systems.PathfindingSystem;
import org.snowjak.runandgun.systems.TeamMapSharingSystem;
import org.snowjak.runandgun.systems.TeamMapUploadingSystem;

import squidpony.squidmath.CoordPacker;

/**
 * Holds configuration items relating to game rules.
 * 
 * @author snowjak88
 *
 */
public class RulesConfiguration {
	
	/**
	 * This config's JSON-file will have this name.
	 */
	public static final String CONFIG_FILENAME = "rules.json";
	
	private String seed = "abracadabra";
	private LightingRulesConfiguration lighting = new LightingRulesConfiguration();
	private EntitySystemRulesConfiguration entitySystem = new EntitySystemRulesConfiguration();
	
	public String getSeed() {
		
		return seed;
	}
	
	public void setSeed(String seed) {
		
		this.seed = seed;
	}
	
	public LightingRulesConfiguration lighting() {
		
		return lighting;
	}
	
	public EntitySystemRulesConfiguration entities() {
		
		return entitySystem;
	}
	
	public static class LightingRulesConfiguration {
		
		private int levels = 6;
		
		private transient double[] lightingLevelsPacking = null;
		private transient double[] lightingLevelsUnpacking = null;
		private transient static final ReentrantLock lock = new ReentrantLock();
		
		/**
		 * The game will try to compress lighting information to same RAM. When
		 * compressed, lighting-levels will be rounded-down to one of the values held in
		 * this array.
		 * <p>
		 * e.g.:
		 * 
		 * <pre>
		 *   levels[] = { 0.2, 0.4, 0.6 }
		 *   
		 *   value_1 = 0.15
		 *   value_2 = 0.4
		 *   value_3 = 0.9
		 *   
		 *   compress(value_1) = compress(0.15) --> 0.0
		 *   compress(value_2) = compress(0.4) --> 0.4
		 *   compress(value_3) = compress(0.9) --> 0.6
		 * </pre>
		 * </p>
		 * 
		 * @return the current number of lighting-levels
		 * @see #getLightingLevelsForPacking() to get the actual array of
		 *      lighting-levels
		 */
		public int getLevels() {
			
			return levels;
		}
		
		/**
		 * The game will try to compress lighting information to same RAM. When
		 * compressed, lighting-levels will be rounded-down to one of the values held in
		 * this array.
		 * <p>
		 * e.g.:
		 * 
		 * <pre>
		 *   levels[] = { 0.2, 0.4, 0.6 }
		 *   
		 *   value_1 = 0.15
		 *   value_2 = 0.4
		 *   value_3 = 0.9
		 *   
		 *   compress(value_1) = compress(0.15) --> 0.0
		 *   compress(value_2) = compress(0.4) --> 0.4
		 *   compress(value_3) = compress(0.9) --> 0.6
		 * </pre>
		 * </p>
		 * 
		 * @param levels
		 */
		public void setLevels(int levels) {
			
			lock.lock();
			
			if (this.levels != levels) {
				lightingLevelsPacking = null;
				lightingLevelsUnpacking = null;
			}
			
			this.levels = levels;
			
			lock.unlock();
		}
		
		/**
		 * The game will try to compress lighting information to same RAM. When
		 * compressed, lighting-levels will be rounded-down to one of the values held in
		 * this array.
		 * <p>
		 * e.g.:
		 * 
		 * <pre>
		 *   levels[] = { 0.2, 0.4, 0.6 }
		 *   
		 *   value_1 = 0.15
		 *   value_2 = 0.4
		 *   value_3 = 0.9
		 *   
		 *   compress(value_1) = compress(0.15) --> 0.0
		 *   compress(value_2) = compress(0.4) --> 0.4
		 *   compress(value_3) = compress(0.9) --> 0.6
		 * </pre>
		 * </p>
		 * 
		 * @return the lighting-level list, to be used with
		 *         {@link CoordPacker#packMulti(double[][], double[])}
		 */
		public double[] getLightingLevelsForPacking() {
			
			if (lightingLevelsPacking == null) {
				lock.lock();
				if (lightingLevelsPacking == null)
					lightingLevelsPacking = CoordPacker.generatePackingLevels(getLevels());
				lock.unlock();
			}
			
			return lightingLevelsPacking;
		}
		
		/**
		 * The game will try to compress lighting information to same RAM. When
		 * compressed, lighting-levels will be rounded-down to one of the values held in
		 * this array.
		 * <p>
		 * e.g.:
		 * 
		 * <pre>
		 *  levels[] = { 0.2, 0.4, 0.6 }
		 * 
		 * value_1 = 0.15 value_2 = 0.4 value_3 = 0.9
		 * 
		 * compress(value_1) = compress(0.15) --> 0.0 compress(value_2) = compress(0.4)
		 * --> 0.4 compress(value_3) = compress(0.9) --> 0.6
		 * </pre>
		 * </p>
		 * 
		 * @return the lighting-level list, to be used with
		 *         {@link CoordPacker#unpackMultiDouble(short[][], int, int, double[])
		 */
		public double[] getLightingLevelsForUnpacking() {
			
			if (lightingLevelsUnpacking == null) {
				lock.lock();
				if (lightingLevelsUnpacking == null)
					lightingLevelsUnpacking = CoordPacker.generateLightLevels(getLevels());
				lock.unlock();
			}
			
			return lightingLevelsUnpacking;
		}
	}
	
	/**
	 * Configuration relating to the entity-system.
	 * 
	 * @author snowjak88
	 *
	 */
	public static class EntitySystemRulesConfiguration {
		
		private float pathfindingInterval = 0.5f;
		private float mapUploadingInterval = 1.0f;
		private float mapSharingInterval = 1.0f;
		
		/**
		 * @return the interval (in seconds) regulating the rate at which
		 *         {@link PathfindingSystem pathfinding} is allowed to take place
		 */
		public float getPathfindingInterval() {
			
			return pathfindingInterval;
		}
		
		/**
		 * Set the interval (in seconds) regulating the rate at which
		 * {@link PathfindingSystem pathfinding} is allowed to take place.
		 * 
		 * @param pathfindingInterval
		 */
		public void setPathfindingInterval(float pathfindingInterval) {
			
			this.pathfindingInterval = pathfindingInterval;
		}
		
		/**
		 * @return the interval (in seconds) regulating the rate at which
		 *         {@link TeamMapUploadingSystem team map-uploading} is allowed to take
		 *         place
		 */
		public float getMapUploadingInterval() {
			
			return mapUploadingInterval;
		}
		
		/**
		 * Set the interval (in seconds) regulating the rate at which
		 * {@link TeamMapUploadingSystem team map-uploading} is allowed to take place.
		 * 
		 * @param mapUploadingInterval
		 */
		public void setMapUploadingInterval(float mapUploadingInterval) {
			
			this.mapUploadingInterval = mapUploadingInterval;
		}
		
		/**
		 * @return the interval (in seconds) regulating the rate at which
		 *         {@link TeamMapSharingSystem team map-sharing} is allowed to take
		 *         place
		 */
		public float getMapSharingInterval() {
			
			return mapSharingInterval;
		}
		
		/**
		 * Set the interval (in seconds) regulating the rate at which
		 * {@link TeamMapSharingSystem team map-sharing} is allowed to take place.
		 * 
		 * @param mapSharingInterval
		 */
		public void setMapSharingInterval(float mapSharingInterval) {
			
			this.mapSharingInterval = mapSharingInterval;
		}
	}
}
