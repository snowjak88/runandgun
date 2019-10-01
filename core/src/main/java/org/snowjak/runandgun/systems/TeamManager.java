/**
 * 
 */
package org.snowjak.runandgun.systems;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.snowjak.runandgun.team.Team;

import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;

/**
 * A system managing named {@link Entity} associations.
 * 
 * @author snowjak88
 *
 */
public class TeamManager extends EntitySystem {
	
	private final Map<String, Team> nameToTeam = new HashMap<>();
	private final Map<Team, Set<Entity>> teamToEntities = new HashMap<>();
	private final Map<Entity, Team> entityToTeam = new HashMap<>();
	
	/**
	 * Clear everything from this TeamManager.
	 */
	public void clear() {
		
		synchronized (this) {
			teamToEntities.clear();
			entityToTeam.clear();
		}
	}
	
	/**
	 * Register a new Team object under the given name.
	 * 
	 * @param name
	 * @return
	 */
	public Team addTeam(String name) {
		
		synchronized (this) {
			final Team team = new Team();
			addTeam(name, team);
			return team;
		}
	}
	
	/**
	 * Add the given Team under the given name.
	 * 
	 * @param name
	 * @param team
	 */
	public void addTeam(String name, Team team) {
		
		synchronized (this) {
			nameToTeam.put(name, team);
		}
	}
	
	/**
	 * @return the set of registered {@link Team}s
	 */
	public Collection<Team> getTeams() {
		
		synchronized (this) {
			return teamToEntities.keySet();
		}
	}
	
	/**
	 * Get the {@link Team} registered under the given name.
	 * 
	 * @param name
	 * @return {@code null} if no such team-name has been registered yet
	 */
	public Team getTeam(String name) {
		
		synchronized (this) {
			return nameToTeam.get(name);
		}
	}
	
	/**
	 * Add the given {@link Entity} to the {@link Team} associated with the given
	 * name.
	 * 
	 * @param teamName
	 * @param entity
	 */
	public void add(String teamName, Entity entity) {
		
		synchronized (this) {
			final Team team = nameToTeam.computeIfAbsent(teamName, (tn) -> new Team());
			
			add(team, entity);
		}
	}
	
	/**
	 * Associate the given {@link Entity} with the given team-name.
	 * 
	 * @param team
	 * @param entity
	 */
	public void add(Team team, Entity entity) {
		
		synchronized (this) {
			teamToEntities.computeIfAbsent(team, (t) -> new LinkedHashSet<>()).add(entity);
			entityToTeam.put(entity, team);
		}
	}
	
	/**
	 * Remove the given {@link Entity} from the {@link Team} with which it is
	 * associated.
	 * 
	 * @param entity
	 */
	public void remove(Entity entity) {
		
		synchronized (this) {
			final Team team = entityToTeam.get(entity);
			if (team == null)
				return;
			entityToTeam.remove(entity);
			teamToEntities.get(team).remove(entity);
		}
	}
	
	/**
	 * Remove the given {@link Team} and <em>all</em> its associations.
	 * 
	 * @param team
	 */
	public void remove(Team team) {
		
		synchronized (this) {
			teamToEntities.getOrDefault(team, Collections.emptySet()).forEach(e -> entityToTeam.remove(e));
			teamToEntities.remove(team);
		}
	}
	
	/**
	 * Get the {@link Entity Entities} associated under the given name, or
	 * {@code null} if no such {@link Team} has been set up yet.
	 * 
	 * @param team
	 * @return
	 */
	public Set<Entity> getEntities(Team team) {
		
		synchronized (this) {
			return teamToEntities.get(team);
		}
	}
	
	/**
	 * Get the {@link Team} with which the given {@link Entity} is associated, or
	 * null if no such association has been set up yet.
	 * 
	 * @param entity
	 * @return
	 */
	public Team getTeam(Entity entity) {
		
		synchronized (this) {
			return entityToTeam.get(entity);
		}
	}
	
	/**
	 * Given a set of {@link Entity Entities}, filter that set by membership on the
	 * given {@link Team}.
	 * 
	 * @param entities
	 * @param team
	 * @return
	 */
	public Set<Entity> filterEntitiesByTeam(Collection<Entity> entities, Team team) {
		
		synchronized (this) {
			return entities.stream().filter(e -> getTeam(e) == team)
					.collect(Collectors.toCollection(LinkedHashSet::new));
		}
	}
}
