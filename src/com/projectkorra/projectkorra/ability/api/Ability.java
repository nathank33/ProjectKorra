package com.projectkorra.projectkorra.ability.api;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface Ability {
	
	public void progress();

	public void remove();
	
	public long getCooldown();
	
	public String getName();
	
	public String getDescription();
	
	public String getElementName();
	
	public Location getLocation();
	
	public Player getPlayer();
	
	public ChatColor getElementColor();
}
