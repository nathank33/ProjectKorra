package com.projectkorra.projectkorra.ability.api;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.configuration.ConfigManager;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public abstract class CoreAbility implements Ability {

	private static final ConcurrentHashMap<Class<? extends CoreAbility>, ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, CoreAbility>>> INSTANCES = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<Class<? extends CoreAbility>, Set<CoreAbility>> INSTANCES_BY_CLASS = new ConcurrentHashMap<>();
	private static final ConcurrentHashMap<String, CoreAbility> ABILITIES_BY_NAME = new ConcurrentHashMap<>();
	private static final String INVALID_PLAYER = "Player is null, make sure the first line if your ability is super(player)";
	private static int idCounter;

	protected long startTime;
	protected Player player;
	protected BendingPlayer bPlayer;
	
	private int id;
	private boolean hasStarted;
	private boolean hasBeenRemoved;

	static {
		idCounter = Integer.MIN_VALUE;
		registerAbilities(ProjectKorra.class);
	}

	public CoreAbility() {}

	public CoreAbility(Player player) {
		if (player == null) {
			throw new IllegalArgumentException("Player cannot be null");
		}
		this.player = player;
		this.bPlayer = BendingPlayer.getBendingPlayer(player);
		this.startTime = System.currentTimeMillis();
		this.hasStarted = false;
		this.id = CoreAbility.idCounter;
		
		if (CoreAbility.idCounter == Integer.MAX_VALUE) {
			CoreAbility.idCounter = Integer.MIN_VALUE;
		} else {
			CoreAbility.idCounter++;
		}
	}

	public void start() {
		if (player == null) {
			throw new IllegalStateException(INVALID_PLAYER);
		}
		
		this.hasStarted = true;
		this.startTime = System.currentTimeMillis();
		Class<? extends CoreAbility> clazz = getClass();
		UUID uuid = player.getUniqueId();

		if (!CoreAbility.INSTANCES.containsKey(clazz)) {
			CoreAbility.INSTANCES.put(clazz, new ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, CoreAbility>>());
		}
		if (!CoreAbility.INSTANCES.get(clazz).containsKey(uuid)) {
			CoreAbility.INSTANCES.get(clazz).put(uuid, new ConcurrentHashMap<Integer, CoreAbility>());
		}
		if (!CoreAbility.INSTANCES_BY_CLASS.containsKey(clazz)) {
			CoreAbility.INSTANCES_BY_CLASS.put(clazz, Collections.newSetFromMap(new ConcurrentHashMap<CoreAbility, Boolean>()));
		}

		CoreAbility.INSTANCES.get(clazz).get(uuid).put(this.id, this);
		CoreAbility.INSTANCES_BY_CLASS.get(clazz).add(this);
	}

	@Override
	public void remove() {
		if (player == null) {
			throw new IllegalStateException(INVALID_PLAYER);
		}
		
		hasBeenRemoved = true;
		
		ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, CoreAbility>> classMap = CoreAbility.INSTANCES.get(getClass());
		if (classMap != null) {
			ConcurrentHashMap<Integer, CoreAbility> playerMap = classMap.get(player.getUniqueId());
			if (playerMap != null) {
				playerMap.remove(this.id);
				if (playerMap.size() == 0) {
					classMap.remove(player.getUniqueId());
				}
			}
		}

		if (CoreAbility.INSTANCES_BY_CLASS.containsKey(getClass())) {
			CoreAbility.INSTANCES_BY_CLASS.get(getClass()).remove(this);
		}
	}

	public static void progressAll() {
		for (Set<CoreAbility> setAbils : INSTANCES_BY_CLASS.values()) {
			for (CoreAbility abil : setAbils) {
				abil.progress();
			}
		}
	}

	public static void removeAll() {
		for (Set<CoreAbility> setAbils : INSTANCES_BY_CLASS.values()) {
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
	
	public static CoreAbility getAbility(String abilityName) {
		return abilityName != null ? ABILITIES_BY_NAME.get(abilityName) : null;
	}
	
	public static ArrayList<CoreAbility> getAbilities() {
		return new ArrayList<CoreAbility>(ABILITIES_BY_NAME.values());
	}

	@SuppressWarnings("unchecked")
	public static <T extends CoreAbility> Collection<T> getAbilities(Class<T> clazz) {
		if (clazz == null || INSTANCES_BY_CLASS.get(clazz) == null || INSTANCES_BY_CLASS.get(clazz).size() == 0) {
			return Collections.emptySet();
		}
		return (Collection<T>) CoreAbility.INSTANCES_BY_CLASS.get(clazz);
	}

	@SuppressWarnings("unchecked")
	public static <T extends CoreAbility> Collection<T> getAbilities(Player player, Class<T> clazz) {
		if (player == null || clazz == null || INSTANCES.get(clazz) == null || INSTANCES.get(clazz).get(player.getUniqueId()) == null) {
			return Collections.emptySet();
		}
		return (Collection<T>) INSTANCES.get(clazz).get(player.getUniqueId()).values();
	}
	
	public static ArrayList<CoreAbility> getAbilitiesByElement(String element) {
		ArrayList<CoreAbility> abilities = new ArrayList<CoreAbility>();
		if (element != null) {
			for (CoreAbility ability : getAbilities()) {
				if (ability.getElementName().equalsIgnoreCase(element)) {
					abilities.add(ability);
				}
			}
		}
		return abilities;
	}
	
	public static <T extends CoreAbility> boolean hasAbility(Player player, Class<T> clazz) {
		return getAbility(player, clazz) != null;
	}
	
	public static HashSet<Player> getPlayers(Class<? extends CoreAbility> clazz) {
		HashSet<Player> players = new HashSet<>();
		if (clazz != null) {
			ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, CoreAbility>> uuidMap = INSTANCES.get(clazz);
			if (uuidMap != null) {
				for (UUID uuid : uuidMap.keySet()) {
					Player uuidPlayer = Bukkit.getPlayer(uuid);
					if (uuidPlayer != null) {
						players.add(uuidPlayer);
					}
				}
			}
		}
		return players;
	}

	public static void registerAbilities(Class<?> pluginClass) {
		ClassLoader loader = pluginClass.getClassLoader();
		
		try {
			for (final ClassInfo info : ClassPath.from(loader).getTopLevelClasses()) {
				Class<?> clazz = null;
				
				try {
					clazz = info.load();
					if (!CoreAbility.class.isAssignableFrom(clazz) || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
						continue;
					}

					Constructor<?> constructor = clazz.getConstructor();
					CoreAbility ability = (CoreAbility) constructor.newInstance();
					ABILITIES_BY_NAME.put(ability.getName(), ability); 
				} catch (NoSuchMethodException e) {
					if (clazz != null) {
						String msg = clazz.getName() + " is a CoreAbility and needs a default constructor.";
						ProjectKorra.log.info(msg);
						throw new IllegalStateException(msg);
					}
				} catch (Exception e) {
				} catch (Error e) {
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public final ChatColor getElementColor() {
		String element = (this instanceof SubAbility) ? getElementName() + "Sub" : getElementName();
		return ChatColor.valueOf(ConfigManager.getConfig().getString("Properties.Chat.Colors." + element));
	}

	public abstract String getElementName();

	public abstract Location getLocation();

	public abstract long getCooldown();
	
	public long getStartTime() {
		return startTime;
	}

	public boolean hasStarted() {
		return hasStarted;
	}
	
	public boolean hasBeenRemoved() {
		return hasBeenRemoved;
	}

	public Player getPlayer() {
		return player;
	}

	public BendingPlayer getBendingPlayer() {
		return bPlayer;
	}

	public int getId() {
		return id;
	}

	public String getDescription() {
		return getConfig().getString("Properties." + getElementName() + "." + getName() + ".Description");
	}

	public static FileConfiguration getConfig() {
		return ConfigManager.getConfig();
	}
	
}
