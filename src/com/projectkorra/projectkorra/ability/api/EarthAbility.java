package com.projectkorra.projectkorra.ability.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.concurrent.ConcurrentHashMap;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.EarthColumn;
import com.projectkorra.projectkorra.earthbending.EarthMethods;
import com.projectkorra.projectkorra.earthbending.EarthPassive;
import com.projectkorra.projectkorra.earthbending.LavaFlow;
import com.projectkorra.projectkorra.util.BlockSource;
import com.projectkorra.projectkorra.util.Information;
import com.projectkorra.projectkorra.util.TempBlock;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

public abstract class EarthAbility extends BlockAbility {

	public static final ConcurrentHashMap<Block, Information> MOVED_EARTH = new ConcurrentHashMap<Block, Information>();
	public static final ConcurrentHashMap<Integer, Information> TEMP_AIR_LOCATIONS = new ConcurrentHashMap<Integer, Information>();
	public static final HashSet<Block> PREVENT_EARTHBENDING = new HashSet<Block>();
	public static final Integer[] TRANSPARENT_MATERIAL = { 0, 6, 8, 9, 10, 11, 30, 31, 32, 37, 38, 39, 40, 50, 51, 59, 78, 83,
			106, 175 };
	public static ArrayList<Block> PREVENT_PHYSICS = new ArrayList<Block>();
	private static final ItemStack DIAMOND_PICKAXE = new ItemStack(Material.DIAMOND_PICKAXE);

	public EarthAbility() {
	}

	public EarthAbility(Player player) {
		super(player);
	}

	@Override
	public final String getElementName() {
		return "Earth";
	}

	/**
	 * Gets the EarthColor from the config.
	 * 
	 * @return Config specified ChatColor
	 */
	public static ChatColor getEarthColor() {
		return ChatColor.valueOf(ConfigManager.getConfig().getString("Properties.Chat.Colors.Earth"));
	}

	/**
	 * Finds a valid Earth source for a Player. To use dynamic source selection, use
	 * BlockSource.getEarthSourceBlock() instead of this method. Dynamic source selection saves the
	 * user's previous source for future use.
	 * {@link BlockSource#getEarthSourceBlock(Player, double, com.projectkorra.projectkorra.util.ClickType)}
	 * 
	 * @param range the maximum block selection range.
	 * @return a valid Earth source block, or null if one could not be found.
	 */
	@SuppressWarnings("deprecation")
	public Block getEarthSourceBlock(double range) {
		Block testblock = player.getTargetBlock(getTransparentEarthbending(), (int) range);
		if (isEarthbendable(testblock) || isMetalbendable(testblock)) {
			return testblock;
		}

		Location location = player.getEyeLocation();
		Vector vector = location.getDirection().clone().normalize();

		for (double i = 0; i <= range; i++) {
			Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if (GeneralMethods.isRegionProtectedFromBuild(this, location)) {
				continue;
			} else if (isEarthbendable(block)) {
				return block;
			}
		}
		return null;
	}

	/**
	 * Finds a valid Lava source for a Player. To use dynamic source selection, use
	 * BlockSource.getLavaSourceBlock() instead of this method. Dynamic source selection saves the
	 * user's previous source for future use.
	 * {@link BlockSource#getLavaSourceBlock(Player, double, com.projectkorra.projectkorra.util.ClickType)}
	 * 
	 * @param range the maximum block selection range.
	 * @return a valid Lava source block, or null if one could not be found.
	 */
	@SuppressWarnings("deprecation")
	public Block getLavaSourceBlock(Player player, double range) {
		Location location = player.getEyeLocation();
		Vector vector = location.getDirection().clone().normalize();

		for (double i = 0; i <= range; i++) {
			Block block = location.clone().add(vector.clone().multiply(i)).getBlock();
			if (GeneralMethods.isRegionProtectedFromBuild(this, location)) {
				continue;
			}
			if (isLavabendable(block, player)) {
				if (TempBlock.isTempBlock(block)) {
					TempBlock tb = TempBlock.get(block);
					byte full = 0x0;
					if (tb.getState().getRawData() != full
							&& (tb.getState().getType() != Material.LAVA || tb.getState().getType() != Material.STATIONARY_LAVA)) {
						continue;
					}
				}
				return block;
			}
		}
		return null;
	}

	public int getEarthbendableBlocksLength(Block block, Vector direction, int maxlength) {
		Location location = block.getLocation();
		direction = direction.normalize();
		for (int i = 0; i <= maxlength; i++) {
			double j = (double) i;
			if (!isEarthbendable(location.clone().add(direction.clone().multiply(j)).getBlock())) {
				return i;
			}
		}
		return maxlength;
	}

