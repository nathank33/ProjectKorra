package com.projectkorra.projectkorra.ability.api;

import org.bukkit.entity.Player;

public abstract class LavaAbility extends EarthAbility implements SubAbility {

	public LavaAbility() {}
	
	public LavaAbility(Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return EarthAbility.class;
	}
	
	@Override
	public String getSubElementName() {
		return "Lava";
	}
	
}
