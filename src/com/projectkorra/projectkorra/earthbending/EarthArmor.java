package com.projectkorra.projectkorra.earthbending;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.util.TempBlock;
import com.projectkorra.projectkorra.util.TempPotionEffect;

public class EarthArmor extends EarthAbility {

	private boolean formed;
	private boolean complete;
	private byte headData;
	private byte legsData;
	private int strength;
	private int range;
	private long time;
	private long cooldown;
	private long interval;
	private long duration;
	private Block headBlock;
	private Block legsBlock;
	private Location headBlockLocation;
	private Location legsBlockLocation;
	private Material headType;
	private Material legsType;
	private ItemStack[] oldArmor;
	
	public EarthArmor() {
	}

	@SuppressWarnings("deprecation")
	public EarthArmor(Player player) {
		super(player);
		if (CoreAbility.hasAbility(player, EarthArmor.class) || !bPlayer.canBend(this)) {
			return;
		}
		
		this.formed = false;
		this.complete = false;
		this.interval = 2000;
		this.cooldown = getConfig().getLong("Abilities.Earth.EarthArmor.Cooldown");
		this.duration = getConfig().getLong("Abilities.Earth.EarthArmor.Duration");
		this.strength = getConfig().getInt("Abilities.Earth.EarthArmor.Strength");
		this.range = 7;

		headBlock = getTargetEarthBlock(range);
		if (!GeneralMethods.isRegionProtectedFromBuild(this, headBlock.getLocation()) 
				&& getEarthbendableBlocksLength(headBlock, new Vector(0, -1, 0), 2) >= 2) {			
			this.legsBlock = headBlock.getRelative(BlockFace.DOWN);
			this.headType = headBlock.getType();
			this.legsType = legsBlock.getType();
			this.headData = headBlock.getData();
			this.legsData = legsBlock.getData();
			this.headBlockLocation = headBlock.getLocation();
			this.legsBlockLocation = legsBlock.getLocation();

			Block oldHeadBlock = headBlock;
			Block oldLegsBlock = legsBlock;

			if (!moveBlocks()) {
				return;
			}
			if (isEarthRevertOn()) {
				addTempAirBlock(oldHeadBlock);
				addTempAirBlock(oldLegsBlock);
			} else {
				GeneralMethods.removeBlock(oldHeadBlock);
				GeneralMethods.removeBlock(oldLegsBlock);
			}
			start();
		}
	}

	private void formArmor() {
		if (TempBlock.isTempBlock(headBlock)) {
			TempBlock.revertBlock(headBlock, Material.AIR);
		}
		if (TempBlock.isTempBlock(legsBlock)) {
			TempBlock.revertBlock(legsBlock, Material.AIR);
		}

		this.oldArmor = player.getInventory().getArmorContents();
		ItemStack armors[] = { new ItemStack(Material.LEATHER_BOOTS, 1), 
				new ItemStack(Material.LEATHER_LEGGINGS, 1), 
				new ItemStack(Material.LEATHER_CHESTPLATE, 1), 
				new ItemStack(Material.LEATHER_HELMET, 1) };
		player.getInventory().setArmorContents(armors);
		PotionEffect resistance = new PotionEffect(PotionEffectType.DAMAGE_RESISTANCE, (int) duration / 50, strength - 1);
		new TempPotionEffect(player, resistance);
		formed = true;
	}
	
	private boolean inPosition() {
		return headBlock.equals(player.getEyeLocation().getBlock()) && legsBlock.equals(player.getLocation().getBlock());
	}

