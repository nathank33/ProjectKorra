package com.projectkorra.projectkorra.ability.api;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.earthbending.LavaFlow;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashSet;

// Contains methods that all 5 elements should be capable of accessing
public abstract class ElementalAbility extends CoreAbility {
	
	private static final HashSet<Block> PREVENT_EARTHBENDING = new HashSet<Block>();
	private static final Integer[] TRANSPARENT_MATERIAL = { 0, 6, 8, 9, 10, 11, 30, 31, 32, 37, 38, 39, 40, 50, 51, 59, 78, 83, 106, 175 };
	
	public ElementalAbility() {}
	
	public ElementalAbility(Player player) {
		super(player);
	}
	
	public boolean isEarthbendable(Block block) {
		return isEarthbendable(player, getName(), block);
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

	public boolean isMetalbendable(Block block) {
		return isMetalbendable(block.getType());
	}
	
	public boolean isMetalbendable(Material material) {
		return isMetalbendable(player, material);
	}
	
	public static boolean isMetalbendable(Player player, Material material) {
		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
		return bPlayer == null ? null : isMetal(material) && bPlayer.canMetalbend();
	}

	public boolean isTransparentToEarthbending(Block block) {
		return isTransparentToEarthbending(player, getName(), block);
	}
	
	public static boolean isTransparentToEarthbending(Player player, Block block) {
		return isTransparentToEarthbending(player, null, block);
	}

	@SuppressWarnings("deprecation")
	public static boolean isTransparentToEarthbending(Player player, String abilityName, Block block) {
		return Arrays.asList(TRANSPARENT_MATERIAL).contains(block.getTypeId())
				&& !GeneralMethods.isRegionProtectedFromBuild(player, abilityName, block.getLocation());
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

	public static boolean isEarthbendable(Material material) {
		return getConfig().getStringList("Properties.Earth.EarthbendableBlocks").contains(material.toString());
	}

	public static boolean isEarthRevertOn() {
		return getConfig().getBoolean("Properties.Earth.RevertEarthbending");
	}

	public static boolean isLava(Block block) {
		return block != null ? isLava(block.getType()) : false;
	}
	
	public static boolean isLava(Material material) {
		return material == Material.LAVA || material == Material.STATIONARY_LAVA;
	}
	
	public static boolean isMetal(Block block) {
		return block != null ? isMetal(block.getType()) : false;
	}
	
	public static boolean isMetal(Material material) {
		return getConfig().getStringList("Properties.Earth.MetalBlocks").contains(material.toString());
	}
	
	public static boolean isMetalBlock(Block block) {
		if (block.getType() == Material.GOLD_BLOCK || block.getType() == Material.IRON_BLOCK
				|| block.getType() == Material.IRON_ORE || block.getType() == Material.GOLD_ORE
				|| block.getType() == Material.QUARTZ_BLOCK || block.getType() == Material.QUARTZ_ORE) {
			return true;
		}
		return false;
	}
	
	public static HashSet<Block> getPreventEarthbendingBlocks() {
		return PREVENT_EARTHBENDING;
	}
	
}
