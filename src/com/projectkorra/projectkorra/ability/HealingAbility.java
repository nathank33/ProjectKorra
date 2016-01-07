package com.projectkorra.projectkorra.ability;

import org.bukkit.entity.Player;

public abstract class HealingAbility extends WaterAbility implements SubAbility {
	
	public HealingAbility(Player player) {
		super(player);
	}

	@Override
	public Class<? extends Ability> getParentAbility() {
		return WaterAbility.class;
	}
	
	@Override
	public String getSubElementName() {
		return "Healing";
	}
}
