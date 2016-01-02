package com.projectkorra.projectkorra.ability.api;

import com.projectkorra.projectkorra.configuration.ConfigManager;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public abstract class ChiAbility extends ElementalAbility {

	public ChiAbility() {}
	
	public ChiAbility(Player player) {
		super(player);
	}

	@Override
	public final String getElementName() {
		return "Chi";
	}

	/**
	 * Gets the AirColor from the config.
	 * 
	 * @return Config specified ChatColor
	 */
	public static ChatColor getChatColor() {
		return ChatColor.valueOf(ConfigManager.getConfig().getString("Properties.Chat.Colors.Chi"));
	}
	
	/**
	 * Gets the AirSubColor from the config.
	 * 
	 * @return Config specified ChatColor
	 */
	public static ChatColor getSubChatColor() {
		return ChatColor.valueOf(ConfigManager.getConfig().getString("Properties.Chat.Colors.Chi"));
	}
	
}
