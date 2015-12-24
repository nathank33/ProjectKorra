package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AvatarState;
import com.projectkorra.projectkorra.ability.api.ChiAbility;
import com.projectkorra.projectkorra.airbending.Suffocate;
import com.projectkorra.projectkorra.command.Commands;

import org.bukkit.Location;
import org.bukkit.entity.Creature;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.concurrent.ConcurrentHashMap;

public class Paralyze extends ChiAbility {

	private static ConcurrentHashMap<Entity, Long> entities = new ConcurrentHashMap<Entity, Long>();
	private static ConcurrentHashMap<Entity, Long> cooldowns = new ConcurrentHashMap<Entity, Long>();

	private long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Chi.Paralyze.Cooldown");
	private static long duration = ProjectKorra.plugin.getConfig().getLong("Abilities.Chi.Paralyze.Duration");
	
	private Entity target;

	public Paralyze () {}
	
	public Paralyze(Player sourceplayer, Entity targetentity) {
		super(sourceplayer);
		target = targetentity;
		cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Chi.Paralyze.Cooldown");
		duration = ProjectKorra.plugin.getConfig().getLong("Abilities.Chi.Paralyze.Duration");
		start();
	}

	private static void paralyze(Entity entity) {
		entities.put(entity, System.currentTimeMillis());
		if (entity instanceof Creature) {
			((Creature) entity).setTarget(null);
		}

		if (entity instanceof Player) {
			if (Suffocate.isChannelingSphere((Player) entity)) {
				Suffocate.remove((Player) entity);
			}
		}
	}

	//TODO change paralyze to use Spigot metadata rather than checking this class
	public static boolean isParalyzed(Entity entity) {
		if (entity instanceof Player) {
			if (AvatarState.isAvatarState((Player) entity))
				return false;
		}
		if (entities.containsKey(entity)) {
			if (System.currentTimeMillis() < entities.get(entity) + duration) {
				return true;
			}
			entities.remove(entity);
		}
		return false;

	}

	@Override
	public String getName() {
		return "Paralyze";
	}

	@Override
	public void progress() {
		if (GeneralMethods.isBender(target.getName(), Element.Chi) && GeneralMethods.getBoundAbility(player).equalsIgnoreCase("Paralyze") && GeneralMethods.canBend(player.getName(), "Paralyze")) {
			if (cooldowns.containsKey(target)) {
				if (System.currentTimeMillis() < cooldowns.get(target) + cooldown) {
					return;
				} else {
					cooldowns.remove(target);
				}
			}
			if (target instanceof Player) {
				if (Commands.invincible.contains(((Player) target).getName())) {
					remove();
					return;
				}
			}
			paralyze(target);
			cooldowns.put(target, System.currentTimeMillis());
		}
		else
			remove();
	}

	@Override
	public Location getLocation() {
		return target.getLocation();
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

}
