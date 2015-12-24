package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.api.ChiAbility;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class QuickStrike extends ChiAbility {
	public static int damage = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.QuickStrike.Damage");
	public static int blockChance = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.QuickStrike.ChiBlockChance");

	private Entity target = null;
	
	public QuickStrike() {}
	
	public QuickStrike(Player player) {
		super(player);
		target = GeneralMethods.getTargetedEntity(player, 2, new ArrayList<Entity>());
		start();
	}

	@Override
	public String getName() {
		return "QuickStrike";
	}

	@Override
	public void progress() {
		if (target == null)
			return;

		GeneralMethods.damageEntity(player, target, damage, "QuickStrike");

		if (target instanceof Player && ChiPassive.willChiBlock(player, (Player) target)) {
			ChiPassive.blockChi((Player) target);
		}
		remove();
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public long getCooldown() {
		return 0;
	}
}
