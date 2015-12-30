package com.projectkorra.projectkorra.waterbending;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.api.CoreAbility;
import com.projectkorra.projectkorra.ability.api.WaterAbility;
import com.projectkorra.projectkorra.util.Flight;
import com.projectkorra.projectkorra.util.ParticleEffect;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class WaterSpout extends WaterAbility {

	private static final ConcurrentHashMap<Block, Block> AFFECTED_BLOCKS = new ConcurrentHashMap<Block, Block>();
	private static final ConcurrentHashMap<Block, Block> NEW_AFFECTED_BLOCKS = new ConcurrentHashMap<Block, Block>();
	private static final ConcurrentHashMap<Block, Long> REVERT_BLOCKS = new ConcurrentHashMap<Block, Long>();

	private boolean canBendOnPackedIce;
	private boolean useParticles;
	private boolean useBlockSpiral;
	private int angle;
	private long time;
	private long interval;
	private double rotation;
	private double height;
	private Block base;
	private TempBlock baseBlock;
	
	public WaterSpout() {
	}

	public WaterSpout(Player player) {
		super(player);
	
		WaterSpout oldSpout = CoreAbility.getAbility(player, WaterSpout.class);
		if (oldSpout != null) {
			oldSpout.remove();
			return;
		}
		
		this.canBendOnPackedIce = ProjectKorra.plugin.getConfig().getBoolean("Properties.Water.CanBendPackedIce");
		this.useParticles = getConfig().getBoolean("Abilities.Water.WaterSpout.Particles");
		this.useBlockSpiral = getConfig().getBoolean("Abilities.Water.WaterSpout.BlockSpiral");
		this.height = getConfig().getDouble("Abilities.Water.WaterSpout.Height");
		this.interval = 50;
		
		WaterSpoutWave spoutWave = new WaterSpoutWave(player, WaterSpoutWave.AbilityType.CLICK);
		if (spoutWave.hasStarted()) {
			return;
		}

		Block topBlock = GeneralMethods.getTopBlock(player.getLocation(), 0, -50);
		if (topBlock == null) {
			topBlock = player.getLocation().getBlock();
		}
		
		if (!isWater(topBlock) && !isIcebendable(topBlock) && !isSnow(topBlock)) {
			return;
		} else if (topBlock.getType() == Material.PACKED_ICE && !canBendOnPackedIce) {
			return;
		}

		new Flight(player);
		player.setAllowFlight(true);
		start();
	}

	private void displayWaterSpiral(Location location) {
		if (!useBlockSpiral) {
			return;
		}

		double maxHeight = player.getLocation().getY() - location.getY() - .5;
		double height = 0;
		rotation += .4;
		int i = 0;
		
		while (height < maxHeight) {
			i += 20;
			height += .4;
			double angle = (i * Math.PI / 180);
			double x = 1 * Math.cos(angle + rotation);
			double z = 1 * Math.sin(angle + rotation);
			Location loc = location.clone().getBlock().getLocation().add(.5, .5, .5);
			loc.add(x, height, z);

			Block block = loc.getBlock();
			if (block.getType().equals(Material.AIR) || !GeneralMethods.isSolid(block)) {
				REVERT_BLOCKS.put(block, 0L);
				new TempBlock(block, Material.STATIONARY_WATER, (byte) 1);
			}
		}
	}

	@Override
	public void progress() {
		if (!bPlayer.canBendIgnoreBindsCooldowns(this)) {
			remove();
			return;
		} else {
			player.setFallDistance(0);
			player.setSprinting(false);
			if ((new Random()).nextInt(4) == 0) {
				playWaterbendingSound(player.getLocation());
			}

			player.removePotionEffect(PotionEffectType.SPEED);
			Location location = player.getLocation().clone().add(0, .2, 0);
			Block block = location.clone().getBlock();
			double height = spoutableWaterHeight(location);

			if (height != -1) {
				location = base.getLocation();
				for (int i = 1; i <= height; i++) {
					block = location.clone().add(0, i, 0).getBlock();
					
					if (!TempBlock.isTempBlock(block)) {
						new TempBlock(block, Material.STATIONARY_WATER, (byte) 8);
					}
					if (!AFFECTED_BLOCKS.containsKey(block)) {
						AFFECTED_BLOCKS.put(block, block);
					}
					rotateParticles(block);
					NEW_AFFECTED_BLOCKS.put(block, block);
				}
				
				displayWaterSpiral(location.clone().add(.5, 0, .5));
				if (player.getLocation().getBlockY() > block.getY()) {
					player.setFlying(false);
				} else {
					new Flight(player);
					player.setAllowFlight(true);
					player.setFlying(true);
				}
			} else {
				remove();
				return;
			}
		}
	}

	@Override
	public void remove() {
		super.remove();
		revertBaseBlock();
	}

	public void revertBaseBlock() {
		if (baseBlock != null) {
			baseBlock.revertBlock();
			baseBlock = null;
		}
	}

	public void rotateParticles(Block block) {
		if (!useParticles) {
			return;
		}

		if (System.currentTimeMillis() >= time + interval) {
			time = System.currentTimeMillis();

			Location location = block.getLocation();
			Location playerLoc = player.getLocation();
			location = new Location(location.getWorld(), playerLoc.getX(), location.getY(), playerLoc.getZ());

			double dy = playerLoc.getY() - block.getY();
			if (dy > height) {
				dy = height;
			}
			
			float[] directions = { -0.5f, 0.325f, 0.25f, 0.125f, 0.f, 0.125f, 0.25f, 0.325f, 0.5f };
			int index = angle;
			angle++;
			if (angle >= directions.length) {
				angle = 0;
			}
			for (int i = 1; i <= dy; i++) {
				index += 1;
				if (index >= directions.length) {
					index = 0;
				}

				Location effectLoc2 = new Location(location.getWorld(), location.getX(), block.getY() + i, location.getZ());
				ParticleEffect.WATER_SPLASH.display(effectLoc2, directions[index], directions[index], directions[index], 5, (int) (height + 5));
			}
		}
	}

	private double spoutableWaterHeight(Location location) {
		double newHeight = height;
		if (isNight(player.getWorld())) {
			newHeight = waterbendingNightAugment(newHeight);
		}
		
		double maxHeight = (height * ProjectKorra.plugin.getConfig().getDouble("Properties.Water.NightFactor")) + 5;
		Block blocki;
		
		for (int i = 0; i < maxHeight; i++) {
			blocki = location.clone().add(0, -i, 0).getBlock();
			if (GeneralMethods.isRegionProtectedFromBuild(this, blocki.getLocation())) {
				return -1;
			}
			
			if (!AFFECTED_BLOCKS.contains(blocki)) {
				if (isWater(blocki)) {
					if (!TempBlock.isTempBlock(blocki)) {
						revertBaseBlock();
					}
					
					base = blocki;
					if (i > newHeight) {
						return newHeight;
					}
					return i;
				}
				
				if (isIcebendable(blocki) || isSnow(blocki)) {
					if (!TempBlock.isTempBlock(blocki)) {
						revertBaseBlock();
						baseBlock = new TempBlock(blocki, Material.STATIONARY_WATER, (byte) 8);
					}
					
					base = blocki;
					if (i > newHeight) {
						return newHeight;
					}
					return i;
				}
				if ((blocki.getType() != Material.AIR && (!isPlant(blocki) || !bPlayer.canPlantbend()))) {
					revertBaseBlock();
					return -1;
				}
			}
		}
		revertBaseBlock();
		return -1;
	}

	public static void progressAllCleanup() {
		NEW_AFFECTED_BLOCKS.clear();
		revertAllBlocks(false);

		for (Block block : AFFECTED_BLOCKS.keySet()) {
			if (!NEW_AFFECTED_BLOCKS.containsKey(block)) {
				AFFECTED_BLOCKS.remove(block);
				TempBlock.revertBlock(block, Material.AIR);
			}
		}
	}

	private static void revertAllBlocks(boolean ignoreTime) {
		for (Block block : REVERT_BLOCKS.keySet()) {
			long time = REVERT_BLOCKS.get(block);
			if (System.currentTimeMillis() > time || ignoreTime) {
				if (TempBlock.isTempBlock(block)) {
					TempBlock.revertBlock(block, Material.AIR);
				}
				REVERT_BLOCKS.remove(block);
			}
		}
	}

	public static void removeAllCleanup() {
		revertAllBlocks(true);
		REVERT_BLOCKS.clear();

		for (Block block : AFFECTED_BLOCKS.keySet()) {
			TempBlock.revertBlock(block, Material.AIR);
			AFFECTED_BLOCKS.remove(block);
		}
	}

	public static boolean removeSpouts(Location loc0, double radius, Player sourcePlayer) {
		boolean removed = false;
		Location loc1 = sourcePlayer.getLocation().getBlock().getLocation();
		loc0 = loc0.getBlock().getLocation();
		double dx = loc1.getX() - loc0.getX();
		double dy = loc1.getY() - loc0.getY();
		double dz = loc1.getZ() - loc0.getZ();
		double distSquared = dx * dx + dz * dz;
		
		for (WaterSpout spout : CoreAbility.getAbilities(sourcePlayer, WaterSpout.class)) {
			if (distSquared <= radius * radius && dy > 0 && dy < spout.height) {
				removed = true;
				spout.remove();
			}
		}
		return removed;
	}

	@Override
	public String getName() {
		return "WaterSpout";
	}

	@Override
	public Location getLocation() {
		return player != null ? player.getLocation() : null;
	}

	@Override
	public long getCooldown() {
		return 0;
	}

	public boolean isCanBendOnPackedIce() {
		return canBendOnPackedIce;
	}

	public void setCanBendOnPackedIce(boolean canBendOnPackedIce) {
		this.canBendOnPackedIce = canBendOnPackedIce;
	}

	public boolean isUseParticles() {
		return useParticles;
	}

	public void setUseParticles(boolean useParticles) {
		this.useParticles = useParticles;
	}

	public boolean isUseBlockSpiral() {
		return useBlockSpiral;
	}

	public void setUseBlockSpiral(boolean useBlockSpiral) {
		this.useBlockSpiral = useBlockSpiral;
	}

	public int getAngle() {
		return angle;
	}

	public void setAngle(int angle) {
		this.angle = angle;
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

	public double getRotation() {
		return rotation;
	}

	public void setRotation(double rotation) {
		this.rotation = rotation;
	}

	public double getHeight() {
		return height;
	}

	public void setHeight(double height) {
		this.height = height;
	}

	public Block getBase() {
		return base;
	}

	public void setBase(Block base) {
		this.base = base;
	}

	public TempBlock getBaseBlock() {
		return baseBlock;
	}

	public void setBaseBlock(TempBlock baseBlock) {
		this.baseBlock = baseBlock;
	}

	public static ConcurrentHashMap<Block, Block> getAffectedBlocks() {
		return AFFECTED_BLOCKS;
	}

	public static ConcurrentHashMap<Block, Block> getNewAffectedBlocks() {
		return NEW_AFFECTED_BLOCKS;
	}

	public static ConcurrentHashMap<Block, Long> getRevertBlocks() {
		return REVERT_BLOCKS;
	}

}