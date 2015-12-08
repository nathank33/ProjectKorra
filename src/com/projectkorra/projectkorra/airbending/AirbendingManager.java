package com.projectkorra.projectkorra.airbending;

import com.projectkorra.projectkorra.ProjectKorra;

import org.bukkit.Bukkit;

public class AirbendingManager implements Runnable {

	public ProjectKorra plugin;

	public AirbendingManager(ProjectKorra plugin) {
		this.plugin = plugin;
	}

	@Override
	public void run() {
		AirBlast.progressOrigins();
		AirPassive.handlePassive(Bukkit.getServer());
		AirBubble.handleBubbles(Bukkit.getServer());
		AirSuction.progressOrigins();
	}

}
