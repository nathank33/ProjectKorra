package com.projectkorra.projectkorra.ability;

import org.bukkit.entity.Player;

public abstract class SandAbility extends EarthAbility implements SubAbility {

	public SandAbility() {}
	
	public SandAbility(Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return EarthAbility.class;
	}
	
	@Override
	public String getSubElementName() {
		return "Sand";
	}
	
}
