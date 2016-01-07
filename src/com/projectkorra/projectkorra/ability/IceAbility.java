package com.projectkorra.projectkorra.ability;

import org.bukkit.entity.Player;

public abstract class IceAbility extends WaterAbility implements SubAbility {

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
