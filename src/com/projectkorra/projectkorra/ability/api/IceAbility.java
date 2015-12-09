package com.projectkorra.projectkorra.ability.api;

import org.bukkit.entity.Player;

public abstract class IceAbility extends WaterAbility implements SubAbility {

	public IceAbility() {}
	
	public IceAbility(Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return WaterAbility.class;
	}
	
	@Override
	public String getSubElementName() {
		return "Ice";
	}
}
