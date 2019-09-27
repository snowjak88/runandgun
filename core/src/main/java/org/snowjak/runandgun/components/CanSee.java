/**
 * 
 */
package org.snowjak.runandgun.components;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import org.snowjak.runandgun.config.RulesConfiguration.LightingRulesConfiguration;
import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.map.Map;

import com.badlogic.ashley.core.Component;

import squidpony.squidmath.Coord;
import squidpony.squidmath.CoordPacker;
import squidpony.squidmath.GreasedRegion;

/**
 * Indicates that an entity can see. It has a defined FOV, and knowledge about
 * the map.
 * 
 * @author snowjak88
 *
 */
public class CanSee implements Component {
	
	private int distance;
	
	private int width, height;
	private short[][] packedLightLevels;
	private short[] seen, known;
	private java.util.Map<Character, short[]> knownMaps = new HashMap<>();
	
	/**
	 * Create a new CanSee component, with a visibility-range of 32,767 cells (i.e.,
	 * practically infinite).
	 */
	public CanSee() {
		
		this(32767);
	}
	
	/**
	 * Create a new CanSee component with the given visibility-range.
	 * 
	 * @param distance
	 */
	public CanSee(int distance) {
		
		this.distance = distance;
		
		final Map map = Context.get().map();
		if (map != null)
			resize(map.getWidth(), map.getHeight());
	}
	
	/**
	 * Should the Map be resized, you should call this to resize this Component.
	 * 
	 * @param width
	 * @param height
	 */
	public void resize(int width, int height) {
		
		synchronized (this) {
			this.width = width;
			this.height = height;
			
			packedLightLevels = null;
			seen = CoordPacker.ALL_WALL;
			known = CoordPacker.ALL_WALL;
			knownMaps.clear();
		}
	}
	
	/**
	 * Update the held FOV "light-levels". This need not strictly equate to "light",
	 * but more generally to "visibility".
	 * 
	 * @param lightLevels
	 */
	public void setLightLevels(double[][] lightLevels) {
		
		synchronized (this) {
			packedLightLevels = CoordPacker.packMulti(lightLevels,
					Context.get().config().rules().lighting().getLightingLevelsForPacking());
			seen = CoordPacker.pack(lightLevels);
			known = CoordPacker.unionPacked(known, seen);
		}
	}
	
	/**
	 * Get the light-levels as held in this FOV.
	 * <p>
	 * <strong>Note</strong> that this incurs de-compression, and thus probably
	 * shouldn't be done too often. Instead, try to restrict yourself to
	 * {@link #isSeen(int, int)} and {@link #getLightLevel(int, int)}.
	 * </p>
	 * 
	 * @return
	 */
	public double[][] getLightLevels() {
		
		synchronized (this) {
			return CoordPacker.unpackMultiDouble(packedLightLevels, width, height,
					Context.get().config().rules().lighting().getLightingLevelsForUnpacking());
		}
	}
	
	/**
	 * Get this CanSee's "light-level" for the given map-location.
	 * 
	 * <p>
	 * Note that, with compression, light-levels will be reported in distinct
	 * {@link LightingRulesConfiguration#getLevels() levels}.
	 * </p>
	 * 
	 * @param mapX
	 * @param mapY
	 * @return
	 */
	public double getLightLevel(int mapX, int mapY) {
		
		synchronized (this) {
			final double[] lightLevels = Context.get().config().rules().lighting().getLightingLevelsForUnpacking();
			for (int i = 0; i < lightLevels.length; i++) {
				if (CoordPacker.queryPacked(packedLightLevels[i], mapX, mapY))
					return lightLevels[i];
			}
			return 0.0;
		}
	}
	
	/**
	 * @return a {@link GreasedRegion} giving the "currently-seen" parts of the map
	 */
	public GreasedRegion getSeenRegion() {
		
		synchronized (this) {
			return CoordPacker.unpackGreasedRegion(seen, width, height);
		}
	}
	
	/**
	 * @return a {@link GreasedRegion} giving the "have-ever-seen" parts of the map
	 */
	public GreasedRegion getKnownRegion() {
		
		synchronized (this) {
			return CoordPacker.unpackGreasedRegion(known, width, height);
		}
	}
	
	/**
	 * Does the {@link #getLightLevel(int, int) light-level for the given square}
	 * exceed 0.0?
	 * 
	 * @param mapX
	 * @param mapY
	 * @return
	 */
	public boolean isSeen(int mapX, int mapY) {
		
		synchronized (this) {
			return CoordPacker.queryPacked(seen, mapX, mapY);
		}
	}
	
	/**
	 * Has this entity ever seen the given map-location? (Note that this does not
	 * say whether the map-location has <em>changed</em> since the entity last saw
	 * it.)
	 * 
	 * @param mapX
	 * @param mapY
	 * @return
	 */
	public boolean isKnown(int mapX, int mapY) {
		
		synchronized (this) {
			return CoordPacker.queryPacked(known, mapX, mapY);
		}
	}
	
