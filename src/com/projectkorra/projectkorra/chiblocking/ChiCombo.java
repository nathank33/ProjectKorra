package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ChiAbility;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;

public class ChiCombo extends ChiAbility {
	
	/**
	 * a Map containing every entity which is paralyzed, and the time in milliseconds at which they will be unparalyzed.
	 */
	private static final ConcurrentHashMap<Entity, Long> PARALYZED_ENTITIES = new ConcurrentHashMap<>();
	
	private boolean enabled;
	private long duration;
	private long cooldown;
	private Entity target;
	private String name;
	
	public ChiCombo(Player player, String ability) {
		super(player);
		
		this.name = ability;
		this.enabled = getConfig().getBoolean("Abilities.Chi.ChiCombo.Enabled");
		
		if (!enabled) {
			return;
		}
		if (ability.equalsIgnoreCase("Immobilize")) {
			this.cooldown = getConfig().getLong("Abilities.Chi.ChiCombo.Immobilize.Cooldown");
			this.duration = getConfig().getLong("Abilities.Chi.ChiCombo.Immobilize.ParalyzeDuration");
			if (!GeneralMethods.canBend(player.getName(), name) || bPlayer.isOnCooldown(this)) {
				return;
			} else {
				target = GeneralMethods.getTargetedEntity(player, 5);
				paralyze(target, duration);
				start();
				bPlayer.addCooldown(this);
			}
		}
	}

	/**
	 * Paralyzes the target for the given duration. The player will
	 * be unable to move or interact for the duration.
	 * @param target The Entity to be paralyzed
	 * @param duration The time in milliseconds the target will be paralyzed
	 */
	private static void paralyze(Entity target, Long duration) {
		PARALYZED_ENTITIES.put(target, (System.currentTimeMillis() + duration));
	}

	/**
	 * Convenience method to see if a Player is paralyzed by a ChiCombo. 
	 * Calls {@link ChiCombo#isParalyzed(Entity)} with the Player casted to an Entity.
	 * 
	 * @param player The player to check if they're paralyzed
	 * @return True if the player is paralyzed, false otherwise
	 */
	public static boolean isParalyzed(Player player) {
		return isParalyzed((Entity) player);
	}

	/**
	 * Checks if an entity is paralyzed by a ChiCombo.
	 * 
	 * @param entity The entity to check if they're paralyzed
	 * @return True if the entity is paralyzed, false otherwise
	 */
	public static boolean isParalyzed(Entity entity) {
		return PARALYZED_ENTITIES.containsKey(entity);
	}

	/**
	 * Checks the status of all paralyzed entities. If their paralysis has expired,
	 * it removes them from {@link ChiCombo#PARALYZED_ENTITIES paralyzedEntities} and
	 * removes the instance of the combo from {@link ChiCombo#instances instances}.
	 */
	public static void handleParalysis() {
		for (Entity entity : PARALYZED_ENTITIES.keySet()) {
			if (PARALYZED_ENTITIES.get(entity) <= System.currentTimeMillis()) {
				PARALYZED_ENTITIES.remove(entity);
				
				for (ChiCombo combo : getAbilities(ChiCombo.class)) {
					if (combo.target.equals(entity)) {
						combo.remove();
					}
				}
			}
		}
	}

	@Override
	public String getName() {
		return name != null ? name : "ChiCombo";
	}

	@Override
	public void progress() {
	}

	@Override
	public Location getLocation() {
		return target != null ? target.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	@Override
	public boolean isHiddenAbility() {
		return true;
	}
	
	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public Entity getTarget() {
		return target;
	}

	public void setTarget(Entity target) {
		this.target = target;
	}

	public static ConcurrentHashMap<Entity, Long> getParalyzedEntities() {
		return PARALYZED_ENTITIES;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setName(String name) {
		this.name = name;
	}

}
