package com.projectkorra.projectkorra.airbending;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.ConfigManager;

import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ConcurrentHashMap;

public class AirPassive {

	private static final ConcurrentHashMap<Player, Float> FOOD = new ConcurrentHashMap<Player, Float>();

	public static float getExhaustion(Player player, float level) {
		if (!FOOD.keySet().contains(player)) {
			FOOD.put(player, level);
			return level;
		} else {
			float oldlevel = FOOD.get(player);
			if (level < oldlevel) {
				level = 0;
			} else {
				float factor = (float) ConfigManager.getConfig().getDouble("Abilities.Air.Passive.Factor");
				level = (level - oldlevel) * factor + oldlevel;
			}
			FOOD.replace(player, level);
			return level;
		}
	}

	public static void handlePassive(Server server) {
		for (World world : server.getWorlds()) {
			for (Player player : world.getPlayers()) {
				if (!player.isOnline()) {
					return;
				}
				
				if (GeneralMethods.canBendPassive(player.getName(), Element.Air)) {
					player.setExhaustion(getExhaustion(player, player.getExhaustion()));
					if (player.isSprinting()) {
						if (!player.hasPotionEffect(PotionEffectType.SPEED)) {
							int speedPower = ConfigManager.getConfig().getInt("Abilities.Air.Passive.Speed");
							player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 60, speedPower - 1));
						}
						if (!player.hasPotionEffect(PotionEffectType.JUMP)) {
							int jumpPower = ConfigManager.getConfig().getInt("Abilities.Air.Passive.Jump");
							player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, 60, jumpPower - 1));
						}
					}
				}
			}
		}
	}
}
