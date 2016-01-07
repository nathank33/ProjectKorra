package com.projectkorra.projectkorra.ability;

import org.bukkit.entity.Player;

public abstract class CombustionAbility extends FireAbility implements SubAbility {

	public CombustionAbility() {}
	
	public CombustionAbility(Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return FireAbility.class;
	}
	
	@Override
	public String getSubElementName() {
		return "Combustion";
	}
	
}
