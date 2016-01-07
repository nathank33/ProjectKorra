package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.ChiAbility;

import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

public class SwiftKick extends ChiAbility {
	
	private int damage;
	private int blockChance;
	private long cooldown;
	private Entity target;
	
	public SwiftKick(Player player) {
		super(player);
		this.damage = getConfig().getInt("Abilities.Chi.SwiftKick.Damage");
		this.cooldown = getConfig().getInt("Abilities.Chi.ChiCombo.ChiBlockChance");
		this.cooldown = 4000;
		this.target = GeneralMethods.getTargetedEntity(player, 4);
		start();
	}

	@Override
	public void progress() {
		if (target == null) {
			remove();
			return;
		}
		GeneralMethods.damageEntity(this, target, damage);
		if (target instanceof Player && ChiPassive.willChiBlock(player, (Player) target)) {
			ChiPassive.blockChi((Player) target);
		}
		bPlayer.addCooldown(this);
		remove();
	}
	
	@Override
	public String getName() {
		return "SwiftKick";
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public int getBlockChance() {
		return blockChance;
	}

	public void setBlockChance(int blockChance) {
		this.blockChance = blockChance;
	}

	public Entity getTarget() {
		return target;
	}

	public void setTarget(Entity target) {
		this.target = target;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
	
}
