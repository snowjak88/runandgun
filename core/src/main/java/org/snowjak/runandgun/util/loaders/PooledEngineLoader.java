/**
 * 
 */
package org.snowjak.runandgun.util.loaders;

import java.lang.reflect.Type;
import java.util.Map;

import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.systems.TeamManager;
import org.snowjak.runandgun.team.Team;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.PooledEngine;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;

/**
 * {@link Engine}-loading is a special case in this application. Instead of
 * creating a new instance, we use the pre-created {@link Context#engine()
 * shared instance}.
 * 
 * @author snowjak88
 *
 */
public class PooledEngineLoader implements Loader<PooledEngine> {
	
	@Override
	public JsonElement serialize(PooledEngine src, Type typeOfSrc, JsonSerializationContext context) {
		
		if (src == null)
			return JsonNull.INSTANCE;
		
		final JsonObject obj = new JsonObject();
		
		final JsonArray entities = new JsonArray();
		for (Entity e : src.getEntities())
			entities.add(context.serialize(e, Entity.class));
		
		obj.add("entities", entities);
		
		final TeamManager tm = Context.get().engine().getSystem(TeamManager.class);
		
		final JsonObject teams = new JsonObject();
		for (Team t : tm.getTeams())
			teams.add(tm.getTeamName(t), context.serialize(t, Team.class));
		
		obj.add("teams", teams);
		
		return obj;
	}
	
	@Override
	public PooledEngine deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		if (json.isJsonNull())
			return null;
		
		if (!json.isJsonObject())
			throw new JsonParseException("Cannot deserialize Engine from JSON -- not an object!");
		
		final JsonObject obj = json.getAsJsonObject();
		
		final PooledEngine engine = Context.get().engine();
		
		final TeamManager tm = engine.getSystem(TeamManager.class);
		
		if (obj.has("teams")) {
			final JsonObject teams = obj.getAsJsonObject("teams");
			for (Map.Entry<String, JsonElement> entry : teams.entrySet())
				tm.addTeam(entry.getKey(), context.deserialize(entry.getValue(), Team.class));
		}
		
		if (obj.has("entities")) {
			final JsonArray entities = obj.getAsJsonArray("entities");
			for (JsonElement je : entities)
				engine.addEntity(context.deserialize(je, Entity.class));
		}
		
		return engine;
	}
	
}