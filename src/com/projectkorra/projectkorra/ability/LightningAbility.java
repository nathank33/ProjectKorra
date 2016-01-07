package com.projectkorra.projectkorra.ability;

import org.bukkit.entity.Player;

public abstract class LightningAbility extends FireAbility implements SubAbility {

	public LightningAbility() {}
	
	public LightningAbility(Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return FireAbility.class;
	}
	
	@Override
	public String getSubElementName() {
		return "Lightning";
	}
}
