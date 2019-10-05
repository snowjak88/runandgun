/**
 * 
 */
package org.snowjak.runandgun.util.loaders;

import java.lang.reflect.Type;

import org.snowjak.runandgun.context.Context;
import org.snowjak.runandgun.systems.EntityRefManager;
import org.snowjak.runandgun.systems.TeamManager;
import org.snowjak.runandgun.systems.UniqueTagManager;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;

import squidpony.squidmath.SquidID;

/**
 * @author snowjak88
 *
 */
public class EntityLoader implements Loader<Entity> {
	
	@Override
	public JsonElement serialize(Entity src, Type typeOfSrc, JsonSerializationContext context) {
		
		if (src == null)
			return JsonNull.INSTANCE;
		
		final JsonObject obj = new JsonObject();
		
		final EntityRefManager ref = Context.get().engine().getSystem(EntityRefManager.class);
		obj.add("id", context.serialize(ref.get(src), SquidID.class));
		
		final TeamManager tm = Context.get().engine().getSystem(TeamManager.class);
		if (tm.getTeam(src) != null)
			obj.add("team", new JsonPrimitive(tm.getTeamName(tm.getTeam(src))));
		
		final UniqueTagManager utm = Context.get().engine().getSystem(UniqueTagManager.class);
		if (utm.has(src))
			obj.add("tag", new JsonPrimitive(utm.get(src)));
		
		final JsonArray components = new JsonArray();
		for (Component c : src.getComponents())
			if (!c.getClass().isAnnotationPresent(IgnoreSerialization.class))
				components.add(context.serialize(c, Component.class));
			
		obj.add("components", components);
		
		return obj;
	}
	
	@Override
	public Entity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {
		
		if (json.isJsonNull())
			return null;
		
		if (!json.isJsonObject())
			throw new JsonParseException("Cannot parse Entity from JSON -- not an object!");
		
		final JsonObject obj = json.getAsJsonObject();
		
		final Entity e = Context.get().engine().createEntity();
		
		if (obj.has("id"))
			Context.get().engine().getSystem(EntityRefManager.class).add(e,
					context.deserialize(obj.get("id"), SquidID.class));
		
		if (obj.has("team"))
			Context.get().engine().getSystem(TeamManager.class).add(obj.get("team").getAsString(), e);
		
		if (obj.has("tag"))
			Context.get().engine().getSystem(UniqueTagManager.class).set(obj.get("tag").getAsString(), e);
		
		if (obj.has("components")) {
			final JsonArray components = obj.getAsJsonArray("components");
			components.forEach(je -> e.add(context.deserialize(je, Component.class)));
		}
		
		return e;
	}
	
}
