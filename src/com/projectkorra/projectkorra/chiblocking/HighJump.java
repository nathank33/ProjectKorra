package com.projectkorra.projectkorra.chiblocking;

import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ProjectKorra;
import com.projectkorra.projectkorra.ability.api.ChiAbility;
import com.projectkorra.projectkorra.waterbending.WaterArmsWhip;

import org.bukkit.Location;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class HighJump extends ChiAbility {

	private int jumpheight = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.HighJump.Height");
	private long cooldown = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.HighJump.Cooldown");

	public HighJump () {}
	
	public HighJump(Player player) {
		super(player);
		jumpheight = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.HighJump.Height");
		cooldown = ProjectKorra.plugin.getConfig().getInt("Abilities.Chi.HighJump.Cooldown");
		start();
	}

	private void jump(Player p) {
		if (!GeneralMethods.isSolid(p.getLocation().getBlock().getRelative(BlockFace.DOWN)))
			return;
		Vector vec = p.getVelocity();
		vec.setY(jumpheight);
		p.setVelocity(vec);
		return;
	}

	public String getDescription() {
		return "To use this ability, simply click. You will jump quite high. This ability has a short cooldown.";
	}

	@Override
	public String getName() {
		return "HighJump";
	}

	@Override
	public void progress() {
		if (bPlayer.isOnCooldown("HighJump")) {
			remove();
			return;
		}

		jump(player);
		if (WaterArmsWhip.grabbedEntities.containsKey(player)) {
			WaterArmsWhip waw = WaterArmsWhip.instances.get(WaterArmsWhip.grabbedEntities.get(player));
			if (waw != null) {
				waw.setGrabbed(false);
			}
		}
		bPlayer.addCooldown("HighJump", cooldown);
	}

	@Override
	public Location getLocation() {
		return player.getLocation();
	}

	@Override
	public long getCooldown() {
		return cooldown;
	}
}
