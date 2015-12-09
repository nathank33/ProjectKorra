package com.projectkorra.projectkorra.ability.api;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.configuration.ConfigManager;

public abstract class CoreAbility implements Ability {

	private static ConcurrentHashMap<Class<? extends CoreAbility>, ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, CoreAbility>>> instances = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<Class<? extends CoreAbility>, Set<CoreAbility>> instancesByClass = new ConcurrentHashMap<>();
	private static ConcurrentHashMap<String, CoreAbility> abilitiesByName = new ConcurrentHashMap<>();
	private static Integer idCounter;
	private static final String INVALID_PLAYER = "Player is null, make sure the first line if your ability is super(player)";

	protected long startTime;
	protected Player player;
	protected BendingPlayer bPlayer;
	protected Integer id;
	private FileConfiguration config;
	private boolean hasStarted;

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
		this.config = ConfigManager.defaultConfig.get();
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
		hasStarted = true;
		Class<? extends CoreAbility> clazz = getClass();
		UUID uuid = player.getUniqueId();

		if (!CoreAbility.instances.containsKey(clazz)) {
			CoreAbility.instances.put(clazz, new ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, CoreAbility>>());
		}
		if (!CoreAbility.instances.get(clazz).containsKey(uuid)) {
			CoreAbility.instances.get(clazz).put(uuid, new ConcurrentHashMap<Integer, CoreAbility>());
		}
		if (!CoreAbility.instancesByClass.containsKey(clazz)) {
			CoreAbility.instancesByClass.put(clazz, Collections.newSetFromMap(new ConcurrentHashMap<CoreAbility, Boolean>()));
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
		if (player == null) {
			throw new IllegalStateException(INVALID_PLAYER);
		}
		
		ConcurrentHashMap<UUID, ConcurrentHashMap<Integer, CoreAbility>> classMap = CoreAbility.instances.get(getClass());
		if (classMap != null) {
			ConcurrentHashMap<Integer, CoreAbility> playerMap = classMap.get(player.getUniqueId());
			if (playerMap != null) {
				playerMap.remove(this.id);
				if (playerMap.size() == 0) {
					classMap.remove(playerMap);
				}
			}
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
	
	public static CoreAbility getAbility(String abilityName) {
		return abilitiesByName.get(abilityName);
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
					abilitiesByName.put(ability.getName(), ability); 
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

}
