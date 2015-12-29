package com.projectkorra.projectkorra.ability.api;

import org.bukkit.entity.Player;

public abstract class PlantAbility extends WaterAbility implements SubAbility {

	public PlantAbility() {}
	
	public PlantAbility(Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return WaterAbility.class;
	}
	
	@Override
	public String getSubElementName() {
		return "Plant";
	}
}
