package com.projectkorra.projectkorra;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.chiblocking.ChiCombo;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.Flight;
import com.projectkorra.projectkorra.util.RevertChecker;
import com.projectkorra.projectkorra.util.TempPotionEffect;
import com.projectkorra.rpg.RPGMethods;
import com.projectkorra.rpg.WorldEvents;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class BendingManager implements Runnable {

	private static BendingManager instance;
	public static HashMap<World, String> events = new HashMap<World, String>(); // holds any current event.

	long time;
	long interval;
	private final HashMap<World, Boolean> times = new HashMap<World, Boolean>(); // true if day time

	public BendingManager() {
		instance = this;
		time = System.currentTimeMillis();
	}

	public static BendingManager getInstance() {
		return instance;
	}

	public void handleCooldowns() {
		for (UUID uuid : BendingPlayer.getPlayers().keySet()) {
			BendingPlayer bPlayer = BendingPlayer.getPlayers().get(uuid);
			for (String abil : bPlayer.getCooldowns().keySet()) {
				if (System.currentTimeMillis() >= bPlayer.getCooldown(abil)) {
					bPlayer.removeCooldown(abil);
				}
			}
		}
	}

	public void handleDayNight() {
		for (World world : Bukkit.getServer().getWorlds()) {
			if (!events.containsKey(world)) {
				events.put(world, "");
			}
		}
		for (World world : Bukkit.getServer().getWorlds()) {
			if (!times.containsKey(world)) {
				if (FireAbility.isDay(world)) {
					times.put(world, true);
				} else {
					times.put(world, false);
				}
			} else {
				if (times.get(world) && !FireAbility.isDay(world)) {
					// The hashmap says it is day, but it is not.
					times.put(world, false); // Sets time to night.
					if (GeneralMethods.hasRPG()) {
						if (RPGMethods.isLunarEclipse(world)) {
							events.put(world, WorldEvents.LunarEclipse.toString());
						} else if (WaterAbility.isFullMoon(world)) {
							events.put(world, "FullMoon");
						} else {
							events.put(world, "");
						}
					} else {
						if (WaterAbility.isFullMoon(world)) {
							events.put(world, "FullMoon");
						} else {
							events.put(world, "");
						}
					}
					for (Player player : world.getPlayers()) {
						BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
						if (!player.hasPermission("bending.message.nightmessage")) {
							continue;
						} else if (bPlayer == null) {
							continue;
						}

						if (bPlayer.hasElement(Element.WATER)) {
							if (GeneralMethods.hasRPG()) {
								if (RPGMethods.isLunarEclipse(world)) {
									player.sendMessage(WaterAbility.getChatColor() + getLunarEclipseMessage());
								} else if (WaterAbility.isFullMoon(world)) {
									player.sendMessage(WaterAbility.getChatColor() + getFullMoonriseMessage());
								} else {
									player.sendMessage(WaterAbility.getChatColor() + getMoonriseMessage());
								}
							} else {
								if (WaterAbility.isFullMoon(world)) {
									player.sendMessage(WaterAbility.getChatColor() + getFullMoonriseMessage());
								} else {
									player.sendMessage(WaterAbility.getChatColor() + getMoonriseMessage());
								}
							}
						}
						if (bPlayer.hasElement(Element.FIRE)) {
							if (!player.hasPermission("bending.message.daymessage"))
								return;
							player.sendMessage(FireAbility.getChatColor() + getSunsetMessage());
						}
					}
				}

				if (!times.get(world) && FireAbility.isDay(world)) {
					// The hashmap says it is night, but it is day.
					times.put(world, true);
					if (GeneralMethods.hasRPG()) {
						if (RPGMethods.isSozinsComet(world)) {
							events.put(world, WorldEvents.SozinsComet.toString());
						} else if (RPGMethods.isSolarEclipse(world) && !RPGMethods.isLunarEclipse(world)) {
							events.put(world, WorldEvents.SolarEclipse.toString());
						} else {
							events.put(world, "");
						}
					} else {
						events.put(world, "");
					}
					for (Player player : world.getPlayers()) {
						BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
						if (bPlayer == null) {
							continue;
						}
						
						if (bPlayer.hasElement(Element.WATER) && player.hasPermission("bending.message.nightmessage")) {
							player.sendMessage(WaterAbility.getChatColor() + getMoonsetMessage());
						}
						if (bPlayer.hasElement(Element.FIRE) && player.hasPermission("bending.message.daymessage")) {
							if (GeneralMethods.hasRPG()) {
								if (RPGMethods.isSozinsComet(world)) {
									player.sendMessage(FireAbility.getChatColor() + getSozinsCometMessage());
								} else if (RPGMethods.isSolarEclipse(world) && !RPGMethods.isLunarEclipse(world)) {
									player.sendMessage(FireAbility.getChatColor() + getSolarEclipseMessage());
								} else {
									player.sendMessage(FireAbility.getChatColor() + getSunriseMessage());
								}
							} else {
								player.sendMessage(FireAbility.getChatColor() + getSunriseMessage());
							}
						}
					}
				}
			}
		}
	}

	public void run() {
		try {
			interval = System.currentTimeMillis() - time;
			time = System.currentTimeMillis();
			ProjectKorra.time_step = interval;

			CoreAbility.progressAll();
			AvatarState.manageAvatarStates();
			TempPotionEffect.progressAll();
			handleDayNight();
			Flight.handle();
			RevertChecker.revertAirBlocks();
			ChiCombo.handleParalysis();
			HorizontalVelocityTracker.updateAll();
			handleCooldowns();
		} catch (Exception e) {
			GeneralMethods.stopBending();
			e.printStackTrace();
		}
	}

	public static String getSozinsCometMessage() {
		return getConfig().getString("Properties.Fire.CometMessage");
	}

	public static String getSolarEclipseMessage() {
		return getConfig().getString("Properties.Fire.SolarEclipseMessage");
	}

	public static String getSunriseMessage() {
		return getConfig().getString("Properties.Fire.DayMessage");
	}

	public static String getSunsetMessage() {
		return getConfig().getString("Properties.Fire.NightMessage");
	}

	public static String getMoonriseMessage() {
		return getConfig().getString("Properties.Water.NightMessage");
	}

	public static String getFullMoonriseMessage() {
		return getConfig().getString("Properties.Water.FullMoonMessage");
	}
	
	public static String getLunarEclipseMessage() {
		return getConfig().getString("Properties.Water.LunarEclipseMessage");
	}

	public static String getMoonsetMessage() {
		return getConfig().getString("Properties.Water.DayMessage");
	}

	private static FileConfiguration getConfig() {
		return ConfigManager.getConfig();
	}
	
}
