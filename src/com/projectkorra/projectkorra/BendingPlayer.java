package com.projectkorra.projectkorra;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.projectkorra.projectkorra.ability.AvatarState;
import com.projectkorra.projectkorra.ability.api.Ability;
import com.projectkorra.projectkorra.ability.api.CoreAbility;
import com.projectkorra.projectkorra.ability.api.FireAbility;
import com.projectkorra.projectkorra.ability.api.SubAbility;
import com.projectkorra.projectkorra.ability.api.WaterAbility;
import com.projectkorra.projectkorra.chiblocking.ChiMethods;
import com.projectkorra.projectkorra.chiblocking.Paralyze;
import com.projectkorra.projectkorra.command.Commands;
import com.projectkorra.projectkorra.configuration.ConfigManager;
import com.projectkorra.projectkorra.earthbending.MetalClips;
import com.projectkorra.projectkorra.event.PlayerCooldownChangeEvent;
import com.projectkorra.projectkorra.event.PlayerCooldownChangeEvent.Result;
import com.projectkorra.projectkorra.storage.DBConnection;
import com.projectkorra.projectkorra.waterbending.Bloodbending;

/**
 * Class that presents a player and stores all bending information about the player.
 */
public class BendingPlayer {

	/**
	 * ConcurrentHashMap that contains all instances of BendingPlayer, with UUID key.
	 */
	private static ConcurrentHashMap<UUID, BendingPlayer> players = new ConcurrentHashMap<>();

	private UUID uuid;
	private String name;
	private Player player;
	private FileConfiguration config;
	private ArrayList<Element> elements;
	private HashMap<Integer, String> abilities;
	private ConcurrentHashMap<String, Long> cooldowns;
	private ConcurrentHashMap<Element, Boolean> toggledElements;
	private boolean permaRemoved;
	private boolean toggled = true;
	private long slowTime = 0;
	private boolean tremorSense = true;
	private boolean chiBlocked = false;

	/**
	 * Creates a new {@link BendingPlayer}.
	 * 
	 * @param uuid The unique identifier
	 * @param playerName The playername
	 * @param elements The known elements
	 * @param abilities The known abilities
	 * @param permaRemoved The permanent removed status
	 */
	public BendingPlayer(UUID uuid, String playerName, ArrayList<Element> elements, HashMap<Integer, String> abilities,
			boolean permaRemoved) {
		this.uuid = uuid;
		this.name = playerName;
		this.elements = elements;
		this.setAbilities(abilities);
		this.permaRemoved = permaRemoved;
		this.player = Bukkit.getPlayer(uuid);
		this.config = ConfigManager.getConfig();
		cooldowns = new ConcurrentHashMap<String, Long>();
		toggledElements = new ConcurrentHashMap<Element, Boolean>();
		toggledElements.put(Element.Air, true);
		toggledElements.put(Element.Earth, true);
		toggledElements.put(Element.Fire, true);
		toggledElements.put(Element.Water, true);
		toggledElements.put(Element.Chi, true);

		players.put(uuid, this);
		PKListener.login(this);
	}

	/**
	 * Gets the map of {@link BendingPlayer}s.
	 * 
	 * @return {@link #players}
	 */
	public static ConcurrentHashMap<UUID, BendingPlayer> getPlayers() {
		return players;
	}

