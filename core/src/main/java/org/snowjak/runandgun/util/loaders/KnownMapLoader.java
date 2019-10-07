/**
 * 
 */
package org.snowjak.runandgun.util.loaders;

import java.lang.reflect.Type;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.map.KnownMap;
import org.snowjak.runandgun.systems.EntityRefManager;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.Color;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import squidpony.squidmath.Coord;
import squidpony.squidmath.SquidID;

/**
 * @author snowjak88
 *
 */
public class KnownMapLoader implements Loader<KnownMap> {
	
	@Override
	public JsonElement serialize(KnownMap src, Type typeOfSrc, JsonSerializationContext context) {
		
		final JsonObject obj = new JsonObject();
		
		obj.add("width", new JsonPrimitive(src.getWidth()));
		obj.add("height", new JsonPrimitive(src.getHeight()));
		
		final String known_64 = toBase64(src.getKnown());
		obj.add("known", new JsonPrimitive(known_64));
		
		//
		// Serialize map
		//
		
		final JsonObject map = new JsonObject();
		for (char c : src.getUniqueKnownChars())
			map.add(String.valueOf(c), new JsonPrimitive(toBase64(src.getKnown(c))));
		obj.add("map", map);
		
		//
		
		final JsonObject colors = new JsonObject();
		for (Color c : src.getUniqueKnownColors())
			colors.add(context.serialize(c, Color.class).getAsString(),
					new JsonPrimitive(toBase64(src.getKnown(c, false))));
		obj.add("colors", colors);
		
		//
		
		final JsonObject bgColors = new JsonObject();
		for (Color c : src.getUniqueKnownBGColors())
			bgColors.add(context.serialize(c, Color.class).getAsString(),
					new JsonPrimitive(toBase64(src.getKnown(c, true))));
		obj.add("bg-colors", bgColors);
		
		//
		
		final JsonObject knownEntities = new JsonObject();
		final EntityRefManager refManager = Context.get().engine().getSystem(EntityRefManager.class);
		
		for (Coord c : src.getVisibleRegion()) {
			
			final Collection<Entity> entities = src.getEntitiesAt(c);
			if (!entities.isEmpty()) {
				
				final JsonArray entitiesAtCoord = new JsonArray(entities.size());
				for (Entity e : entities) {
					if (e == null)
						continue;
					entitiesAtCoord.add(context.serialize(refManager.get(e), SquidID.class));
				}
				knownEntities.add(toString(c), entitiesAtCoord);
			}
		}
		
		obj.add("entities", knownEntities);
		
		return obj;
	}
	
	@Override
	public KnownMap deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		if (json == JsonNull.INSTANCE)
			return null;
		
		if (!json.isJsonObject())
			throw new JsonParseException("Cannot parse known-map from JSON -- not an object!");
		
		final JsonObject obj = json.getAsJsonObject();
		
		final int width, height;
		
		if (!obj.has("width"))
			throw new JsonParseException("Cannot parse known-map from JSON -- missing [width]!");
		width = obj.get("width").getAsInt();
		
		if (!obj.has("height"))
			throw new JsonParseException("Cannot parse known-map from JSON -- missing [width]!");
		height = obj.get("height").getAsInt();
		
		if (!obj.has("known"))
			throw new JsonParseException("Cannnot parse known-map from JSON -- missing [known]!");
		final short[] known = toShortArray(obj.get("known").getAsString());
		
		if (!obj.has("map"))
			throw new JsonParseException("Cannot parse known-map from JSON -- missing [map]!");
		if (!obj.get("map").isJsonObject())
			throw new JsonParseException("Cannot parse known-map from JSON -- [map] is not an object!");
		final JsonObject map = obj.getAsJsonObject("map");
		
		final Map<Character, short[]> charMap = new HashMap<>();
		for (Map.Entry<String, JsonElement> mapEntry : map.entrySet())
			charMap.put(mapEntry.getKey().charAt(0), toShortArray(mapEntry.getValue().getAsString()));
		
		if (!obj.has("colors"))
			throw new JsonParseException("Cannot parse known-map from JSON -- missing [colors]!");
		if (!obj.get("colors").isJsonObject())
			throw new JsonParseException("Cannot parse known-map from JSON -- [colors] is not an object!");
		final JsonObject colors = obj.getAsJsonObject("colors");
		
