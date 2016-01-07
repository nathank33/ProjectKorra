package com.projectkorra.projectkorra.command;

import com.projectkorra.projectkorra.BendingPlayer;
import com.projectkorra.projectkorra.GeneralMethods;
import com.projectkorra.projectkorra.ability.CoreAbility;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Executor for /bending bind. Extends {@link PKCommand}.
 */
public class BindCommand extends PKCommand {

	public BindCommand() {
		super("bind", "/bending bind [Ability] <#>", "This command will bind an ability to the slot you specify (if you specify one), or the slot currently selected in your hotbar (If you do not specify a Slot #).", new String[]{ "bind", "b" });
	}

	@Override
	public void execute(CommandSender sender, List<String> args) {
		if (!hasPermission(sender) || !correctLength(sender, args.size(), 1, 2) || !isPlayer(sender)) {
			return;
		}

		if (!GeneralMethods.abilityExists(args.get(0))) {
			sender.sendMessage(ChatColor.RED + "That ability doesn't exist.");
			return;
		}
		
		CoreAbility coreAbil = CoreAbility.getAbility(args.get(0));
		// bending bind [Ability]
		if (args.size() == 1) {
			bind(sender, coreAbil.getName(), ((Player) sender).getInventory().getHeldItemSlot()+1);
		}

		// bending bind [ability] [#]
		if (args.size() == 2) {
			bind(sender, coreAbil.getName(), Integer.parseInt(args.get(1)));
		}
	}

	private void bind(CommandSender sender, String ability, int slot) {
		if (slot < 1 || slot > 9) {
			sender.sendMessage(ChatColor.RED + "Slot must be an integer between 1 and 9.");
			return;
		}

		BendingPlayer bPlayer = BendingPlayer.getBendingPlayer(sender.getName());
		CoreAbility coreAbil = CoreAbility.getAbility(ability);
		if (!bPlayer.canBind(coreAbil)) {
			sender.sendMessage(ChatColor.RED + "You don't have permission to bend this element.");
			return;
		} else if (!GeneralMethods.getBendingPlayer(sender.getName()).isElementToggled(GeneralMethods.getAbilityElement(ability))) {
			sender.sendMessage(ChatColor.RED + "You have that ability's element toggled off currently.");
		}
		
		String name = coreAbil != null ? coreAbil.getName() : null;
		GeneralMethods.bindAbility((Player) sender, name, slot);
	}
}