	private boolean moveBlocks() {
		if (!player.getWorld().equals(headBlock.getWorld())) {
			remove();
			return false;
		}
		
		Location headLocation = player.getEyeLocation();
		Location legsLocation = player.getLocation();
		Vector headDirection = headLocation.toVector().subtract(headBlockLocation.toVector()).normalize().multiply(.5);
		Vector legsDirection = legsLocation.toVector().subtract(legsBlockLocation.toVector()).normalize().multiply(.5);
		Block newHeadBlock = headBlock;
		Block newLegsBlock = legsBlock;

		if (!headLocation.getBlock().equals(headBlock)) {
			headBlockLocation = headBlockLocation.clone().add(headDirection);
			newHeadBlock = headBlockLocation.getBlock();
		}
		if (!legsLocation.getBlock().equals(legsBlock)) {
			legsBlockLocation = legsBlockLocation.clone().add(legsDirection);
			newLegsBlock = legsBlockLocation.getBlock();
		}

		if (isTransparentToEarthbending(newHeadBlock) && !newHeadBlock.isLiquid()) {
			GeneralMethods.breakBlock(newHeadBlock);
		} else if (!isEarthbendable(newHeadBlock) && !newHeadBlock.isLiquid() && newHeadBlock.getType() != Material.AIR) {
			remove();
			return false;
		}

		if (isTransparentToEarthbending(newLegsBlock) && !newLegsBlock.isLiquid()) {
			GeneralMethods.breakBlock(newLegsBlock);
		} else if (!isEarthbendable(newLegsBlock) && !newLegsBlock.isLiquid() && newLegsBlock.getType() != Material.AIR) {
			remove();
			return false;
		}

		if (headBlock.getLocation().distanceSquared(player.getEyeLocation()) > range * range 
				|| legsBlock.getLocation().distanceSquared(player.getLocation()) > range * range) {
			remove();
			return false;
		}

		if (!newHeadBlock.equals(headBlock)) {
			new TempBlock(newHeadBlock, headType, headData);
			if (TempBlock.isTempBlock(headBlock)) {
				TempBlock.revertBlock(headBlock, Material.AIR);
			}
		}

		if (!newLegsBlock.equals(legsBlock)) {
			new TempBlock(newLegsBlock, legsType, legsData);
			if (TempBlock.isTempBlock(legsBlock)) {
				TempBlock.revertBlock(legsBlock, Material.AIR);
			}
		}
		headBlock = newHeadBlock;
		legsBlock = newLegsBlock;
		return true;
	}

	@Override
	public void progress() {		
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			return;
		}

		if (formed) {
			if (System.currentTimeMillis() > startTime + duration && !complete) {
				complete = true;
				bPlayer.addCooldown(this);
				remove();
				return;
			}
		} else if (System.currentTimeMillis() > time + interval) {
			if (!moveBlocks()) {
				return;
			}
			if (inPosition()) {
				formArmor();
			}
		}
	}

	@Override
	public void remove() {
		super.remove();
		if (isEarthRevertOn()) {
			if (TempBlock.isTempBlock(headBlock)) {
				TempBlock.revertBlock(headBlock, Material.AIR);
			}
			if (TempBlock.isTempBlock(legsBlock)) {
				TempBlock.revertBlock(legsBlock, Material.AIR);
			}
		} else {
			headBlock.breakNaturally();
			legsBlock.breakNaturally();
		}
		
		if (oldArmor != null) {
			player.getInventory().setArmorContents(oldArmor);
		}
		player.removePotionEffect(PotionEffectType.DAMAGE_RESISTANCE);
	}

	@Override
	public String getName() {
		return "EarthArmor";
	}

	@Override
	public Location getLocation() {
		return headBlockLocation;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	public boolean isFormed() {
		return formed;
	}

	public void setFormed(boolean formed) {
		this.formed = formed;
	}

	public boolean isComplete() {
		return complete;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public byte getHeadData() {
		return headData;
	}

	public void setHeadData(byte headData) {
		this.headData = headData;
	}

	public byte getLegsData() {
		return legsData;
	}

	public void setLegsData(byte legsData) {
		this.legsData = legsData;
	}

	public int getStrength() {
		return strength;
	}

	public void setStrength(int strength) {
		this.strength = strength;
	}

	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public long getInterval() {
		return interval;
	}

	public void setInterval(long interval) {
		this.interval = interval;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public Block getHeadBlock() {
		return headBlock;
	}

	public void setHeadBlock(Block headBlock) {
		this.headBlock = headBlock;
	}

	public Block getLegsBlock() {
		return legsBlock;
	}

	public void setLegsBlock(Block legsBlock) {
		this.legsBlock = legsBlock;
	}

	public Location getHeadBlockLocation() {
		return headBlockLocation;
	}

	public void setHeadBlockLocation(Location headBlockLocation) {
		this.headBlockLocation = headBlockLocation;
	}

	public Location getLegsBlockLocation() {
		return legsBlockLocation;
	}

	public void setLegsBlockLocation(Location legsBlockLocation) {
		this.legsBlockLocation = legsBlockLocation;
	}

	public Material getHeadType() {
		return headType;
	}

	public void setHeadType(Material headType) {
		this.headType = headType;
	}

	public Material getLegsType() {
		return legsType;
	}

	public void setLegsType(Material legsType) {
		this.legsType = legsType;
	}

	public ItemStack[] getOldArmor() {
		return oldArmor;
	}

	public void setOldArmor(ItemStack[] oldArmor) {
		this.oldArmor = oldArmor;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
	
}
