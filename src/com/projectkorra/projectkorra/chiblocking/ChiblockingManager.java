package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.ProjectKorra;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class ChiblockingManager implements Runnable {
	public ProjectKorra plugin;

	public ChiblockingManager(ProjectKorra plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		ChiPassive.handlePassive();
		for (Player player : Bukkit.getOnlinePlayers()) {
			Smokescreen.removeFromHashMap(player);
			if (Paralyze.isParalyzed(player)) {
				player.setFallDistance(0F);
			}
		}
	}

}
