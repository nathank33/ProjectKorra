package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.api.AirAbility;
import com.projectkorra.projectkorra.ability.api.CoreAbility;
import com.projectkorra.projectkorra.ability.api.HealingAbility;
import com.projectkorra.projectkorra.ability.api.WaterAbility;
import com.projectkorra.projectkorra.chiblocking.Smokescreen;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class HealingWaters extends HealingAbility {

	private static long time = 0;
	
	public HealingWaters() {
	}

	public static void heal() {
		if (System.currentTimeMillis() - time >= getInterval()) {
			time = System.currentTimeMillis();
			for (Player player : Bukkit.getServer().getOnlinePlayers()) {
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
				if (bPlayer != null && bPlayer.canBend(CoreAbility.getAbility("HealingWaters"))) {
					heal(player);
				}
			}
		}
	}

	private static void heal(Player player) {
		if (inWater(player)) {
			if (player.isSneaking()) {
				Entity entity = GeneralMethods.getTargetedEntity(player, getRadius());
				if (entity instanceof LivingEntity && inWater(entity)) {
					giveHPToEntity((LivingEntity) entity);
				}
			} else {
				giveHP(player);
			}
		}
	}

	private static void giveHPToEntity(LivingEntity le) {
		if (!le.isDead() && le.getHealth() < le.getMaxHealth()) {
			applyHealingToEntity(le);
		}
		for (PotionEffect effect : le.getActivePotionEffects()) {
			if (WaterAbility.isNegativeEffect(effect.getType())) {
				le.removePotionEffect(effect.getType());
			}
		}
	}

	private static void giveHP(Player player) {
		if (!player.isDead() && player.getHealth() < 20) {
			applyHealing(player);
		}
		
		for (PotionEffect effect : player.getActivePotionEffects()) {
			if (isNegativeEffect(effect.getType())) {
				if ((effect.getType() == PotionEffectType.BLINDNESS) && Smokescreen.getBlindedTimes().containsKey(player.getName())) {
					return;
				}
				player.removePotionEffect(effect.getType());
			}
		}
	}

	private static boolean inWater(Entity entity) {
		Block block = entity.getLocation().getBlock();
		return isWater(block) && !TempBlock.isTempBlock(block);
	}

	private static void applyHealing(Player player) {
		if (!GeneralMethods.isRegionProtectedFromBuild(player, "HealingWaters", player.getLocation())) {
			if (player.getHealth() < player.getMaxHealth()) {
				player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, getDuration(), getPower()));
				AirAbility.breakBreathbendingHold(player);
			}
		}
	}

	private static void applyHealingToEntity(LivingEntity le) {
		if (le.getHealth() < le.getMaxHealth()) {
			le.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, getDuration(), 1));
			AirAbility.breakBreathbendingHold(le);
		}
	}

	public static long getTime() {
		return time;
	}

	public static void setTime(long time) {
		HealingWaters.time = time;
	}

	public static double getRadius() {
		return getConfig().getDouble("Abilities.Water.HealingWaters.Radius");
	}

	public static long getInterval() {
		return getConfig().getLong("Abilities.Water.HealingWaters.Interval");
	}

	public static int getPower() {
		return getConfig().getInt("Abilities.Water.HealingWaters.Power");
	}
	
	public static int getDuration() {
		// TODO: add a duration config option
		return 70;
	}

	@Override
	public String getName() {
		return "HealingWaters";
	}

	@Override
	public void progress() {
	}

	@Override
	public Location getLocation() {
		return null;
	}

	@Override
	public long getCooldown() {
		return 0;
	}
	
}
