package com.projectkorra.projectkorra.ability;

import org.bukkit.entity.Player;

public abstract class SpiritualAbility extends AirAbility implements SubAbility {

	public SpiritualAbility(Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return AirAbility.class;
	}
	
	@Override
	public String getSubElementName() {
		return "Spiritual";
	}
}
