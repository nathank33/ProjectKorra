package com.projectkorra.projectkorra.ability.api;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public abstract class AvatarAbility extends ElementalAbility {

	public AvatarAbility() {
	}
	
	public AvatarAbility(Player player) {
		super(player);
	}

	@Override
	public final String getElementName() {
		return "Avatar";
	}

	public static ChatColor getChatColor() {
		return ChatColor.valueOf(getConfig().getString("Properties.Chat.Colors.Avatar"));
	}

}
