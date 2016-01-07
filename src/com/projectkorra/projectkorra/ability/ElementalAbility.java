package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.earthbending.LavaFlow;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.HashSet;

// Contains methods that all 5 elements should be capable of accessing
public abstract class ElementalAbility extends CoreAbility {
	
	private static final HashSet<Block> PREVENT_EARTHBENDING = new HashSet<Block>();
	private static final Integer[] TRANSPARENT_MATERIAL = { 0, 6, 8, 9, 10, 11, 30, 31, 32, 37, 38, 39, 40, 50, 51, 59, 78, 83, 106, 175 };
	private static final Integer[] PLANT_IDS = { 6, 18, 31, 37, 38, 39, 40, 59, 81, 83, 86, 99, 100, 103, 104, 105, 106, 111, 161, 175 };
	private static final PotionEffectType[] POSITIVE_EFFECTS = {PotionEffectType.ABSORPTION, PotionEffectType.DAMAGE_RESISTANCE, PotionEffectType.FAST_DIGGING, 
				PotionEffectType.FIRE_RESISTANCE, PotionEffectType.HEAL, PotionEffectType.HEALTH_BOOST, PotionEffectType.INCREASE_DAMAGE, PotionEffectType.JUMP, 
				PotionEffectType.NIGHT_VISION, PotionEffectType.REGENERATION, PotionEffectType.SATURATION, PotionEffectType.SPEED, PotionEffectType.WATER_BREATHING};
	private static final PotionEffectType[] NEUTRAL_EFFECTS = {PotionEffectType.INVISIBILITY};
	private static final PotionEffectType[] NEGATIVE_EFFECTS = {PotionEffectType.POISON, PotionEffectType.BLINDNESS, PotionEffectType.CONFUSION, 
				PotionEffectType.HARM, PotionEffectType.HUNGER, PotionEffectType.SLOW, PotionEffectType.SLOW_DIGGING, PotionEffectType.WEAKNESS, PotionEffectType.WITHER};
	
	public ElementalAbility() {}
	
	public ElementalAbility(Player player) {
		super(player);
	}
	
	public boolean isEarthbendable(Block block) {
		return isEarthbendable(player, getName(), block);
	}
	
	public boolean isMetalbendable(Block block) {
		return isMetalbendable(block.getType());
	}
	
	public boolean isMetalbendable(Material material) {
		return isMetalbendable(player, material);
	}

	public boolean isTransparentToEarthbending(Block block) {
		return isTransparentToEarthbending(player, getName(), block);
	}

	public static HashSet<Block> getPreventEarthbendingBlocks() {
		return PREVENT_EARTHBENDING;
	}
	
	public static Integer[] getTransparentMaterial() {
		return TRANSPARENT_MATERIAL;
	}
	
	public static HashSet<Byte> getTransparentMaterialSet() {
		HashSet<Byte> set = new HashSet<Byte>();
		for (int i : TRANSPARENT_MATERIAL) {
			set.add((byte) i);
		}
		return set;
	}
	
	public static boolean isDay(World world) {
		long time = world.getTime();
		if (world.getEnvironment() == Environment.NETHER || world.getEnvironment() == Environment.THE_END) {
			return true;
		}
		if (time >= 23500 || time <= 12500) {
			return true;
		}
		return false;
	}

	public static boolean isEarthbendable(Material material) {
		return getConfig().getStringList("Properties.Earth.EarthbendableBlocks").contains(material.toString());
	}
	
	public static boolean isEarthbendable(Player player, Block block) {
		return isEarthbendable(player, null, block);
	}

