package com.projectkorra.projectkorra.airbending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Server;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.api.AirAbility;
import com.projectkorra.projectkorra.ability.api.CoreAbility;
import com.projectkorra.projectkorra.waterbending.WaterManipulation;
import com.projectkorra.projectkorra.waterbending.WaterMethods;

// TODO: Split WaterBubble into its own class
public class AirBubble extends AirAbility {

	private double radius;
	private double airRadius;
	private double waterRadius;
	private ConcurrentHashMap<Block, BlockState> waterOrigins;
	
	public AirBubble() {}

	public AirBubble(Player player) {
		super(player);
		this.radius = 0;
		this.airRadius = getConfig().getDouble("Abilities.Air.AirBubble.Radius");
		this.waterRadius = getConfig().getDouble("Abilities.Air.AirBubble.Radius");
		this.waterOrigins = new ConcurrentHashMap<>();
		start();
	}

	public static boolean canFlowTo(Block block) {
		for (AirBubble airBubble : CoreAbility.getAbilities(AirBubble.class)) {
			if (airBubble.blockInBubble(block)) {
				return false;
			}
		}
		return true;
	}

	public static void handleBubbles(Server server) {
		for (Player player : server.getOnlinePlayers()) {
			if (GeneralMethods.getBoundAbility(player) != null) {
				if (GeneralMethods.getBoundAbility(player).equalsIgnoreCase("AirBubble")
						|| GeneralMethods.getBoundAbility(player).equalsIgnoreCase("WaterBubble")) {
					if (!CoreAbility.hasAbility(player, AirBubble.class) && player.isSneaking()) {
						new AirBubble(player);
					}
				}
			}
		}
	}

	public boolean blockInBubble(Block block) {
		if (block.getWorld() != player.getWorld()) {
			return false;
		} else if (block.getLocation().distanceSquared(player.getLocation()) <= radius * radius) {
			return true;
		}
		return false;
	}

	@Override
	public void progress() {
		if (!player.isSneaking()) {
			remove();
			return;
		} 
		if (bPlayer.canBend(this)) {
			pushWater();
			return;
		} else if (GeneralMethods.getBoundAbility(player).equalsIgnoreCase("WaterBubble")
				&& GeneralMethods.canBend(player.getName(), "WaterBubble")) {
			pushWater();
			return;
		}
		remove();
		return;
	}

	private void pushWater() {
		if (GeneralMethods.isBender(player.getName(), Element.Air)) {
			radius = airRadius;
		} else {
			radius = waterRadius;
		}
		if (GeneralMethods.isBender(player.getName(), Element.Water) && WaterMethods.isNight(player.getWorld())) {
			radius = WaterMethods.waterbendingNightAugment(waterRadius, player.getWorld());
		}
		if (airRadius > radius && GeneralMethods.isBender(player.getName(), Element.Air)) {
			radius = airRadius;
		}

		Location location = player.getLocation();

		for (Block block : waterOrigins.keySet()) {
			if (block.getWorld() != location.getWorld()) {
				if (block.getType() == Material.AIR || WaterMethods.isWater(block)) {
					waterOrigins.get(block).update(true);
				}
				waterOrigins.remove(block);
			} else if (block.getLocation().distanceSquared(location) > radius * radius) {
				if (block.getType() == Material.AIR || WaterMethods.isWater(block)) {
					waterOrigins.get(block).update(true);
				}
				waterOrigins.remove(block);
			}
		}

		for (Block block : GeneralMethods.getBlocksAroundPoint(location, radius)) {
			if (waterOrigins.containsKey(block)) {
				continue;
			} else if (!WaterMethods.isWater(block)) {
				continue;
			} else if (GeneralMethods.isRegionProtectedFromBuild(player, "AirBubble", block.getLocation())) {
				continue;
			} else if (block.getType() == Material.STATIONARY_WATER || block.getType() == Material.WATER) {
				if (WaterManipulation.canBubbleWater(block)) {
					waterOrigins.put(block, block.getState());
					block.setType(Material.AIR);
				}
			}
		}
	}

	@Override
	public void remove() {
		super.remove();
		for (Block block : waterOrigins.keySet()) {
			if (block.getType() == Material.AIR || block.isLiquid()) {
				waterOrigins.get(block).update(true);
			}
		}
	}

	@Override
	public String getName() {
		return "AirBubble";
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getAirRadius() {
		return airRadius;
	}

	public void setAirRadius(double airRadius) {
		this.airRadius = airRadius;
	}

	public double getWaterRadius() {
		return waterRadius;
	}

	public void setWaterRadius(double waterRadius) {
		this.waterRadius = waterRadius;
	}

	public ConcurrentHashMap<Block, BlockState> getWaterOrigins() {
		return waterOrigins;
	}

}
