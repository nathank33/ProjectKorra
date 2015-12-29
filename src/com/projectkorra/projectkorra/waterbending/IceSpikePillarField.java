package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.api.IceAbility;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class IceSpikePillarField extends IceAbility {
	
	private int damage;
	private int radius;
	private int numberOfSpikes;
	private long cooldown;
	private Vector thrownForce;

	public IceSpikePillarField() {
	}
	
	public IceSpikePillarField(Player player) {
		super(player);
		
		this.damage = 2;
		this.radius = 6;
		this.numberOfSpikes = ((radius * 2) * (radius * 2)) / 16;
		this.cooldown = getConfig().getLong("Abilities.Water.IceSpike.Cooldown");
		this.thrownForce = new Vector(0, 1, 0);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}
		
		Random random = new Random();
		int locX = player.getLocation().getBlockX();
		int locY = player.getLocation().getBlockY();
		int locZ = player.getLocation().getBlockZ();
		List<Block> iceBlocks = new ArrayList<Block>();
		
		for (int x = -(radius - 1); x <= (radius - 1); x++) {
			for (int z = -(radius - 1); z <= (radius - 1); z++) {
				for (int y = -1; y <= 1; y++) {
					Block testBlock = player.getWorld().getBlockAt(locX + x,	locY + y, locZ + z);
					
					if (testBlock.getType() == Material.ICE
							&& testBlock.getRelative(BlockFace.UP).getType() == Material.AIR
							&& !(testBlock.getX() == player.getEyeLocation().getBlock().getX() 
								&& testBlock.getZ() == player.getEyeLocation().getBlock().getZ())) {
						iceBlocks.add(testBlock);
						for(Block iceBlockForSound : iceBlocks) {
							playIcebendingSound(iceBlockForSound.getLocation());
						}
					}
				}
			}
		}

		List<Entity> entities = GeneralMethods.getEntitiesAroundPoint(player.getLocation(),	radius);
		for (int i = 0; i < numberOfSpikes; i++) {
			if (iceBlocks.isEmpty()) {
				return;
			}

			Entity target = null;
			Block targetBlock = null;
			for (Entity entity : entities) {
				if (entity instanceof LivingEntity && entity.getEntityId() != player.getEntityId()) {
					for (Block block : iceBlocks) {
						if (block.getX() == entity.getLocation().getBlockX() && block.getZ() == entity.getLocation().getBlockZ()) {
							target = entity;
							targetBlock = block;
							break;
						}
					}
				} else {
					continue;
				}
			}

			if (target != null) {
				entities.remove(target);
			} else {
				targetBlock = iceBlocks.get(random.nextInt(iceBlocks.size()));
			}
			
			if (targetBlock.getRelative(BlockFace.UP).getType() != Material.ICE) {
				new IceSpikePillar(player, targetBlock.getLocation(), damage, thrownForce, cooldown);
				bPlayer.addCooldown(this);
				iceBlocks.remove(targetBlock);
			}
		}
	}

	@Override
	public String getName() {
		return "IceSpike";
	}

	@Override
	public void progress() {}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	public int getDamage() {
		return damage;
	}

	public void setDamage(int damage) {
		this.damage = damage;
	}

	public int getRadius() {
		return radius;
	}

	public void setRadius(int radius) {
		this.radius = radius;
	}

	public int getNumberOfSpikes() {
		return numberOfSpikes;
	}

	public void setNumberOfSpikes(int numberOfSpikes) {
		this.numberOfSpikes = numberOfSpikes;
	}

	public Vector getThrownForce() {
		return thrownForce;
	}

	public void setThrownForce(Vector thrownForce) {
		this.thrownForce = thrownForce;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

}