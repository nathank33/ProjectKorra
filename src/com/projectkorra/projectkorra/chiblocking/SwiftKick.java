package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.api.ChiAbility;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.ArrayList;

public class SwiftKick extends ChiAbility {
	public static int damage = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.SwiftKick.Damage");
	public static int blockChance = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.ChiCombo.ChiBlockChance");
	
	private Entity target;

	public SwiftKick () {}
	
	public SwiftKick(Player player) {
		super(player);
		damage = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.SwiftKick.Damage");
		blockChance = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.ChiCombo.ChiBlockChance");
		target = GeneralMethods.getTargetedEntity(player, 4, new ArrayList<Entity>());
		start();
	}

	@Override
	public String getName() {
		return "SwiftKick";
	}

	@Override
	public void progress() {
		if (target == null) {
			remove();
			return;
		}
		GeneralMethods.damageEntity(player, target, damage, "SwiftKick");
		if (target instanceof Player && ChiPassive.willChiBlock(player, (Player) target)) {
			ChiPassive.blockChi((Player) target);
		}
		bPlayer.addCooldown(this);
		remove();
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public long getCooldown() {
		//TODO make this cooldown not hardcoded
		return 4000;
	}
}
