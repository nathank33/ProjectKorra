package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AvatarState;
import com.projectkorra.projectkorra.ability.api.IceAbility;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.ConcurrentHashMap;

public class PhaseChangeFreeze extends IceAbility {

	private static final ConcurrentHashMap<Block, Byte> FROZEN_BLOCKS = new ConcurrentHashMap<>();
	private static final double REMOVE_RANGE = 50; // TODO: Make the remove range non static
	
	private static boolean overloading = false;
	private static int overloadingLimit = 0;
	private static int overloadCounter = 200;
	
	private double range;
	private double radius;
	private long cooldown;
	private Location location;
	
	public PhaseChangeFreeze() {
	}

	public PhaseChangeFreeze(Player player) {
		super(player);
		
		this.range = getConfig().getDouble("Abilities.Water.PhaseChange.Range");
		this.radius = getConfig().getDouble("Abilities.Water.PhaseChange.Radius");
		this.cooldown = 0;
		
		this.range = getNightFactor(range);
		this.radius = getNightFactor(radius);
		
		if (!bPlayer.canBend(this) || !bPlayer.canIcebend()) {
			return;
		}

		if (bPlayer.isAvatarState()) {
			range = AvatarState.getValue(range);
		}

		location = GeneralMethods.getTargetedLocation(player, range);
		start();
		
		for (Block block : GeneralMethods.getBlocksAroundPoint(location, radius)) {
			if (isFreezable(player, block)) {
				freeze(player, block);
			}
		}

		bPlayer.addCooldown(this);
		remove();
	}

	private static boolean isFreezable(Player player, Block block) {
		if (GeneralMethods.isRegionProtectedFromBuild(player, "PhaseChange", block.getLocation())) {
			return false;
		}
		return isWater(block) && WaterManipulation.canPhysicsChange(block) && !TempBlock.isTempBlock(block);
	}

	@SuppressWarnings("deprecation")
	public static void freeze(Player player, Block block) {
		if (GeneralMethods.isRegionProtectedFromBuild(player, "PhaseChange", block.getLocation())) {
			return;
		} else if (TempBlock.isTempBlock(block)) {
			return;
		}
		
		byte data = block.getData();
		block.setType(Material.ICE);
		
		if(FROZEN_BLOCKS.size() % 50 == 0) {
			playIcebendingSound(block.getLocation());
		}
		FROZEN_BLOCKS.put(block, data);
	}

	@SuppressWarnings("deprecation")
	public static void thaw(Block block) {
		if (FROZEN_BLOCKS.containsKey(block)) {
			byte data = FROZEN_BLOCKS.get(block);
			FROZEN_BLOCKS.remove(block);
			block.setType(Material.WATER);
			block.setData(data);
		}
	}

	public static void handleFrozenBlocks() {
		int size = FROZEN_BLOCKS.keySet().size();
		overloadCounter++;
		overloadCounter %= 10;
		if (overloadCounter == 0) {
			overloading = size > overloadingLimit ? true : false;
		}

		// We only want to run this method once every 10 ticks if we are overloading.
		if (overloading && overloadCounter != 0) {
			return;
		}

		if (overloading) {
			int i = 0;
			for (Block block : FROZEN_BLOCKS.keySet()) {
				final Block fblock = block;
				new BukkitRunnable() {
					@Override
					public void run() {
						if (canThaw(fblock)) {
							thaw(fblock);
						}
					}
				}.runTaskLater(ProjectKorra.plugin, i % 10);
				i++;
			}
		} else {
			for (Block block : FROZEN_BLOCKS.keySet()) {
				if (canThaw(block)) {
					thaw(block);
				}
			}
		}
	}

	public static boolean canThaw(Block block) {
		if (FROZEN_BLOCKS.containsKey(block)) {
			for (Player player : block.getWorld().getPlayers()) {
				BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
				if (bPlayer == null || !player.isOnline()) {
					return true;
				} else if (bPlayer.getBoundAbilityName() == null) {
					return true;
				}
				
				if (bPlayer.getBoundAbilityName().equalsIgnoreCase("OctopusForm")) {
					if (block.getLocation().distance(player.getLocation()) <= REMOVE_RANGE + 2) {
						return false;
					}
				}
				
				if (GeneralMethods.canBend(player.getName(), "PhaseChange")) {
					double range = getNightFactor(REMOVE_RANGE, player.getWorld());
					if (AvatarState.isAvatarState(player)) {
						range = AvatarState.getValue(range);
					}
					if (block.getLocation().distanceSquared(player.getLocation()) <= range * range) {
						return false;
					}
				}
			}
		}
		
		if (!WaterManipulation.canPhysicsChange(block)) {
			return false;
		}
		return true;
	}

	@SuppressWarnings("deprecation")
	public static void removeAllCleanup() {
		for (Block block : FROZEN_BLOCKS.keySet()) {
			if (block.getType() == Material.ICE) {
				byte data = FROZEN_BLOCKS.get(block);
				block.setType(Material.WATER);
				block.setData(data);
				FROZEN_BLOCKS.remove(block);
			}
		}
	}

	@Override
	public String getName() {
		return "PhaseChange";
	}

	@Override
	public void progress() {}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	public static boolean isOverloading() {
		return overloading;
	}

	public static void setOverloading(boolean overloading) {
		PhaseChangeFreeze.overloading = overloading;
	}

	public static int getOverloadingLimit() {
		return overloadingLimit;
	}

	public static void setOverloadingLimit(int overloadingLimit) {
		PhaseChangeFreeze.overloadingLimit = overloadingLimit;
	}

	public static int getOverloadCounter() {
		return overloadCounter;
	}

	public static void setOverloadCounter(int overloadCounter) {
		PhaseChangeFreeze.overloadCounter = overloadCounter;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public static ConcurrentHashMap<Block, Byte> getFrozenBlocks() {
		return FROZEN_BLOCKS;
	}

	public static double getRemoveRange() {
		return REMOVE_RANGE;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(Location location) {
		this.location = location;
	}
	
}
