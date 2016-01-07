package com.projectkorra.projectkorra.ability;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public abstract class AvatarAbility extends ElementalAbility {
	
	public AvatarAbility(Player player) {
		super(player);
	}
	
	@Override
	public boolean isIgniteAbility() {
		return false;
	}
	
	@Override
	public boolean isExplosiveAbility() {
		return false;
	}

	@Override
	public final String getElementName() {
		return "Avatar";
	}

	public static ChatColor getChatColor() {
		return ChatColor.valueOf(getConfig().getString("Properties.Chat.Colors.Avatar"));
	}

}
