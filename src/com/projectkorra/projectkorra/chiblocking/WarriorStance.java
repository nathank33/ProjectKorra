package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.StockAbility;
import com.projectkorra.projectkorra.ability.api.ChiAbility;
import com.projectkorra.projectkorra.earthbending.MetalClips;
import com.projectkorra.projectkorra.waterbending.Bloodbending;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class WarriorStance extends ChiAbility {

	public int strength = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.WarriorStance.Strength") - 1;
	public int resistance = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.WarriorStance.Resistance");

	public WarriorStance() {}
	
	public WarriorStance(Player player) {
		super(player);
		strength = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.WarriorStance.Strength") - 1;
		resistance = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.WarriorStance.Resistance");
		ChiAbility stance = bPlayer.getStance();
		if (!(stance instanceof WarriorStance)) {
			stance.remove();
			bPlayer.setStance(this);
		}
		start();
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline() || !GeneralMethods.canBend(player.getName(), "AcrobatStance") 
				|| MetalClips.isControlled(player) || Paralyze.isParalyzed(player) || Bloodbending.isBloodbended(player)) {
			remove();
			return;
		}
		
		if (!player.hasPotionEffect(PotionEffectType.DAMAGE_RESISTANCE)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, 60, resistance));
		}

		if (!player.hasPotionEffect(PotionEffectType.INCREASE_DAMAGE)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.INCREASE_DAMAGE, 60, strength));
		}
	}
	
	@Override
	public String getName() {
		return "WarriorStance";
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
