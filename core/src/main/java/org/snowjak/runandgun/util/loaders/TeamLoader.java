/**
 * 
 */
package org.snowjak.runandgun.util.loaders;

import java.lang.reflect.Type;

import org.snowjak.runandgun.map.KnownMap;
import org.snowjak.runandgun.team.Team;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

/**
 * @author snowjak88
 *
 */
public class TeamLoader implements Loader<Team> {
	
	@Override
	public JsonElement serialize(Team src, Type typeOfSrc, JsonSerializationContext context) {
		
		if (src == null)
			return JsonNull.INSTANCE;
		
		final JsonObject obj = new JsonObject();
		
		obj.add("map", context.serialize(src.getMap(), KnownMap.class));
		
		return obj;
	}
	
	@Override
	public Team deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		if (json.isJsonNull())
			return null;
		
		if (!json.isJsonObject())
			throw new JsonParseException("Cannot parse Team from JSON -- is not an object!");
		
		final JsonObject obj = json.getAsJsonObject();
		
		if (!obj.has("map"))
			throw new JsonParseException("Cannot parse Team from JSON -- missing [map]!");
		
		final KnownMap map = context.deserialize(obj.get("map"), KnownMap.class);
		
		return new Team(map);
	}
	
}