	public static boolean isEarthbendable(Player player, String abilityName, Block block) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		if (bPlayer == null || !isEarthbendable(block.getType()) || PREVENT_EARTHBENDING.contains(block)
				|| GeneralMethods.isRegionProtectedFromBuild(player, abilityName, block.getLocation())) {
			return false;
		} else if (isMetal(block) && !bPlayer.canMetalbend()) {
			return false;
		}
		return true;
	}

	public static boolean isEarthRevertOn() {
		return getConfig().getBoolean("Properties.Earth.RevertEarthbending");
	}
	
	public static boolean isFullMoon(World world) {
		long days = world.getFullTime() / 24000;
		long phase = days % 8;
		if (phase == 0) {
			return true;
		}
		return false;
	}
	
	public static boolean isIce(Block block) {
		return isIce(block.getType());
	}
	
	public static boolean isIce(Material material) {
		return material == Material.ICE || material == Material.PACKED_ICE;
	}

	public static boolean isLava(Block block) {
		return block != null ? isLava(block.getType()) : false;
	}

	public static boolean isLava(Material material) {
		return material == Material.LAVA || material == Material.STATIONARY_LAVA;
	}

	@SuppressWarnings("deprecation")
	public static boolean isLavabendable(Block block) {
		byte full = 0x0;
		if (TempBlock.isTempBlock(block)) {
			TempBlock tblock = TempBlock.instances.get(block);
			if (tblock == null || !LavaFlow.getTempLavaBlocks().contains(tblock)) {
				return false;
			}
		}
		if (isLava(block) && block.getData() == full) {
			return true;
		}
		return false;
	}
	
	public static boolean isMeltable(Block block) {
		if (block.getType() == Material.ICE || block.getType() == Material.SNOW) {
			return true;
		}
		return false;
	}
	
	public static boolean isMetal(Block block) {
		return block != null ? isMetal(block.getType()) : false;
	}
	
	public static boolean isMetal(Material material) {
		return getConfig().getStringList("Properties.Earth.MetalBlocks").contains(material.toString());
	}
	
	public static boolean isMetalbendable(Player player, Material material) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		return bPlayer == null ? null : isMetal(material) && bPlayer.canMetalbend();
	}
	
	public static boolean isMetalBlock(Block block) {
		if (block.getType() == Material.GOLD_BLOCK || block.getType() == Material.IRON_BLOCK
				|| block.getType() == Material.IRON_ORE || block.getType() == Material.GOLD_ORE
				|| block.getType() == Material.QUARTZ_BLOCK || block.getType() == Material.QUARTZ_ORE) {
			return true;
		}
		return false;
	}
	
	public static boolean isNegativeEffect(PotionEffectType effect) {
		for (PotionEffectType effect2 : NEGATIVE_EFFECTS) {
			if (effect2.equals(effect)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isNeutralEffect(PotionEffectType effect) {
		for (PotionEffectType effect2 : NEUTRAL_EFFECTS) {
			if (effect2.equals(effect)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isNight(World world) {
		if (world.getEnvironment() == Environment.NETHER || world.getEnvironment() == Environment.THE_END) {
			return false;
		}

		long time = world.getTime();
		if (time >= 12950 && time <= 23050) {
			return true;
		}
		return false;
	}
	
	@SuppressWarnings("deprecation")
	public static boolean isPlant(Block block) {
		if (block == null) {
			return false;
		} else if (Arrays.asList(PLANT_IDS).contains(block.getTypeId())) {
			return true;
		}
		return false;
	}

	public static boolean isPositiveEffect(PotionEffectType effect) {
		for (PotionEffectType effect2 : POSITIVE_EFFECTS) {
			if (effect2.equals(effect)) {
				return true;
			}
		}
		return false;
	}

	public static boolean isTransparentToEarthbending(Player player, Block block) {
		return isTransparentToEarthbending(player, null, block);
	}

	@SuppressWarnings("deprecation")
	public static boolean isTransparentToEarthbending(Player player, String abilityName, Block block) {
		return Arrays.asList(TRANSPARENT_MATERIAL).contains(block.getTypeId())
				&& !GeneralMethods.isRegionProtectedFromBuild(player, abilityName, block.getLocation());
	}
	
	public static boolean isUndead(Entity entity) {
		if (entity == null) {
			return false;
		} else if (entity.getType() == EntityType.ZOMBIE || entity.getType() == EntityType.BLAZE 
				|| entity.getType() == EntityType.GIANT || entity.getType() == EntityType.IRON_GOLEM 
				|| entity.getType() == EntityType.MAGMA_CUBE || entity.getType() == EntityType.PIG_ZOMBIE 
				|| entity.getType() == EntityType.SKELETON || entity.getType() == EntityType.SLIME 
				|| entity.getType() == EntityType.SNOWMAN || entity.getType() == EntityType.ZOMBIE) {
			return true;
		}
		return false;
	}

	public static boolean isWater(Block block) {
		return block != null ? isWater(block.getType()) : null;
	}
	
	public static boolean isWater(Material material) {
		return material == Material.WATER || material == Material.STATIONARY_WATER;
	}
	
}