		final Map<Color, short[]> colorMap = new HashMap<>();
		for (Map.Entry<String, JsonElement> colorsEntry : colors.entrySet())
			colorMap.put(Color.valueOf(colorsEntry.getKey()), toShortArray(colorsEntry.getValue().getAsString()));
		
		if (!obj.has("bg-colors"))
			throw new JsonParseException("Cannot parse known-map from JSON -- missing [bg-colors]!");
		if (!obj.get("bg-colors").isJsonObject())
			throw new JsonParseException("Cannot parse known-map from JSON -- [bg-colors] is not an object!");
		final JsonObject bgColors = obj.getAsJsonObject("bg-colors");
		
		final KnownMap result = new KnownMap(width, height, known, charMap, colorMap, colorMap);
		
		final Map<Color, short[]> bgColorMap = new HashMap<>();
		for (Map.Entry<String, JsonElement> bgColorsEntry : bgColors.entrySet())
			bgColorMap.put(Color.valueOf(bgColorsEntry.getKey()), toShortArray(bgColorsEntry.getValue().getAsString()));
		
		if (!obj.has("entities"))
			throw new JsonParseException("Cannot parse known-map from JSON -- missing [entities]!");
		if (!obj.get("entities").isJsonObject())
			throw new JsonParseException("Cannot parse known-map from JSON -- [entities] is not an object!");
		
		try {
			final EntityRefManager ref = Context.get().engine().getSystem(EntityRefManager.class);
			for (Map.Entry<String, JsonElement> entityEntry : obj.get("entities").getAsJsonObject().entrySet()) {
				final Coord c = toCoord(entityEntry.getKey());
				
				if (!entityEntry.getValue().isJsonArray())
					throw new JsonParseException("Cannot parse known-map from JSON -- [entities] has a child at '"
							+ entityEntry.getKey() + "' which is not an array!");
				final JsonArray entityRefArray = entityEntry.getValue().getAsJsonArray();
				
				for (int i = 0; i < entityRefArray.size(); i++)
					ref.addReferenceResolution(context.deserialize(entityRefArray.get(i), SquidID.class),
							(e) -> result.addKnownEntity(e, c));
			}
		} catch (IllegalArgumentException e) {
			throw new JsonParseException("Cannot parse known-map from JSON -- cannot parse [entities]!", e);
		}
		
		return result;
	}
	
	private String toBase64(short[] value) {
		
		final ByteBuffer lineBuffer = ByteBuffer.allocate(value.length * Short.BYTES);
		lineBuffer.asShortBuffer().put(value);
		
		return Base64.getEncoder().encodeToString(lineBuffer.array());
	}
	
	private short[] toShortArray(String base64) {
		
		final ShortBuffer buffer = ByteBuffer.wrap(Base64.getDecoder().decode(base64)).asShortBuffer();
		
		if (!buffer.isReadOnly() && buffer.hasArray())
			return buffer.compact().array();
		
		buffer.rewind();
		final short[] result = new short[buffer.capacity()];
		buffer.get(result);
		return result;
	}
	
	private String toString(Coord coord) {
		
		if (coord == null)
			return null;
		
		return "[" + coord.x + ":" + coord.y + "]";
	}
	
	private Coord toCoord(String coord) throws IllegalArgumentException {
		
		if (coord == null || coord.trim().equals(""))
			return null;
		coord = coord.trim();
		
		if (!coord.startsWith("[") || !coord.endsWith("]"))
			throw new IllegalArgumentException("Cannot parse String ('" + coord + "') as Coord -- improper format!");
		coord = coord.substring(1, coord.length() - 1);
		
		final String[] split = coord.split(":");
		if (split.length != 2)
			throw new IllegalArgumentException("Cannot parse String ('" + coord + "') as Coord -- improper format!");
		
		try {
			final int[] ints = Arrays.stream(split).mapToInt(Integer::parseInt).toArray();
			
			return Coord.get(ints[0], ints[1]);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Cannot parse String ('" + coord + "') as Coord -- improper format!", e);
		}
	}
}
