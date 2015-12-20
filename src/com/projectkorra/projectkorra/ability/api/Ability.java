package com.projectkorra.projectkorra.ability.api;

import org.bukkit.entity.Player;

public interface Ability {
	
	public String getName();
	
	public String getDescription();
	
	public Player getPlayer();

	public void progress();

	public void remove();
}
