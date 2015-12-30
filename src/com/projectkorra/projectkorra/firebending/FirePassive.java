package com.projectkorra.projectkorra.firebending;

import com.projectkorra.projectkorra.BendingPlayer;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

public class FirePassive {

	public static void handlePassive() {
		for (Player player : Bukkit.getOnlinePlayers()) {
			BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(player);
			if (bPlayer != null && bPlayer.canBendPassive("Fire")) {
				if (player.getFireTicks() > 80) {
					player.setFireTicks(80);
				}
			}
		}
	}
}
