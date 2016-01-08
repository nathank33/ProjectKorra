package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.Element;

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
	public final Element getElement() {
		return Element.AVATAR;
	}

	public static ChatColor getChatColor() {
		return ChatColor.valueOf(getConfig().getString("Properties.Chat.Colors.Avatar"));
	}

}