	/**
	 * Adds an ability to the cooldowns map while firing a {@link PlayerCooldownChangeEvent}.
	 * 
	 * @param ability Name of the ability
	 * @param cooldown The cooldown time
	 */
	public void addCooldown(String ability, long cooldown) {
		PlayerCooldownChangeEvent event = new PlayerCooldownChangeEvent(Bukkit.getPlayer(uuid), ability, Result.ADDED);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			this.cooldowns.put(ability, cooldown + System.currentTimeMillis());
		}
	}

	public void addCooldown(Ability ability, long cooldown) {
		addCooldown(ability.getName(), cooldown);
	}

	public void addCooldown(CoreAbility ability) {
		addCooldown(ability, ability.getCooldown());
	}

	/**
	 * Adds an element to the {@link BendingPlayer}'s known list.
	 * 
	 * @param e The element to add
	 */
	public void addElement(Element e) {
		this.elements.add(e);
	}

	/**
	 * Sets chiBlocked to true.
	 */
	public void blockChi() {
		chiBlocked = true;
	}

	/**
	 * Checks to see if {@link BendingPlayer} can be slowed.
	 * 
	 * @return true If player can be slowed
	 */
	public boolean canBeSlowed() {
		return (System.currentTimeMillis() > slowTime);
	}

	/**
	 * Checks to see if a player can use Flight.
	 * 
	 * @return true If player has permission node "bending.air.flight"
	 */
	public boolean canUseFlight() {
		return player.hasPermission("bending.air.flight");
	}

	/**
	 * Checks to see if a player can use SpiritualProjection.
	 * 
	 * @param player The player to check
	 * @return true If player has permission node "bending.air.spiritualprojection"
	 */
	public boolean canUseSpiritualProjection() {
		return player.hasPermission("bending.air.spiritualprojection");
	}

	/**
	 * Checks to see if a Player is effected by BloodBending.
	 * 
	 * @return true If {@link ChiMethods#isChiBlocked(String)} is true <br />
	 *         false If player is BloodBender and Bending is toggled on, or if player is in
	 *         AvatarState
	 */
	public boolean canBeBloodbent() {
		if (AvatarState.isAvatarState(player)) {
			if (ChiMethods.isChiBlocked(name)) {
				return true;
			}
		}
		if (GeneralMethods.canBend(name, "Bloodbending") && !isToggled()) {
			return false;
		}
		return true;
	}

	/**
	 * Checks to see if a player can BloodBend.
	 * 
	 * @return true If player has permission node "bending.earth.bloodbending"
	 */
	public boolean canBloodbend() {
		return player.hasPermission("bending.water.bloodbending");
	}

	public boolean canBloodbendAtAnytime() {
		return canBloodbend() && player.hasPermission("bending.water.bloodbending.anytime");
	}

	public boolean canIcebend() {
		return player.hasPermission("bending.water.icebending");

	}

	/**
	 * Checks to see if a player can PlantBend.
	 * 
	 * @param player The player to check
	 * @return true If player has permission node "bending.ability.plantbending"
	 */
	public static boolean canPlantbend(Player player) {
		return player.hasPermission("bending.water.plantbending");
	}

	public boolean canWaterHeal() {
		return player.hasPermission("bending.water.healing");
	}

	/**
	 * Checks to see if a player can SandBend.
	 * 
	 * @param player The player to check
	 * @return true If player has permission node "bending.earth.sandbending"
	 */
	public boolean canSandbend() {
		return player.hasPermission("bending.earth.sandbending");
	}

	/**
	 * Checks to see if a player can MetalBend.
	 * 
	 * @param player The player to check
	 * @return true If player has permission node "bending.earth.metalbending"
	 */
	public boolean canMetalbend() {
		return player.hasPermission("bending.earth.metalbending");
	}

	/**
	 * Checks to see if a player can LavaBend.
	 * 
	 * @param player The player to check
	 * @return true If player has permission node "bending.earth.lavabending"
	 */
	public static boolean canLavabend(Player player) {
		return player.hasPermission("bending.earth.lavabending");
	}

	public static boolean canCombustionbend(Player player) {
		return player.hasPermission("bending.fire.combustionbending");
	}

	public static boolean canLightningbend(Player player) {
		return player.hasPermission("bending.fire.lightningbending");
	}

	/**
	 * Gets the map of abilities that the {@link BendingPlayer} knows.
	 * 
	 * @return map of abilities
	 */
	public HashMap<Integer, String> getAbilities() {
		return this.abilities;
	}

	/**
	 * Gets the cooldown time of the ability.
	 * 
	 * @param ability The ability to check
	 * @return the cooldown time
	 *         <p>
	 *         or -1 if cooldown doesn't exist
	 *         </p>
	 */
	public long getCooldown(String ability) {
		if (cooldowns.containsKey(ability)) {
			return cooldowns.get(ability);
		}
		return -1;
	}

	/**
	 * Gets the map of cooldowns of the {@link BendingPlayer}.
	 * 
	 * @return map of cooldowns
	 */
	public ConcurrentHashMap<String, Long> getCooldowns() {
		return cooldowns;
	}

	/**
	 * Gets the list of elements the {@link BendingPlayer} knows.
	 * 
	 * @return a list of elements
	 */
	public List<Element> getElements() {
		return this.elements;
	}

	/**
	 * Gets the name of the {@link BendingPlayer}.
	 * 
	 * @return the player name
	 */
	public String getName() {
		return this.name;
	}

	/**
	 * Gets the unique identifier of the {@link BendingPlayer}.
	 * 
	 * @return the uuid
	 */
	public UUID getUUID() {
		return this.uuid;
	}

	/**
	 * Convenience method to {@link #getUUID()} as a string.
	 * 
	 * @return string version of uuid
	 */
	public String getUUIDString() {
		return this.uuid.toString();
	}

	/**
	 * Checks to see if the {@link BendingPlayer} knows a specific element.
	 * 
	 * @param e The element to check
	 * @return true If the player knows the element
	 */
	public boolean hasElement(Element e) {
		return this.elements.contains(e);
	}

	public boolean hasElement(String elementName) {
		// TODO: Finish this
		return true;
	}

	/**
	 * Checks to see if the {@link BendingPlayer} is chi blocked.
	 * 
	 * @return true If the player is chi blocked
	 */
	public boolean isChiBlocked() {
		return this.chiBlocked;
	}

	public boolean isParalyzed() {
		return Paralyze.isParalyzed(player);
	}

	public boolean isBloodbended() {
		return Bloodbending.isBloodbended(player);
	}

	public boolean isControlledByMetalClips() {
		return MetalClips.isControlled(player);
	}

	public boolean isElementToggled(Element e) {
		if (e != null)
			return this.toggledElements.get(e);
		return true;
	}

	/**
	 * Checks to see if a specific ability is on cooldown.
	 * 
	 * @param ability The ability name to check
	 * @return true if the cooldown map contains the ability
	 */
	public boolean isOnCooldown(String ability) {
		return this.cooldowns.containsKey(ability);
	}

	public boolean isOnCooldown(Ability ability) {
		return isOnCooldown(ability.getName());
	}

	/**
	 * Checks if the {@link BendingPlayer} is permaremoved.
	 * 
	 * @return true If the player is permaremoved
	 */
	public boolean isPermaRemoved() {
		return this.permaRemoved;
	}

	/**
	 * Checks if the {@link BendingPlayer} has bending toggled on.
	 * 
	 * @return true If bending is toggled on
	 */
	public boolean isToggled() {
		return this.toggled;
	}

	public boolean isElementToggled(String elementName) {
		// TODO: Finish this
		return true;
	}

	/**
	 * Checks if the {@link BendingPlayer} is tremor sensing.
	 * 
	 * @return true if player is tremor sensing
	 */
	public boolean isTremorSensing() {
		return this.tremorSense;
	}

	/**
	 * Removes the cooldown of an ability.
	 * 
	 * @param ability The ability's cooldown to remove
	 */
	public void removeCooldown(String ability) {
		PlayerCooldownChangeEvent event = new PlayerCooldownChangeEvent(Bukkit.getPlayer(uuid), ability, Result.REMOVED);
		Bukkit.getServer().getPluginManager().callEvent(event);
		if (!event.isCancelled()) {
			this.cooldowns.remove(ability);
		}
	}

	/**
	 * Sets the {@link BendingPlayer}'s abilities. This method also saves the abilities to the
	 * database.
	 * 
	 * @param abilities The abilities to set/save
	 */
	public void setAbilities(HashMap<Integer, String> abilities) {
		this.abilities = abilities;
		for (int i = 1; i <= 9; i++) {
			DBConnection.sql.modifyQuery("UPDATE pk_players SET slot" + i + " = '" + abilities.get(i) + "' WHERE uuid = '" + uuid
					+ "'");
		}
	}

	/**
	 * Sets the {@link BendingPlayer}'s element. If the player had elements before they will be
	 * overwritten.
	 * 
	 * @param e The element to set
	 */
	public void setElement(Element e) {
		this.elements.clear();
		this.elements.add(e);
	}

	/**
	 * Sets the permanent removed state of the {@link BendingPlayer}.
	 * 
	 * @param permaRemoved
	 */
	public void setPermaRemoved(boolean permaRemoved) {
		this.permaRemoved = permaRemoved;
	}

	/**
	 * Slow the {@link BendingPlayer} for a certain amount of time.
	 * 
	 * @param cooldown The amount of time to slow.
	 */
	public void slow(long cooldown) {
		slowTime = System.currentTimeMillis() + cooldown;
	}

	/**
	 * Toggles the {@link BendingPlayer}'s bending.
	 */
	public void toggleBending() {
		toggled = !toggled;
	}

	public void toggleElement(Element e) {
		toggledElements.put(e, !toggledElements.get(e));
	}

	/**
	 * Toggles the {@link BendingPlayer}'s tremor sensing.
	 */
	public void toggleTremorSense() {
		tremorSense = !tremorSense;
	}

	/**
	 * Sets the {@link BendingPlayer}'s chi blocked to false.
	 */
	public void unblockChi() {
		chiBlocked = false;
	}

	public boolean canBend(CoreAbility ability) {
		return canBend(ability, false, false);
	}

	public boolean canBendIgnoreCooldowns(CoreAbility ability) {
		return canBend(ability, false, true);
	}

	public boolean canBendIgnoreBinds(CoreAbility ability) {
		return canBend(ability, true, false);
	}

	private boolean canBend(CoreAbility ability, boolean ignoreBinds, boolean ignoreCooldowns) {
		List<String> disabledWorlds = config.getStringList("Properties.DisabledWorlds");
		if (player == null || !player.isOnline() || player.isDead()) {
			return false;
		} else if (!ignoreCooldowns && isOnCooldown(ability.getName())) {
			return false;
		} else if (!ignoreBinds && !ability.getName().equals(getBoundAbility())) {
			return false;
		} else if (disabledWorlds != null && disabledWorlds.contains(player.getWorld().getName())) {
			return false;
		} else if (Commands.isToggledForAll || !isToggled() || !isElementToggled(ability.getName())) {
			return false;
		} else if (player.getGameMode() == GameMode.SPECTATOR) {
			return false;
		}
		if (!ignoreCooldowns && cooldowns.containsKey(name)) { // TODO: wtf is this
			if (cooldowns.get(name) + config.getLong("Properties.GlobalCooldown") >= System.currentTimeMillis()) {
				return false;
			}
			cooldowns.remove(name);
		}

		if (isChiBlocked() || isParalyzed() || isBloodbended() || isControlledByMetalClips()) {
			return false;
		} else if (GeneralMethods.isRegionProtectedFromBuild(player, ability.getName(), player.getLocation())) {
			return false;
		} else if (ability instanceof FireAbility && BendingManager.events.get(player.getWorld()) != null
				&& BendingManager.events.get(player.getWorld()).equalsIgnoreCase("SolarEclipse")) {
			return false;
		} else if (ability instanceof WaterAbility && BendingManager.events.get(player.getWorld()) != null
				&& BendingManager.events.get(player.getWorld()).equalsIgnoreCase("LunarEclipse")) {
			return false;
		} else if (!ignoreBinds && !canBind(ability)) {
			return false;
		}
		return true;
	}

	public boolean canBind(CoreAbility ability) {
		if (player == null && player.isOnline()) {
			return false;
		} else if (!player.hasPermission("bending.ability." + ability.getName())) {
			return false;
		} else if (!hasElement(ability.getElementName())) {
			return false;
		} else if (ability instanceof SubAbility) {
			SubAbility subAbil = (SubAbility) ability;
			if (!hasElement(subAbil.getSubElementName())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Attempts to get a {@link BendingPlayer} from specified player name. this method tries to get
	 * a {@link Player} object and gets the uuid and then calls {@link #getBendingPlayer(UUID)}
	 * 
	 * @param playerName The name of the Player
	 * @return The BendingPlayer object if {@link BendingPlayer#players} contains the player name
	 * 
	 * @see #getBendingPlayer(UUID)
	 */
	public static BendingPlayer getBendingPlayer(String player) {
		OfflinePlayer oPlayer = Bukkit.getPlayer(player);
		if (player == null) {
			oPlayer = Bukkit.getOfflinePlayer(oPlayer.getUniqueId());
		}
		return getBendingPlayer(oPlayer);
	}
	
	public static BendingPlayer getBendingPlayer(OfflinePlayer oPlayer) {
		return BendingPlayer.getPlayers().get(oPlayer.getUniqueId());
	}

	public static BendingPlayer getBendingPlayer(Player player) {
		return getBendingPlayer(player.getName());
	}
	
	/**
	 * Gets the Ability bound to the slot that the player is in.
	 * 
	 * @return The Ability name bounded to the slot
	 *         <p>
	 *         else null
	 *         </p>
	 */
	public String getBoundAbility() {
		int slot = player.getInventory().getHeldItemSlot() + 1;
		return getAbilities().get(slot);
	}

}
