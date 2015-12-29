package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AbilityModuleManager;
import com.projectkorra.projectkorra.ability.api.CoreAbility;
import com.projectkorra.projectkorra.ability.api.WaterAbility;
import com.projectkorra.projectkorra.earthbending.EarthArmor;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

public class WaterPassive {

	public static boolean applyNoFall(Player player) {
		Block block = player.getLocation().getBlock();
		Block fallBlock = block.getRelative(BlockFace.DOWN);
		if (TempBlock.isTempBlock(fallBlock) && (fallBlock.getType().equals(Material.ICE))) {
			return true;
		} else if (WaterAbility.isWaterbendable(block, player) && !WaterAbility.isPlant(block)) {
			return true;
		} else if (fallBlock.getType() == Material.AIR) {
			return true;
		} else if ((WaterAbility.isWaterbendable(fallBlock, player) && !WaterAbility.isPlant(fallBlock)) || fallBlock.getType() == Material.SNOW_BLOCK) {
			return true;
		}
		return false;
	}

	public static void handlePassive() {
		double swimSpeed = ProjectKorra.plugin.getConfig().getDouble("Abilities.Water.Passive.SwimSpeedFactor");
		
		for (Player player : Bukkit.getServer().getOnlinePlayers()) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			if (bPlayer == null) {
				continue;
			}
			
			String ability = bPlayer.getBoundAbilityName();
			if (GeneralMethods.canBendPassive(player.getName(), Element.Water)) {
				if (CoreAbility.hasAbility(player, WaterSpout.class) || CoreAbility.hasAbility(player, EarthArmor.class)) {
					continue;
				} else if (!AbilityModuleManager.shiftabilities.contains(ability)) {
					if (player.isSneaking() && WaterAbility.isWater(player.getLocation().getBlock())) {
						player.setVelocity(player.getEyeLocation().getDirection().clone().normalize().multiply(swimSpeed));
					}
				}
			}
		}
	}
}
