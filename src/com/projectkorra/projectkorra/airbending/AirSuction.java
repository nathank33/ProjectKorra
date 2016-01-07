package com.projectkorra.projectkorra.airbending;

import com.projectkorra.projectkorra.Element;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.AirAbility;
import com.projectkorra.projectkorra.ability.CoreAbility;
import com.projectkorra.projectkorra.avatar.AvatarState;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.object.HorizontalVelocityTracker;
import com.projectkorra.projectkorra.util.Flight;
import com.projectkorra.projectkorra.waterbending.WaterSpout;

import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

import java.util.concurrent.ConcurrentHashMap;

public class AirSuction extends AirAbility {

	private static final int ORIGIN_PARTICLE_COUNT = 6;
	private static final int ABILITY_PARTICLE_COUNT = 6;
	private static final int MAX_TICKS = 10000;
	private static final double ORIGIN_SELECT_RANGE = 10;
	private static final ConcurrentHashMap<Player, Location> ORIGINS = new ConcurrentHashMap<>();
	
	private boolean hasOtherOrigin;
	private int ticks;
	private int particleCount;
	private long cooldown;
	private double speed;
	private double range;
	private double radius;
	private double pushFactor;
	private Location location;
	private Location origin;
	private Vector direction;
	
	public AirSuction() {}

	public AirSuction(Player player) {
		super(player);
		if (bPlayer.isOnCooldown("AirSuction")) {
			return;
		}
		if (player.getEyeLocation().getBlock().isLiquid()) {
			return;
		}
		if ( CoreAbility.hasAbility(player, AirSpout.class) || CoreAbility.hasAbility(player, WaterSpout.class)) {
			return;
		}

		this.hasOtherOrigin = false;
		this.ticks = 0;
		this.particleCount = ABILITY_PARTICLE_COUNT;
		this.speed = getConfig().getDouble("Abilities.Air.AirSuction.Speed");
		this.range = getConfig().getDouble("Abilities.Air.AirSuction.Range");
		this.radius = getConfig().getDouble("Abilities.Air.AirSuction.Radius");
		this.pushFactor = getConfig().getDouble("Abilities.Air.AirSuction.Push");
		this.cooldown = GeneralMethods.getGlobalCooldown();

		if (ORIGINS.containsKey(player)) {
			origin = ORIGINS.get(player);
			hasOtherOrigin = true;
			ORIGINS.remove(player);
		} else {
			origin = player.getEyeLocation();
		}

		location = GeneralMethods.getTargetedLocation(player, range, GeneralMethods.nonOpaque);
		direction = GeneralMethods.getDirection(location, origin).normalize();
		Entity entity = GeneralMethods.getTargetedEntity(player, range);

		if (entity != null) {
			direction = GeneralMethods.getDirection(entity.getLocation(), origin).normalize();
			location = getLocation(origin, direction.clone().multiply(-1));
		}

		bPlayer.addCooldown("AirSuction", cooldown);
		start();
	}

	private static void playOriginEffect(Player player) {
		if (!ORIGINS.containsKey(player)) {
			return;
		}

		Location origin = ORIGINS.get(player);
		if (player.isDead() || !player.isOnline()) {
			return;
		} else if (!origin.getWorld().equals(player.getWorld())) {
			ORIGINS.remove(player);
			return;
		} else if (GeneralMethods.getBoundAbility(player) == null) {
			ORIGINS.remove(player);
			return;
		} else if (!GeneralMethods.getBoundAbility(player).equalsIgnoreCase("AirSuction")
				|| !GeneralMethods.canBend(player.getName(), "AirSuction")) {
			ORIGINS.remove(player);
			return;
		} else if (origin.distanceSquared(player.getEyeLocation()) > ORIGIN_SELECT_RANGE * ORIGIN_SELECT_RANGE) {
			ORIGINS.remove(player);
			return;
		}

		playAirbendingParticles(origin, ORIGIN_PARTICLE_COUNT);
	}

	public static void progressOrigins() {
		for (Player player : ORIGINS.keySet()) {
			playOriginEffect(player);
		}
	}

	public static void setOrigin(Player player) {
		Location location = GeneralMethods.getTargetedLocation(player, ORIGIN_SELECT_RANGE, GeneralMethods.nonOpaque);
		if (location.getBlock().isLiquid() || GeneralMethods.isSolid(location.getBlock())) {
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(player, "AirSuction", location)) {
			return;
		} else if (ORIGINS.containsKey(player)) {
			ORIGINS.replace(player, location);
		} else {
			ORIGINS.put(player, location);
		}
	}

