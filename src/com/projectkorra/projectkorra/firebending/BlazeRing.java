package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.FireAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class BlazeRing extends FireAbility {
	
	private int range;
	private long cooldown;
	private double angleIncrement;
	private Location location;
	
	public BlazeRing() {
	}

	public BlazeRing(Player player) {
		super(player);
		
		this.range = getConfig().getInt("Abilities.Fire.Blaze.RingOfFire.Range");
		this.angleIncrement = 10;
		this.cooldown = GeneralMethods.getGlobalCooldown();
		this.location = player.getLocation();
		
		this.range = (int) AvatarState.getValue(this.range, player);
		
		if (bPlayer.isOnCooldown(this)) {
			return;
		}		

		for (double degrees = 0; degrees < 360; degrees += angleIncrement) {
			double angle = Math.toRadians(degrees);
			Vector direction = player.getEyeLocation().getDirection().clone();
			double x, z, vx, vz;
			
			x = direction.getX();
			z = direction.getZ();

			vx = x * Math.cos(angle) - z * Math.sin(angle);
			vz = x * Math.sin(angle) + z * Math.cos(angle);

			direction.setX(vx);
			direction.setZ(vz);
			
			new BlazeArc(player, location, direction, range);
		}
		
		start();
		bPlayer.addCooldown(this);
		remove();
	}

	@Override
	public String getName() {
		return "Blaze";
	}

	@Override
	public void progress() {}

	@Override
	public Location getLocation() {
		return location;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

	public int getRange() {
		return range;
	}

	public void setRange(int range) {
		this.range = range;
	}

	public double getAngleIncrement() {
		return angleIncrement;
	}

	public void setAngleIncrement(double angleIncrement) {
		this.angleIncrement = angleIncrement;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

}