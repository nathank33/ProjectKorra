package com.projectkorra.projectkorra.ability;

import com.projectkorra.projectkorra.Element;

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
	
	public Element getElement();
	
	public Location getLocation();
	
	public Player getPlayer();

}
