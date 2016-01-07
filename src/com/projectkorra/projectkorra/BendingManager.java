package com.projectkorra.projectkorra;

import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.ability.WaterAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.chiblocking.ChiCombo;
import com.projectkorra.projectkorra.configuration.ConfigLoadable;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.Flight;
import com.projectkorra.projectkorra.util.RevertChecker;
import com.projectkorra.projectkorra.util.TempPotionEffect;
import com.projectkorra.rpg.RPGMethods;
import com.projectkorra.rpg.WorldEvents;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class BendingManager implements Runnable, ConfigLoadable {

	private static BendingManager instance;

	public static HashMap<World, String> events = new HashMap<World, String>(); // holds any current event.

	private static String sozinsCometMessage = config.get().getString("Properties.Fire.CometMessage");
	private static String solarEclipseMessage = config.get().getString("Properties.Fire.SolarEclipseMessage");

	private static String sunriseMessage = config.get().getString("Properties.Fire.DayMessage");
	private static String sunsetMessage = config.get().getString("Properties.Fire.NightMessage");

	private static String moonriseMessage = config.get().getString("Properties.Water.NightMessage");
	private static String fullMoonriseMessage = config.get().getString("Properties.Water.FullMoonMessage");
	private static String lunarEclipseMessage = config.get().getString("Properties.Water.LunarEclipseMessage");
	private static String moonsetMessage = config.get().getString("Properties.Water.DayMessage");

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

						if (!player.hasPermission("bending.message.nightmessage"))
							return;

						if (GeneralMethods.isBender(player.getName(), Element.Water)) {
							if (GeneralMethods.hasRPG()) {
								if (RPGMethods.isLunarEclipse(world)) {
									player.sendMessage(WaterAbility.getChatColor() + lunarEclipseMessage);
								} else if (WaterAbility.isFullMoon(world)) {
									player.sendMessage(WaterAbility.getChatColor() + fullMoonriseMessage);
								} else {
									player.sendMessage(WaterAbility.getChatColor() + moonriseMessage);
								}
							} else {
								if (WaterAbility.isFullMoon(world)) {
									player.sendMessage(WaterAbility.getChatColor() + fullMoonriseMessage);
								} else {
									player.sendMessage(WaterAbility.getChatColor() + moonriseMessage);
								}
							}
						}
						if (GeneralMethods.isBender(player.getName(), Element.Fire)) {
							if (!player.hasPermission("bending.message.daymessage"))
								return;
							player.sendMessage(FireAbility.getChatColor() + sunsetMessage);
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
						if (GeneralMethods.isBender(player.getName(), Element.Water) && player.hasPermission("bending.message.nightmessage")) {
							player.sendMessage(WaterAbility.getChatColor() + moonsetMessage);
						}
						if (GeneralMethods.isBender(player.getName(), Element.Fire) && player.hasPermission("bending.message.daymessage")) {
							if (GeneralMethods.hasRPG()) {
								if (RPGMethods.isSozinsComet(world)) {
									player.sendMessage(FireAbility.getChatColor() + sozinsCometMessage);
								} else if (RPGMethods.isSolarEclipse(world) && !RPGMethods.isLunarEclipse(world)) {
									player.sendMessage(FireAbility.getChatColor() + solarEclipseMessage);
								} else {
									player.sendMessage(FireAbility.getChatColor() + sunriseMessage);
								}
							} else {
								player.sendMessage(FireAbility.getChatColor() + sunriseMessage);
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
		}
		catch (Exception e) {
			GeneralMethods.stopBending();
			e.printStackTrace();
		}
	}

	@Override
	public void reloadVariables() {
		sozinsCometMessage = config.get().getString("Properties.Fire.CometMessage");
		solarEclipseMessage = config.get().getString("Properties.Fire.SolarEclipseMessage");

		sunriseMessage = config.get().getString("Properties.Fire.DayMessage");
		sunsetMessage = config.get().getString("Properties.Fire.NightMessage");

		moonriseMessage = config.get().getString("Properties.Water.NightMessage");
		fullMoonriseMessage = config.get().getString("Properties.Water.FullMoonMessage");
		lunarEclipseMessage = config.get().getString("Properties.Water.LunarEclipsetMessage");
		moonsetMessage = config.get().getString("Properties.Water.DayMessage");
	}

}
