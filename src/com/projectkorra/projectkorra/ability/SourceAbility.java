package com.projectkorra.projectkorra.ability;

import org.bukkit.block.Block;

public interface SourceAbility {
	
	public Block getSource();
	
	public boolean canAutoSource();
	
	public boolean canDynamicSource();

}
