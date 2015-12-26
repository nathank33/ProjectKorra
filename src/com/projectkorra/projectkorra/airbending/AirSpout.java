package com.projectkorra.projectkorra.airbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.api.AirAbility;
import com.projectkorra.projectkorra.ability.api.CoreAbility;
import com.projectkorra.projectkorra.util.Flight;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Random;

public class AirSpout extends AirAbility {

	private static final Integer[] DIRECTIONS = { 0, 1, 2, 3, 5, 6, 7, 8 };

	private int angle;
	private long updateInterval;
	private long cooldown;
	private double height;
	
	public AirSpout() {}

	public AirSpout(Player player) {
		super(player);
		
		AirSpout spout = CoreAbility.getAbility(player, AirSpout.class);
		if (spout != null) {
			spout.remove();
			return;
		}
		
		if (!bPlayer.canBend(this)) {
			remove();
			return;
		}

		this.angle = 0;
		this.cooldown = 0;
		this.height = getConfig().getDouble("Abilities.Air.AirSpout.Height");
		this.updateInterval = 100;

		new Flight(player);
		start();
		bPlayer.addCooldown(this);
	}

	public static ArrayList<Player> getPlayers() {
		ArrayList<Player> players = new ArrayList<Player>();
		for (AirSpout spout : CoreAbility.getAbilities(AirSpout.class)) {
			players.add(spout.getPlayer());
		}
		return players;
	}

	public static boolean removeSpouts(Location loc0, double radius, Player sourceplayer) {
		boolean removed = false;
		for (AirSpout spout : CoreAbility.getAbilities(AirSpout.class)) {
			if (!spout.player.equals(sourceplayer)) {
				Location loc1 = spout.player.getLocation().getBlock().getLocation();
				loc0 = loc0.getBlock().getLocation();
				double dx = loc1.getX() - loc0.getX();
				double dy = loc1.getY() - loc0.getY();
				double dz = loc1.getZ() - loc0.getZ();

				double distance = Math.sqrt(dx * dx + dz * dz);

				if (distance <= radius && dy > 0 && dy < spout.height) {
					spout.remove();
					removed = true;
				}
			}
		}
		return removed;
	}

	private void allowFlight() {
		player.setAllowFlight(true);
		player.setFlying(true);
	}

	private Block getGround() {
		Block standingblock = player.getLocation().getBlock();
		for (int i = 0; i <= height + 5; i++) {
			Block block = standingblock.getRelative(BlockFace.DOWN, i);
			if (GeneralMethods.isSolid(block) || block.isLiquid()) {
				return block;
			}
		}
		return null;
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		}

		Block eyeBlock = player.getEyeLocation().getBlock();
		if (eyeBlock.isLiquid() || GeneralMethods.isSolid(eyeBlock)) {
			remove();
			return;
		}

		player.setFallDistance(0);
		player.setSprinting(false);
		if ((new Random()).nextInt(4) == 0) {
			playAirbendingSound(player.getLocation());
		}

		Block block = getGround();
		if (block != null) {
			double dy = player.getLocation().getY() - block.getY();
			if (dy > height) {
				removeFlight();
			} else {
				allowFlight();
			}
			rotateAirColumn(block);
		} else {
			remove();
		}
	}

	public void remove() {
		super.remove();
		removeFlight();
	}

	private void removeFlight() {
		player.setAllowFlight(false);
		player.setFlying(false);
	}

	private void rotateAirColumn(Block block) {
		if (!player.getWorld().equals(block.getWorld())) {
			return;
		}
		if (System.currentTimeMillis() >= startTime + updateInterval) {
			startTime = System.currentTimeMillis();
			Location location = block.getLocation();
			Location playerloc = player.getLocation();
			location = new Location(location.getWorld(), playerloc.getX(), location.getY(), playerloc.getZ());

			int index = angle;
			double dy = Math.min(playerloc.getY() - block.getY(), height);
			angle = angle >= DIRECTIONS.length ? 0 : angle + 1;

			for (int i = 1; i <= dy; i++) {
				index = index >= DIRECTIONS.length ? 0 : index + 1;
				Location effectloc2 = new Location(location.getWorld(), location.getX(), block.getY() + i, location.getZ());
				playAirbendingParticles(effectloc2, 3, 0.4F, 0.4F, 0.4F);
			}
		}
	}

	@Override
	public String getName() {
		return "AirSpout";
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	public int getAngle() {
		return angle;
	}

	public void setAngle(int angle) {
		this.angle = angle;
	}

	public long getUpdateInterval() {
		return updateInterval;
	}

	public void setUpdateInterval(long updateInterval) {
		this.updateInterval = updateInterval;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
	
}
