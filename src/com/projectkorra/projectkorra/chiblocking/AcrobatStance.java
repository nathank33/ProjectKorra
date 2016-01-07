package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.ability.ChiAbility;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class AcrobatStance extends ChiAbility {
	
	private int speed;
	private int jump;
	private double chiBlockBoost;
	private double paralyzeDodgeBoost;

	public AcrobatStance () {
	}
	
	public AcrobatStance(Player player) {
		super(player);
		
		this.chiBlockBoost = getConfig().getDouble("Abilities.Chi.AcrobatStance.ChiBlockBoost");
		this.paralyzeDodgeBoost = getConfig().getDouble("Abilities.Chi.AcrobatStance.ParalyzeChanceDecrease");
		this.speed = ChiPassive.getSpeedPower() + 1;
		this.jump = ChiPassive.getJumpPower() + 1;
		
		ChiAbility stance = bPlayer.getStance();
		if (stance != null && !(stance instanceof AcrobatStance)) {
			stance.remove();
			bPlayer.setStance(this);
		}
		start();
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
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
	
	@Override
	public String getName() {
		return "AcrobatStance";
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return 0;
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
	
}
