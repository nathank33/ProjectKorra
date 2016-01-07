package com.projectkorra.projectkorra.ability;

public interface SubAbility {

	public Class<? extends Ability> getParentAbility();
	
	public String getSubElementName();
}
