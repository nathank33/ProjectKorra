package com.projectkorra.projectkorra.avatar;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AvatarAbility;
import com.projectkorra.projectkorra.util.Flight;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class AvatarState extends AvatarAbility {

	public static ConcurrentHashMap<Player, AvatarState> instances = new ConcurrentHashMap<Player, AvatarState>();
	public static Map<String, Long> startTimes = new HashMap<String, Long>();

	public static FileConfiguration config = ProjectKorra.plugin.getConfig();
	private static final long cooldown = ProjectKorra.plugin.getConfig().getLong("Abilities.AvatarState.Cooldown");
	private static boolean regenEnabled = config.getBoolean("Abilities.AvatarState.PotionEffects.Regeneration.Enabled");
	private static int regenPower = config.getInt("Abilities.AvatarState.PotionEffects.Regeneration.Power") - 1;
	private static boolean speedEnabled = config.getBoolean("Abilities.AvatarState.PotionEffects.Speed.Enabled");
	private static int speedPower = config.getInt("Abilities.AvatarState.PotionEffects.Speed.Power") - 1;
	private static boolean resistanceEnabled = config.getBoolean("Abilities.AvatarState.PotionEffects.DamageResistance.Enabled");
	private static int resistancePower = config.getInt("Abilities.AvatarState.PotionEffects.DamageResistance.Power") - 1;
	private static boolean fireResistanceEnabled = config.getBoolean("Abilities.AvatarState.PotionEffects.FireResistance.Enabled");
	private static int fireResistancePower = config.getInt("Abilities.AvatarState.PotionEffects.FireResistance.Power") - 1;
	private static long duration = config.getLong("Abilities.AvatarState.Duration");
	
	public static final double factor = config.getDouble("Abilities.AvatarState.PowerMultiplier");
	
	public AvatarState(Player player) {
		super(player);
		
		if (instances.containsKey(player)) {
			instances.remove(player);
			return;
		}

		if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
		new Flight(player);
		GeneralMethods.playAvatarSound(player.getLocation());
		instances.put(player, this);
		bPlayer.addCooldown(this);
		if (duration != 0) {
			startTimes.put(player.getName(), System.currentTimeMillis());
		}
	}

	public static void manageAvatarStates() {
		for (Player player : instances.keySet()) {
			progress(player);
		}
	}

	public static void progress(Player player) {
		instances.get(player).progress();
	}

	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			instances.remove(player);
			return;
		}
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			instances.remove(player);
			if (player != null) {
				if (bPlayer.isOnCooldown(this)) {
					bPlayer.removeCooldown(this);
				}
				return;
			}
		}

		if (startTimes.containsKey(player.getName())) {
			if (startTimes.get(player.getName()) + duration < System.currentTimeMillis()) {
				startTimes.remove(player.getName());
				instances.remove(player);
			}
		}

		addPotionEffects();
	}

	private void addPotionEffects() {
		int duration = 70;
		if (regenEnabled) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, duration, regenPower));
		}
		if (speedEnabled) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, duration, speedPower));
		}
		if (resistanceEnabled) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, duration, resistancePower));
		}
		if (fireResistanceEnabled) {
			player.addPotionEffect(new PotionEffect(PotionEffectType.FIRE_RESISTANCE, duration, fireResistancePower));
		}
	}

	public static boolean isAvatarState(Player player) {
		if (instances.containsKey(player))
			return true;
		return false;
	}

	public static double getValue(double value) {
		return factor * value;
	}

	public static int getValue(int value) {
		return (int) factor * value;
	}
	
	public static double getValue(double value, Player player) {
		return isAvatarState(player) ? AvatarState.getValue(value) : value;
	}

	public static ArrayList<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<Player>();
		for (Player player : instances.keySet()) {
			players.add(player);
		}
		return players;
	}

	@Override
	public String getName() {
		return "AvatarState";
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	/*@Override
	public boolean isSneakAbiliity() {
		return false;
	}

	@Override
	public boolean isHarmlessAbility() {
		return true;
	}*/
}
