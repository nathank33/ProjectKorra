package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.AvatarState;
import com.projectkorra.projectkorra.ability.api.FireAbility;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;


public class Blaze extends FireAbility {
	
	private int arc;
	private long cooldown;
	private double range;
	private double speed;

	public Blaze() {
	}
	
	public Blaze(Player player) {
		super(player);
		
		this.speed = 2;
		this.cooldown = GeneralMethods.getGlobalCooldown();
		this.arc = getConfig().getInt("Abilities.Fire.Blaze.ArcOfFire.Arc");
		this.range = getConfig().getDouble("Abilities.Fire.Blaze.ArcOfFire.Range");
		
		if (!bPlayer.canBend(this)) {
			return;
		}
		
		this.range = getFirebendingDayAugment(range);
		this.range = AvatarState.getValue(range, player);
		this.arc = (int) getFirebendingDayAugment(arc);
		Location location = player.getLocation();

		for (int i = -arc; i <= arc; i += speed) {
			double angle = Math.toRadians(i);
			Vector direction = player.getEyeLocation().getDirection().clone();
			double x, z, vx, vz;
			
			x = direction.getX();
			z = direction.getZ();

			vx = x * Math.cos(angle) - z * Math.sin(angle);
			vz = x * Math.sin(angle) + z * Math.cos(angle);

			direction.setX(vx);
			direction.setZ(vz);

			new BlazeArc(location, direction, player, range);
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
	public void progress() {
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}

}
