package com.projectkorra.projectkorra.ability.multiability;

import com.projectkorra.projectkorra.ability.AddonAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.multiability.MultiAbilityManager.MultiAbilitySub;

import org.bukkit.entity.Player;

import java.util.ArrayList;

public abstract class MultiAbilityModule extends CoreAbility implements AddonAbility {

	public MultiAbilityModule() {
	}
	
	public MultiAbilityModule(Player player) {
		super(player);
	}

	/**
	 * Returns the sub abilities of a MultiAbility. e.g. {@code new
	 * MultiAbilitySub("SubAbility", Element.Fire, SubElement.Lightning);}
	 * 
	 * @return arraylist of multiabilitysub
	 */
	public abstract ArrayList<MultiAbilitySub> getMultiAbilities();

}
