package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.api.PlantAbility;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

public class PlantRegrowth extends PlantAbility {
	
	private byte data;
	private long time;
	private long regrowTime;
	private Material type;
	private Block block;
	
	public PlantRegrowth() {
	}

	@SuppressWarnings("deprecation")
	public PlantRegrowth(Player player, Block block) {
		super(player);
		
		this.regrowTime = getConfig().getLong("Abilities.Water.Plantbending.RegrowTime");
		if (regrowTime != 0) {
			this.block = block;
			this.type = block.getType();
			this.data = block.getData();
			time = System.currentTimeMillis() + regrowTime / 2 + (long) (Math.random() * (double) regrowTime) / 2;
			start();
		}
	}

	@Override
	@SuppressWarnings("deprecation")
	public void remove() {
		super.remove();
		if (block.getType() == Material.AIR) {
			block.setType(type);
			block.setData(data);
		} else {
			GeneralMethods.dropItems(block, GeneralMethods.getDrops(block, type, data, null));
		}
	}

	public void progress() {
		if (time < System.currentTimeMillis()) {
			remove();
		}
	}

	@Override
	public String getName() {
		return null;
	}

	@Override
	public Location getLocation() {
		return block != null ? block.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	public byte getData() {
		return data;
	}

	public void setData(byte data) {
		this.data = data;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getRegrowTime() {
		return regrowTime;
	}

	public void setRegrowTime(long regrowTime) {
		this.regrowTime = regrowTime;
	}

	public Material getType() {
		return type;
	}

	public void setType(Material type) {
		this.type = type;
	}

	public Block getBlock() {
		return block;
	}

	public void setBlock(Block block) {
		this.block = block;
	}

}