	/**
	 * Get the character known to be at the given map-location.
	 * 
	 * @param mapX
	 * @param mapY
	 * @return
	 */
	public char getKnownMap(int mapX, int mapY) {
		
		synchronized (this) {
			for (Character c : knownMaps.keySet())
				if (CoordPacker.queryPacked(knownMaps.get(c), mapX, mapY))
					return c;
				
			return 0;
		}
	}
	
	/**
	 * Get this CanSee's "known" map.
	 * 
	 * @return
	 */
	public char[][] getKnownMap() {
		
		synchronized (this) {
			final char[][] result = new char[width][height];
			getKnownRegions().forEach((c, r) -> r.intoChars(result, c));
			return result;
		}
	}
	
	/**
	 * Get the "known-map" for each known character.
	 * 
	 * @return
	 */
	public java.util.Map<Character, GreasedRegion> getKnownRegions() {
		
		synchronized (this) {
			final java.util.Map<Character, GreasedRegion> result = new HashMap<>();
			for (Character c : knownMaps.keySet())
				result.put(c, getKnownRegion(c));
			return result;
		}
	}
	
	/**
	 * Get the "known-map" for the given character -- giving the "last-known"
	 * locations for all map-cells of the given type.
	 * 
	 * @param c
	 * @return
	 */
	public char[][] getKnownMap(Character c) {
		
		synchronized (this) {
			return getKnownRegion(c).intoChars(new char[width][height], c);
		}
	}
	
	/**
	 * Get the "known-map" for the given character -- giving the "last-known"
	 * locations for all map-cells of the given type.
	 * 
	 * @return an empty {@link GreasedRegion} if this CanSee has not yet stored any
	 *         locations for the given character
	 */
	public GreasedRegion getKnownRegion(Character c) {
		
		synchronized (this) {
			return Optional.ofNullable(knownMaps.get(c)).map(s -> CoordPacker.unpackGreasedRegion(s, width, height))
					.orElse(new GreasedRegion(width, height));
		}
	}
	
	/**
	 * Set the "known-map" for all characters contained in the given map. You can
	 * use this to, e.g., give an entity global map-knowledge without the need to
	 * allow it to actually {@link #getSeenRegion() see} the whole map.
	 * 
	 * @param map
	 */
	public void setKnownMap(char[][] map) {
		
		synchronized (this) {
			for (Character c : getUniqueCharacters(map))
				knownMaps.put(c, CoordPacker.pack(map, c));
		}
	}
	
	/**
	 * Given the {@link #getLightLevels() current light-levels}, update this
	 * CanSee's {@link #getKnownRegion(Character) known maps}
	 * 
	 * @param map
	 */
	public void updateKnownMap(char[][] map) {
		
		synchronized (this) {
			final Coord[] seenBounds = CoordPacker.bounds(seen);
			for (Character c : getUniqueCharacters(map, seenBounds[0].x, seenBounds[0].y, seenBounds[1].x,
					seenBounds[1].y)) {
				
				final short[] seenChars = CoordPacker.intersectPacked(seen, CoordPacker.pack(map, c));
				
				final short[] newKnown = Optional.ofNullable(knownMaps.get(c)).map(knownChars -> {
					
					final short[] notSeen = CoordPacker.negatePacked(seen);
					final short[] keepAsIs = CoordPacker.intersectPacked(knownChars, notSeen);
					return CoordPacker.unionPacked(keepAsIs, seenChars);
					
				}).orElse(seenChars);
				knownMaps.put(c, newKnown);
				
			}
		}
	}
	
	private static Set<Character> getUniqueCharacters(char[][] map) {
		
		return getUniqueCharacters(map, 0, 0, map.length - 1, map[0].length);
	}
	
	private static Set<Character> getUniqueCharacters(char[][] map, int minX, int minY, int maxX, int maxY) {
		
		final Set<Character> chars = new HashSet<>();
		for (int x = minX; x <= maxX; x++)
			for (int y = minY; y <= maxY; y++)
				chars.add(map[x][y]);
		return chars;
	}
	
	/**
	 * Forget everything about the map except what you can currently see.
	 */
	public void forget() {
		
		synchronized (this) {
			known = Arrays.copyOf(seen, seen.length);
			for (Character c : knownMaps.keySet())
				knownMaps.put(c, CoordPacker.intersectPacked(knownMaps.get(c), known));
		}
	}
	
	/**
	 * @return this CanSee's given visibility-distance
	 */
	public int getDistance() {
		
		return distance;
	}
	
	/**
	 * Set this CanSee's visibility-distance.
	 * 
	 * @param distance
	 */
	public void setDistance(int distance) {
		
		this.distance = distance;
	}
}
