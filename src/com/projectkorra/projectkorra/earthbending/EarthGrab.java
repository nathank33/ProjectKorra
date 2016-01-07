package com.projectkorra.projectkorra.earthbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.EarthAbility;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.ArrayList;

public class EarthGrab extends EarthAbility {
	
	private long cooldown;
	private double lowestDistance;
	private double range;
	private double height;
	private Location origin;
	private Vector direction;
	private Entity closestEntity;

	public EarthGrab(Player player, boolean isOtherEntity) {
		super(player);
		
		this.range = getConfig().getDouble("Abilities.Earth.EarthGrab.Range");
		this.height = 6;
		this.cooldown = GeneralMethods.getGlobalCooldown();
		this.origin = player.getEyeLocation();
		this.direction = origin.getDirection();
		this.lowestDistance = range + 1;
		this.closestEntity = null;
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		start();
		if (isOtherEntity) {
			earthGrabOtherEntity();
		} else {
			earthGrabSelf();
		}
		remove();
	}
	
	public void earthGrabOtherEntity() {
		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(origin, range)) {
			if (GeneralMethods.getDistanceFromLine(direction, origin, entity.getLocation()) <= 3
					&& (entity instanceof LivingEntity)
					&& (entity.getEntityId() != player.getEntityId())) {
				double distance = origin.distance(entity.getLocation());
				if (distance < lowestDistance) {
					closestEntity = entity;
					lowestDistance = distance;
				}
			}
		}

		if (closestEntity != null) {
			ArrayList<Block> blocks = new ArrayList<Block>();
			Location location = closestEntity.getLocation();
			Location loc1 = location.clone();
			Location loc2 = location.clone();
			Location testLoc, testloc2;
			double factor = 3;
			double factor2 = 4;
			int height1 = 3;
			int height2 = 2;
			
			for (double angle = 0; angle <= 360; angle += 20) {
				testLoc = loc1.clone().add(
						factor * Math.cos(Math.toRadians(angle)), 1,
						factor * Math.sin(Math.toRadians(angle)));
				testloc2 = loc2.clone().add(
						factor2 * Math.cos(Math.toRadians(angle)), 1,
						factor2 * Math.sin(Math.toRadians(angle)));
				
				for (int y = 0; y < height - height1; y++) {
					testLoc = testLoc.clone().add(0, -1, 0);
					if (isEarthbendable(testLoc.getBlock())) {
						if (!blocks.contains(testLoc.getBlock())) {
							new RaiseEarth(player, testLoc, height1 + y - 1);
						}
						blocks.add(testLoc.getBlock());
						break;
					}
				}
				
				for (int y = 0; y < height - height2; y++) {
					testloc2 = testloc2.clone().add(0, -1, 0);
					if (isEarthbendable(testloc2.getBlock())) {
						if (!blocks.contains(testloc2.getBlock())) {
							new RaiseEarth(player, testloc2, height2 + y - 1);
						}
						blocks.add(testloc2.getBlock());
						break;
					}
				}
			}

			if (!blocks.isEmpty()) {
				bPlayer.addCooldown(this);
			}
		}
	}

	public void earthGrabSelf() {		
		closestEntity = player;
		if (closestEntity != null) {
			ArrayList<Block> blocks = new ArrayList<Block>();
			Location location = closestEntity.getLocation();
			Location loc1 = location.clone();
			Location loc2 = location.clone();
			Location testLoc, testLoc2;
			double factor = 3;
			double factor2 = 4;
			int height1 = 3;
			int height2 = 2;
			
			for (double angle = 0; angle <= 360; angle += 20) {
				testLoc = loc1.clone().add(
						factor * Math.cos(Math.toRadians(angle)), 1,
						factor * Math.sin(Math.toRadians(angle)));
				testLoc2 = loc2.clone().add(
						factor2 * Math.cos(Math.toRadians(angle)), 1,
						factor2 * Math.sin(Math.toRadians(angle)));
				
				for (int y = 0; y < height - height1; y++) {
					testLoc = testLoc.clone().add(0, -1, 0);
					if (isEarthbendable(testLoc.getBlock())) {
						if (!blocks.contains(testLoc.getBlock())) {
							new RaiseEarth(player, testLoc, height1 + y - 1);
						}
						blocks.add(testLoc.getBlock());
						break;
					}
				}
				
				for (int y = 0; y < height - height2; y++) {
					testLoc2 = testLoc2.clone().add(0, -1, 0);
					if (isEarthbendable(testLoc2.getBlock())) {
						if (!blocks.contains(testLoc2.getBlock())) {
							new RaiseEarth(player, testLoc2, height2 + y - 1);
						}
						blocks.add(testLoc2.getBlock());
						break;
					}
				}
			}

			if (!blocks.isEmpty()) {
				bPlayer.addCooldown(this);
			}
		}
	}

	@Override
	public String getName() {
		return "EarthGrab";
	}

	@Override
	public void progress() {
	}

	@Override
	public Location getLocation() {
		return origin;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	public double getLowestDistance() {
		return lowestDistance;
	}

	public void setLowestDistance(double lowestDistance) {
		this.lowestDistance = lowestDistance;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
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

	public Entity getClosestEntity() {
		return closestEntity;
	}

	public void setClosestEntity(Entity closestEntity) {
		this.closestEntity = closestEntity;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}
	
}