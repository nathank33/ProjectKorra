package com.projectkorra.projectkorra.ability;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public interface Ability {
	
	public void progress();

	public void remove();
	
	//public boolean isSneakAbiliity();
	
	//public boolean isHarmlessAbility();
	
	public boolean isIgniteAbility();
	
	public boolean isExplosiveAbility();
	
	public boolean isHiddenAbility();
	
	public long getCooldown();
	
	public String getName();
	
	public String getDescription();
	
	public String getElementName();
	
	public Location getLocation();
	
	public Player getPlayer();
	
	public ChatColor getElementColor();
}
