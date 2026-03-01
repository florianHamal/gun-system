package at.flori4n.gunsystem;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

public class GunCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        Player player = (Player) sender;
        ItemStack item = player.getItemInHand();

        if (item == null || item.getType() == Material.AIR) {
            player.sendMessage("You must hold an item in your hand!");
            return true;
        }

        if (args.length < 6) {
            player.sendMessage("Usage: /creategun <name> <magazineSize> <shootingSpeed> <reloadTime> <damage> <shootingDebounce> <reloadingDebounce>");
            return true;
        }

        try {
            String name = args[0];
            int magazineSize = Integer.parseInt(args[1]);
            int currentMagazineLoad = magazineSize;
            long shootingSpeed = Long.parseLong(args[2]);
            long reloadTime = Long.parseLong(args[3]);
            double damage = Double.parseDouble(args[4]);
            long shootingDebounceValue = Long.parseLong(args[5]);
            long reloadingDebounceValue = Long.parseLong(args[6]);

            Gun gun = new Gun(name, magazineSize, currentMagazineLoad, shootingSpeed, 
                            reloadTime, damage, shootingDebounceValue, reloadingDebounceValue);
            
            Gun.toItem(item, gun);
            player.setItemInHand(item);
            player.sendMessage("Gun created: " + name + " with " + magazineSize + " ammo!");

        } catch (NumberFormatException e) {
            player.sendMessage("Invalid number format!");
        }

        return true;
    }
}