	public double getMetalAugment(double value) {
		return value * getConfig().getDouble("Properties.Earth.MetalPowerFactor");
	}

	/**
	 * Attempts to find the closest earth block near a given location.
	 * 
	 * @param loc the initial location to search from.
	 * @param radius the maximum radius to search for the earth block.
	 * @param maxVertical the maximum block height difference between the starting location and the
	 *            earth bendable block.
	 * @return an earth bendable block, or null.
	 */
	public Block getNearbyEarthBlock(Location loc, double radius, int maxVertical) {
		if (loc == null) {
			return null;
		}

		int rotation = 30;
		for (int i = 0; i < radius; i++) {
			Vector tracer = new Vector(i, 0, 0);
			for (int deg = 0; deg < 360; deg += rotation) {
				Location searchLoc = loc.clone().add(tracer);
				Block block = GeneralMethods.getTopBlock(searchLoc, maxVertical);

				if (block != null && isEarthbendable(block.getType())) {
					return block;
				}
				tracer = GeneralMethods.rotateXZ(tracer, rotation);
			}
		}
		return null;
	}

	public HashSet<Byte> getTransparentEarthbending() {
		HashSet<Byte> set = new HashSet<Byte>();
		for (int i : TRANSPARENT_MATERIAL) {
			set.add((byte) i);
		}
		return set;
	}

	public boolean isEarthbendable(Material material) {
		return getConfig().getStringList("Properties.Earth.EarthbendableBlocks").contains(material.toString());
	}

	public boolean isEarthbendable(Block block) {
		if (!isEarthbendable(block.getType()) || PREVENT_EARTHBENDING.contains(block)
				|| GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
			return false;
		} else if (isMetal(block) && !bPlayer.canMetalbend()) {
			return false;
		}
		return true;
	}

	public boolean isLava(Block block) {
		return block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA;
	}

	@SuppressWarnings("deprecation")
	public boolean isLavabendable(Block block, Player player) {
		byte full = 0x0;
		if (TempBlock.isTempBlock(block)) {
			TempBlock tblock = TempBlock.instances.get(block);
			if (tblock == null || !LavaFlow.TEMP_LAVA_BLOCKS.contains(tblock)) {
				return false;
			}
		}
		if ((block.getType() == Material.LAVA || block.getType() == Material.STATIONARY_LAVA) && block.getData() == full) {
			return true;
		}
		return false;
	}

	public boolean isMetal(Material material) {
		return getConfig().getStringList("Properties.Earth.MetalBlocks").contains(material.toString());
	}

	public boolean isMetal(Block block) {
		return isMetal(block.getType());
	}

	public boolean isMetalbendable(Material material) {
		return isMetal(material) && bPlayer.canMetalbend();
	}

	public boolean isMetalbendable(Block block) {
		return isMetalbendable(block.getType());
	}

