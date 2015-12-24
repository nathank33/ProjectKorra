package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.api.ChiAbility;
import com.projectkorra.projectkorra.airbending.Suffocate;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class RapidPunch extends ChiAbility {

	public static List<Player> punching = new ArrayList<Player>();

	private int damage = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.RapidPunch.Damage");
	private int punches = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.RapidPunch.Punches");
	private int distance = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.RapidPunch.Distance");
	private long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.Chi.RapidPunch.Cooldown");

	private int numpunches = 0;
	private Entity target;

	public RapidPunch() {}
	
	public RapidPunch(Player player) {
		super(player);
		target = GeneralMethods.getTargetedEntity(player, distance, new ArrayList<Entity>());
		start();
	}

	@Override
	public void progress() {
		if (numpunches >= punches || target == null || !(target instanceof LivingEntity)) {
			remove();
			return;
		}
		LivingEntity lt = (LivingEntity) target;
		GeneralMethods.damageEntity(player, target, damage, "RapidPunch");
		if (target instanceof Player) {
			if (ChiPassive.willChiBlock(player, (Player) target)) {
				ChiPassive.blockChi((Player) target);
			}
			if (Suffocate.isChannelingSphere((Player) target)) {
				Suffocate.remove((Player) target);
			}
		}
		lt.setNoDamageTicks(0);
		bPlayer.addCooldown(this);
		numpunches++;
	}

	@Override
	public String getDescription() {
		return "This ability allows the chiblocker to punch rapidly in a short period. To use, simply punch." + " This has a short cooldown.";
	}
	
	@Override
	public String getName() {
		return "RapidPunch";
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}
}
