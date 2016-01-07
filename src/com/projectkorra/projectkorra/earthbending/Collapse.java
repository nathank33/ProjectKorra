package com.projectkorra.projectkorra.earthbending;

import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.ability.EarthAbility;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.ClickType;

public class Collapse extends EarthAbility {

	private int distance;
	private int height;
	private long time;
	private long cooldown;
	private double range;
	private double speed;
	private Location origin;
	private Location location;
	private Vector direction;
	private Block block;
	private ConcurrentHashMap<Block, Block> affectedBlocks;
	
	public Collapse(Player player) {
		super(player);
		setFields();
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		block = BlockSource.getEarthSourceBlock(player, range, ClickType.LEFT_CLICK);
		if (block == null) {
			return;
		}

		this.origin = block.getLocation();
		this.location = origin.clone();
		this.distance = getEarthbendableBlocksLength(block, direction.clone().multiply(-1), height);
		loadAffectedBlocks();

		if (distance != 0) {
			start();
			bPlayer.addCooldown(this);
			time = System.currentTimeMillis() - (long) (1000.0 / speed);
		} else {
			remove();
		}
	}

	public Collapse(Player player, Location origin) {
		super(player);
		setFields();
		this.origin = origin;
		this.player = player;
		this.block = origin.getBlock();
		this.location = origin.clone();
		this.distance = getEarthbendableBlocksLength(block, direction.clone().multiply(-1), height);
		loadAffectedBlocks();

		if (distance != 0) {
			start();
			time = System.currentTimeMillis() - (long) (1000.0 / speed);
		} else {
			remove();
		}
	}

	private void setFields() {
		this.height = getConfig().getInt("Abilities.Earth.RaiseEarth.Column.Height");
		this.range = getConfig().getInt("Abilities.Earth.Collapse.Range");
		this.speed = getConfig().getDouble("Abilities.Earth.Collapse.Speed");
		this.cooldown = GeneralMethods.getGlobalCooldown();
		this.direction = new Vector(0, -1, 0);
		this.affectedBlocks = new ConcurrentHashMap<>();
	}

	private void loadAffectedBlocks() {
		affectedBlocks.clear();
		Block thisBlock;
		
		for (int i = 0; i <= distance; i++) {
			thisBlock = block.getWorld().getBlockAt(location.clone().add(direction.clone().multiply(-i)));
			affectedBlocks.put(thisBlock, thisBlock);
			if (RaiseEarth.blockInAllAffectedBlocks(thisBlock)) {
				RaiseEarth.revertBlock(thisBlock);
			}
		}
	}

	public static boolean blockInAllAffectedBlocks(Block block) {
		for (Collapse collapse : CoreAbility.getAbilities(Collapse.class)) {
			if (collapse.affectedBlocks.containsKey(block)) {
				return true;
			}
		}
		return false;
	}

	public static void revert(Block block) {
		for (Collapse collapse : CoreAbility.getAbilities(Collapse.class)) {
			collapse.affectedBlocks.remove(block);
		}
	}

	@Override
	public void progress() {
		if (System.currentTimeMillis() - time >= (long) (1000.0 / speed)) {
			time = System.currentTimeMillis();
			if (!tryToMoveEarth()) {
				remove();
				return;
			}
		}
	}

	private boolean tryToMoveEarth() {
		Block block = location.getBlock();
		location = location.add(direction);
		if (distance == 0) {
			return false;
		}
		
		moveEarth(block, direction, distance);
		loadAffectedBlocks();
		return location.distanceSquared(origin) < distance * distance;
	}

	@Override
	public String getName() {
		return "Collapse";
	}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	public Location getOrigin() {
		return origin;
	}

	public void setOrigin(Location origin) {
		this.origin = origin;
	}

	public Vector getDirection() {
		return direction;
	}

	public void setDirection(Vector direction) {
		this.direction = direction;
	}

	public Block getBlock() {
		return block;
	}

	public void setBlock(Block block) {
		this.block = block;
	}

	public int getDistance() {
		return distance;
	}

	public void setDistance(int distance) {
		this.distance = distance;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public ConcurrentHashMap<Block, Block> getAffectedBlocks() {
		return affectedBlocks;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
}