	public boolean isMetalBlock(Block block) {
		if (block.getType() == Material.GOLD_BLOCK || block.getType() == Material.IRON_BLOCK
				|| block.getType() == Material.IRON_ORE || block.getType() == Material.GOLD_ORE
				|| block.getType() == Material.QUARTZ_BLOCK || block.getType() == Material.QUARTZ_ORE) {
			return true;
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	public boolean isTransparentToEarthbending(Block block) {
		return Arrays.asList(TRANSPARENT_MATERIAL).contains(block.getTypeId())
				&& !GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation());
	}

	public void moveEarth(Block block, Vector direction, int chainlength) {
		moveEarth(block, direction, chainlength, true);
	}

	public boolean moveEarth(Block block, Vector direction, int chainlength, boolean throwplayer) {
		if (isEarthbendable(block) && !GeneralMethods.isRegionProtectedFromBuild(this, block.getLocation())) {
			boolean up = false;
			boolean down = false;
			Vector norm = direction.clone().normalize();
			if (norm.dot(new Vector(0, 1, 0)) == 1) {
				up = true;
			} else if (norm.dot(new Vector(0, -1, 0)) == 1) {
				down = true;
			}

			Vector negnorm = norm.clone().multiply(-1);
			Location location = block.getLocation();
			ArrayList<Block> blocks = new ArrayList<Block>();

			for (double j = -2; j <= chainlength; j++) {
				Block checkblock = location.clone().add(negnorm.clone().multiply(j)).getBlock();
				if (!PREVENT_PHYSICS.contains(checkblock)) {
					blocks.add(checkblock);
					PREVENT_PHYSICS.add(checkblock);
				}
			}

			Block affectedblock = location.clone().add(norm).getBlock();
			if (EarthPassive.isPassiveSand(block)) {
				EarthPassive.revertSand(block);
			}

			if (affectedblock == null) {
				return false;
			} else if (isTransparentToEarthbending(affectedblock)) {
				if (throwplayer) {
					for (Entity entity : GeneralMethods.getEntitiesAroundPoint(affectedblock.getLocation(), 1.75)) {
						if (entity instanceof LivingEntity) {
							LivingEntity lentity = (LivingEntity) entity;
							if (lentity.getEyeLocation().getBlockX() == affectedblock.getX()
									&& lentity.getEyeLocation().getBlockZ() == affectedblock.getZ()) {
								if (!(entity instanceof FallingBlock)) {
									entity.setVelocity(norm.clone().multiply(.75));
								}
							}
						} else {
							if (entity.getLocation().getBlockX() == affectedblock.getX()
									&& entity.getLocation().getBlockZ() == affectedblock.getZ()) {
								if (!(entity instanceof FallingBlock)) {
									entity.setVelocity(norm.clone().multiply(.75));
								}
							}
						}
					}
				}
				if (up) {
					Block topblock = affectedblock.getRelative(BlockFace.UP);
					if (topblock.getType() != Material.AIR) {
						GeneralMethods.breakBlock(affectedblock);
					} else if (!affectedblock.isLiquid() && affectedblock.getType() != Material.AIR) {
						moveEarthBlock(affectedblock, topblock);
					}
				} else {
					GeneralMethods.breakBlock(affectedblock);
				}

				moveEarthBlock(block, affectedblock);
				EarthMethods.playEarthbendingSound(block.getLocation());

				for (double i = 1; i < chainlength; i++) {
					affectedblock = location.clone().add(negnorm.getX() * i, negnorm.getY() * i, negnorm.getZ() * i).getBlock();
					if (!isEarthbendable(affectedblock)) {
						if (down) {
							if (isTransparentToEarthbending(affectedblock) && !affectedblock.isLiquid()
									&& affectedblock.getType() != Material.AIR) {
								moveEarthBlock(affectedblock, block);
							}
						}
						break;
					}
					if (EarthPassive.isPassiveSand(affectedblock)) {
						EarthPassive.revertSand(affectedblock);
					}
					if (block == null) {
						for (Block checkblock : blocks) {
							PREVENT_PHYSICS.remove(checkblock);
						}
						return false;
					}
					moveEarthBlock(affectedblock, block);
					block = affectedblock;
				}

				int i = chainlength;
				affectedblock = location.clone().add(negnorm.getX() * i, negnorm.getY() * i, negnorm.getZ() * i).getBlock();
				if (!isEarthbendable(affectedblock)) {
					if (down) {
						if (isTransparentToEarthbending(affectedblock) && !affectedblock.isLiquid()) {
							moveEarthBlock(affectedblock, block);
						}
					}
				}
			} else {
				for (Block checkblock : blocks) {
					PREVENT_PHYSICS.remove(checkblock);
				}
				return false;
			}
			for (Block checkblock : blocks) {
				PREVENT_PHYSICS.remove(checkblock);
			}
			return true;
		}
		return false;
	}

	public void moveEarth(Location location, Vector direction, int chainlength) {
		moveEarth(location, direction, chainlength, true);
	}

	public void moveEarth(Location location, Vector direction, int chainlength, boolean throwplayer) {
		moveEarth(location.getBlock(), direction, chainlength, throwplayer);
	}

	@SuppressWarnings("deprecation")
	public void moveEarthBlock(Block source, Block target) {
		byte full = 0x0;
		Information info;

		if (MOVED_EARTH.containsKey(source)) {
			info = MOVED_EARTH.get(source);
			MOVED_EARTH.remove(source);
		} else {
			info = new Information();
			info.setBlock(source);
			info.setTime(System.currentTimeMillis());
			info.setState(source.getState());
		}
		info.setTime(System.currentTimeMillis());
		MOVED_EARTH.put(target, info);

		if (GeneralMethods.isAdjacentToThreeOrMoreSources(source)) {
			source.setType(Material.WATER);
			source.setData(full);
		} else {
			source.setType(Material.AIR);
		}
		if (info.getState().getType() == Material.SAND) {
			if (info.getState().getRawData() == (byte) 0x1) {
				target.setType(Material.RED_SANDSTONE);
			} else {
				target.setType(Material.SANDSTONE);
			}
		} else {
			target.setType(info.getState().getType());
			target.setData(info.getState().getRawData());
		}
	}

	public static void removeAllEarthbendedBlocks() {
		for (Block block : MOVED_EARTH.keySet()) {
			revertBlock(block);
		}
		for (Integer i : TEMP_AIR_LOCATIONS.keySet()) {
			revertAirBlock(i, true);
		}
	}

	public static void removeRevertIndex(Block block) {
		if (MOVED_EARTH.containsKey(block)) {
			Information info = MOVED_EARTH.get(block);
			if (block.getType() == Material.SANDSTONE && info.getType() == Material.SAND) {
				block.setType(Material.SAND);
			}
			if (EarthColumn.blockInAllAffectedBlocks(block)) {
				EarthColumn.revertBlock(block);
			}

			MOVED_EARTH.remove(block);
		}
	}

	public void revertAirBlock(int i) {
		revertAirBlock(i, false);
	}

	/**
	 * Creates a temporary air block.
	 * 
	 * @param block The block to use as a base
	 */
	public static void addTempAirBlock(Block block) {
		Information info;
		if (MOVED_EARTH.containsKey(block)) {
			info = MOVED_EARTH.get(block);
			MOVED_EARTH.remove(block);
		} else {
			info = new Information();
			info.setBlock(block);
			info.setState(block.getState());
		}
		block.setType(Material.AIR);
		info.setTime(System.currentTimeMillis());
		TEMP_AIR_LOCATIONS.put(info.getID(), info);
	}

	@SuppressWarnings("deprecation")
	public static void revertAirBlock(int i, boolean force) {
		if (!TEMP_AIR_LOCATIONS.containsKey(i)) {
			return;
		}

		Information info = TEMP_AIR_LOCATIONS.get(i);
		Block block = info.getState().getBlock();

		if (block.getType() != Material.AIR && !block.isLiquid()) {
			if (force || !MOVED_EARTH.containsKey(block)) {
				GeneralMethods.dropItems(block,
						GeneralMethods.getDrops(block, info.getState().getType(), info.getState().getRawData(), DIAMOND_PICKAXE));
				TEMP_AIR_LOCATIONS.remove(i);
			} else {
				info.setTime(info.getTime() + 10000);
			}
			return;
		} else {
			info.getState().update(true);
			TEMP_AIR_LOCATIONS.remove(i);
		}
	}

	@SuppressWarnings("deprecation")
	public static boolean revertBlock(Block block) {
		byte full = 0x0;
		if (!ConfigManager.getConfig().getBoolean("Properties.Earth.RevertEarthbending")) {
			MOVED_EARTH.remove(block);
			return false;
		}
		if (MOVED_EARTH.containsKey(block)) {
			Information info = MOVED_EARTH.get(block);
			Block sourceblock = info.getState().getBlock();

			if (info.getState().getType() == Material.AIR) {
				MOVED_EARTH.remove(block);
				return true;
			}

			if (block.equals(sourceblock)) {
				info.getState().update(true);
				if (EarthColumn.blockInAllAffectedBlocks(sourceblock))
					EarthColumn.revertBlock(sourceblock);
				if (EarthColumn.blockInAllAffectedBlocks(block))
					EarthColumn.revertBlock(block);
				MOVED_EARTH.remove(block);
				return true;
			}

			if (MOVED_EARTH.containsKey(sourceblock)) {
				// TODO: if we can remove this nonstatic call then we can clean up a lot of the
				// statics in this class
				addTempAirBlock(block);
				MOVED_EARTH.remove(block);
				return true;
			}

			if (sourceblock.getType() == Material.AIR || sourceblock.isLiquid()) {
				info.getState().update(true);
			} else {
				GeneralMethods.dropItems(block,
						GeneralMethods.getDrops(block, info.getState().getType(), info.getState().getRawData(), DIAMOND_PICKAXE));
			}

			if (GeneralMethods.isAdjacentToThreeOrMoreSources(block)) {
				block.setType(Material.WATER);
				block.setData(full);
			} else {
				block.setType(Material.AIR);
			}

			if (EarthColumn.blockInAllAffectedBlocks(sourceblock))
				EarthColumn.revertBlock(sourceblock);
			if (EarthColumn.blockInAllAffectedBlocks(block))
				EarthColumn.revertBlock(block);
			MOVED_EARTH.remove(block);
		}
		return true;
	}

}
