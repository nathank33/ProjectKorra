package com.projectkorra.projectkorra.ability.api;

import org.bukkit.entity.Player;

public abstract class BloodAbility extends WaterAbility implements SubAbility {

	public BloodAbility() {}
	
	public BloodAbility(Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return WaterAbility.class;
	}
	
	@Override
	public String getSubElementName() {
		return "Blood";
	}
}
