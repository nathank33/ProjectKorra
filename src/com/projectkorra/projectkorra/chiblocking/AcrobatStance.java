package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.api.ChiAbility;
import com.projectkorra.projectkorra.earthbending.MetalClips;
import com.projectkorra.projectkorra.waterbending.Bloodbending;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AcrobatStance extends ChiAbility {

	private Player player;
	public double chiBlockBoost = ProjectKorra.plugin.getConfig().getDouble("Abilities.Chi.AcrobatStance.ChiBlockBoost");
	public double paralyzeDodgeBoost = ProjectKorra.plugin.getConfig().getDouble("Abilities.Chi.AcrobatStance.ParalyzeChanceDecrease");
	public int speed = ChiPassive.speedPower + 1;
	public int jump = ChiPassive.jumpPower + 1;

	public AcrobatStance () {}
	
	public AcrobatStance(Player player) {
		super(player);
		chiBlockBoost = ProjectKorra.plugin.getConfig().getDouble("Abilities.Chi.AcrobatStance.ChiBlockBoost");
		paralyzeDodgeBoost = ProjectKorra.plugin.getConfig().getDouble("Abilities.Chi.AcrobatStance.ParalyzeChanceDecrease");
		ChiAbility stance = bPlayer.getStance();
		if (!(stance instanceof AcrobatStance)) {
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

		if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, speed));
		}

		if (!player.hasPotionEffect(PotionEffectType.JUMP)) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, jump));
		}
	}
	
	public double getChiBlockBoost() {
		return chiBlockBoost;
	}

	public void setChiBlockBoost(double chiBlockBoost) {
		this.chiBlockBoost = chiBlockBoost;
	}

	public double getParalyzeDodgeBoost() {
		return paralyzeDodgeBoost;
	}

	public void setParalyzeDodgeBoost(double paralyzeDodgeBoost) {
		this.paralyzeDodgeBoost = paralyzeDodgeBoost;
	}

	public Player getPlayer() {
		return player;
	}

	public int getSpeed() {
		return speed;
	}

	public void setSpeed(int speed) {
		this.speed = speed;
	}

	public int getJump() {
		return jump;
	}

	public void setJump(int jump) {
		this.jump = jump;
	}

	@Override
	public String getName() {
		return "AcrobatStance";
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