	private void advanceLocation() {
		playAirbendingParticles(location, particleCount, 0.275F, 0.275F, 0.275F);
		if (GeneralMethods.rand.nextInt(4) == 0) {
			playAirbendingSound(location);
		}
		double speedFactor = speed * (ProjectKorra.time_step / 1000.);
		location = location.add(direction.clone().multiply(speedFactor));
	}

	private Location getLocation(Location origin, Vector direction) {
		Location location = origin.clone();
		for (double i = 1; i <= range; i++) {
			location = origin.clone().add(direction.clone().multiply(i));
			if (!isTransparentToEarthbending(location.getBlock())
					|| GeneralMethods.isRegionProtectedFromBuild(this, location)) {
				return origin.clone().add(direction.clone().multiply(i - 1));
			}
		}
		return location;
	}

	@Override
	public void progress() {
		if (player.isDead() || !player.isOnline()) {
			remove();
			return;
		} else if (GeneralMethods.isRegionProtectedFromBuild(player, "AirSuction", location)) {
			remove();
			return;
		}

		ticks++;
		if (ticks > MAX_TICKS) {
			remove();
			return;
		} else if ((location.distanceSquared(origin) > range * range) || (location.distanceSquared(origin) <= 1)) {
			remove();
			return;
		}

		for (Entity entity : GeneralMethods.getEntitiesAroundPoint(location, radius)) {
			if (entity.getEntityId() != player.getEntityId() || hasOtherOrigin) {
				Vector velocity = entity.getVelocity();
				double max = speed;
				double factor = pushFactor;
				if (AvatarState.isAvatarState(player)) {
					max = AvatarState.getValue(max);
					factor = AvatarState.getValue(factor);
				}

				Vector push = direction.clone();
				if (Math.abs(push.getY()) > max && entity.getEntityId() != player.getEntityId()) {
					if (push.getY() < 0) {
						push.setY(-max);
					} else {
						push.setY(max);
					}
				}

				factor *= 1 - location.distance(origin) / (2 * range);

				double comp = velocity.dot(push.clone().normalize());
				if (comp > factor) {
					velocity.multiply(.5);
					velocity.add(push.clone().normalize().multiply(velocity.clone().dot(push.clone().normalize())));
				} else if (comp + factor * .5 > factor) {
					velocity.add(push.clone().multiply(factor - comp));
				} else {
					velocity.add(push.clone().multiply(factor * .5));
				}

				if (entity instanceof Player) {
					if (Commands.invincible.contains(((Player) entity).getName())) {
						continue;
					}
				}

				GeneralMethods.setVelocity(entity, velocity);
				new HorizontalVelocityTracker(entity, player, 200l, "AirSuction", Element.Air, null);
				entity.setFallDistance(0);
				if (entity.getEntityId() != player.getEntityId() && entity instanceof Player) {
					new Flight((Player) entity, player);
				}

				if (entity.getFireTicks() > 0) {
					entity.getWorld().playEffect(entity.getLocation(), Effect.EXTINGUISH, 0);
				}
				entity.setFireTicks(0);
				breakBreathbendingHold(entity);
			}
		}

		advanceLocation();
	}

	public static boolean removeAirSuctionsAroundPoint(Location location, double radius) {
		boolean removed = false;
		for (AirSuction airSuction : CoreAbility.getAbilities(AirSuction.class)) {
			Location airSuctionlocation = airSuction.location;
			if (location.getWorld() == airSuctionlocation.getWorld()) {
				if (location.distanceSquared(airSuctionlocation) <= radius * radius) {
					airSuction.remove();
				}
				removed = true;
			}
		}
		return removed;
	}

	@Override
	public String getName() {
		return "AirSuction";
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

	public boolean isHasOtherOrigin() {
		return hasOtherOrigin;
	}

	public void setHasOtherOrigin(boolean hasOtherOrigin) {
		this.hasOtherOrigin = hasOtherOrigin;
	}

	public int getTicks() {
		return ticks;
	}

	public void setTicks(int ticks) {
		this.ticks = ticks;
	}

	public int getParticleCount() {
		return particleCount;
	}

	public void setParticleCount(int particleCount) {
		this.particleCount = particleCount;
	}

	public double getSpeed() {
		return speed;
	}

	public void setSpeed(double speed) {
		this.speed = speed;
	}

	public double getRange() {
		return range;
	}

	public void setRange(double range) {
		this.range = range;
	}

	public double getRadius() {
		return radius;
	}

	public void setRadius(double radius) {
		this.radius = radius;
	}

	public double getPushFactor() {
		return pushFactor;
	}

	public void setPushFactor(double pushFactor) {
		this.pushFactor = pushFactor;
	}

	public void setLocation(Location location) {
		this.location = location;
	}

	public void setCooldown(long cooldown) {
		this.cooldown = cooldown;
	}

}
