package com.projectkorra.projectkorra.ability.api;

import org.bukkit.entity.Player;

public abstract class MetalAbility extends EarthAbility implements SubAbility {
	
	public MetalAbility() {}
	
	public MetalAbility(Player player) {
		super(player);
	}
	
	@Override
	public Class<? extends Ability> getParentAbility() {
		return EarthAbility.class;
	}
	
	@Override
	public String getSubElementName() {
		return "Metal";
	}

}
