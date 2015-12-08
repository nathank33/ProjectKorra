package com.projectkorra.projectkorra.ability.api;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.configuration.ConfigManager;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CoreAbility implements Ability {

	private static ConcurrentHashMap<Class<? extends CoreAbility>, ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, CoreAbility>>> instances = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<Class<? extends CoreAbility>, Set<CoreAbility>> instancesByClass = new ConcurrentHashMap<>();
	private static Integer idCounter;

	protected long startTime;
	protected Player player;
	protected BendingPlayer bPlayer;
	protected Integer id;
	protected FileConfiguration config;
	
	private boolean hasStarted;

	static {
		idCounter = Integer.MIN_VALUE;
	}

	public CoreAbility(Player player) {
		if (player == null) {
			throw new IllegalArgumentException("Player cannot be null");
		}

		this.player = player;
		this.bPlayer = BendingPlayer.getBendingPlayer(player);
		this.startTime = System.currentTimeMillis();
		this.config = ConfigManager.defaultConfig.get();
		this.hasStarted = false;
	}

	public void start() {
		hasStarted = true;
		Class<? extends CoreAbility> clazz = getClass();
		UUID uuid = player.getUniqueId();

		if (!CoreAbility.instances.containsKey(clazz)) {
			CoreAbility.instances.put(clazz, new ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, CoreAbility>>());
			CoreAbility.instancesByClass.put(clazz, Collections.newSetFromMap(new ConcurrentHashMap<CoreAbility, Boolean>()));
		}
		if (!CoreAbility.instances.get(clazz).containsKey(uuid)) {
			CoreAbility.instances.get(clazz).put(uuid, new ConcurrentHashMap<Integer, CoreAbility>());
		}

		this.id = CoreAbility.idCounter;
		if (CoreAbility.idCounter == Integer.MAX_VALUE) {
			CoreAbility.idCounter = Integer.MIN_VALUE;
		} else {
			CoreAbility.idCounter++;
		}

		CoreAbility.instances.get(clazz).get(uuid).put(this.id, this);
		CoreAbility.instancesByClass.get(clazz).add(this);
	}

	public long getStartTime() {
		return startTime;
	}
	
	public boolean hasStarted() {
		return hasStarted;
	}

	public Player getPlayer() {
		return player;
	}
	
	public BendingPlayer getBendingPlayer() {
		return bPlayer;
	}

	public Integer getId() {
		return id;
	}

	public String getDescription() {
		return config.getString("Properties." + getElementName() + "." + getName() + ".Description");
	}
	
	public FileConfiguration getConfig() {
		return config;
	}

	@Override
	public void remove() {
		ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, CoreAbility>> classMap = CoreAbility.instances.get(getClass());
		ConcurrentHashMap<Integer, CoreAbility> playerMap = classMap.get(player.getUniqueId());
		playerMap.remove(this.id);
		if (playerMap.size() == 0) {
			classMap.remove(playerMap);
		}
		CoreAbility.instancesByClass.get(getClass()).remove(this);
	}

	public static void progressAll() {
		for (Set<CoreAbility> setAbils : instancesByClass.values()) {
			for (CoreAbility abil : setAbils) {
				abil.progress();
			}
		}
	}
	
	public static void progressAll(Class<? extends CoreAbility> clazz) {
		for (CoreAbility abil : getAbilities(clazz)) {
			abil.progress();
		}
	}
	
	public static void removeAll() {
		for (Set<CoreAbility> setAbils : instancesByClass.values()) {
			for (CoreAbility abil : setAbils) {
				abil.remove();
			}
		}
	}

	public static void removeAll(Class<? extends CoreAbility> clazz) {
		for (CoreAbility abil : getAbilities(clazz)) {
			abil.remove();
		}
	}

	public static <T extends CoreAbility> T getAbility(Player player, Class<T> clazz) {
		Collection<T> abils = getAbilities(player, clazz);
		if (abils.iterator().hasNext()) {
			return abils.iterator().next();
		}
		return null;
	}
	
	public static <T extends CoreAbility> boolean hasAbility(Player player, Class<T> clazz) {
		return getAbility(player, clazz) != null;
	}

	@SuppressWarnings("unchecked")
	public static <T extends CoreAbility> Collection<T> getAbilities(Class<T> clazz) {
		if (instancesByClass.get(clazz) == null || instancesByClass.get(clazz).size() == 0) {
			return Collections.emptySet();
		}
		return (Collection<T>) CoreAbility.instancesByClass.get(clazz);
	}

	@SuppressWarnings("unchecked")
	public static <T extends CoreAbility> Collection<T> getAbilities(Player player, Class<T> clazz) {
		if (player == null || instances.get(clazz) == null || instances.get(clazz).get(player.getUniqueId()) == null) {
			return Collections.emptySet();
		}
		return (Collection<T>) instances.get(clazz).get(player.getUniqueId()).values();
	}

	public final ChatColor getElementColor() {
		String element = (this instanceof SubAbility) ? getElementName() + "Sub" : getElementName();
		return ChatColor.valueOf(ConfigManager.getConfig().getString("Properties.Chat.Colors."+element));		
	}	

	public abstract String getElementName();

	public abstract Location getLocation();

	public abstract long getCooldown();

}
