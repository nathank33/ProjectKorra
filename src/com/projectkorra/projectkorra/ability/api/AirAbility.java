package com.projectkorra.projectkorra.ability.api;

import com.projectkorra.projectkorra.configuration.ConfigManager;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public abstract class AirAbility extends CoreAbility {

	public AirAbility(Player player) {
		super(player);
	}

	@Override
	public final String getElementName() {
		return "Air";
	}

	/**
	 * Gets the AirColor from the config.
	 * 
	 * @return Config specified ChatColor
	 */
	public static ChatColor getChatColor() {
		return ChatColor.valueOf(ConfigManager.getConfig().getString("Properties.Chat.Colors.Air"));
	}
	
	/**
	 * Gets the AirSubColor from the config.
	 * 
	 * @return Config specified ChatColor
	 */
	public static ChatColor getSubChatColor() {
		return ChatColor.valueOf(ConfigManager.getConfig().getString("Properties.Chat.Colors.AirSub"));
	}

}
