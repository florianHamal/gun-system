package at.flori4n.gunsystem;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Effect;

public class GunCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("gunhelp")) {
            sender.sendMessage("§6=== Available Particle Types (1.8) ===");
            sender.sendMessage("§e CLICK2, CLICK1, BOW_FIRE, DOOR_TOGGLE, EXTINGUISH, RECORD_PLAY, GHAST_SHRIEK, GHAST_SHOOT, BLAZE_SHOOT, ZOMBIE_CHEW_WOODEN_DOOR, ZOMBIE_CHEW_IRON_DOOR, ZOMBIE_DESTROY_DOOR, SMOKE, STEP_SOUND, POTION_BREAK, ENDER_SIGNAL, MOBSPAWNER_FLAMES, FIREWORKS_SPARK, CRIT, MAGIC_CRIT, POTION_SWIRL, POTION_SWIRL_TRANSPARENT, SPELL, INSTANT_SPELL, WITCH_MAGIC, NOTE, PORTAL, FLYING_GLYPH, FLAME, LAVA_POP, FOOTSTEP, SPLASH, PARTICLE_SMOKE, EXPLOSION_HUGE, EXPLOSION_LARGE, EXPLOSION, VOID_FOG, SMALL_SMOKE, CLOUD, COLOURED_DUST, SNOWBALL_BREAK, WATERDRIP, LAVADRIP, SNOW_SHOVEL, SLIME, HEART, VILLAGER_THUNDERCLOUD, HAPPY_VILLAGER, LARGE_SMOKE, ITEM_BREAK, TILE_BREAK, TILE_DUST");
          
        }
        
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

        if (args.length < 7) {
            player.sendMessage("Usage: /creategun <name> <magazineSize> <shootingSpeed> <reloadTime> <damage> <useRaycast> <particleType>");
            player.sendMessage("Use /gunhelp for particle types list");
            return true;
        }

        try {
            String name = args[0];
            int magazineSize = Integer.parseInt(args[1]);
            int currentMagazineLoad = magazineSize;
            long shootingSpeed = Long.parseLong(args[2]);
            long reloadTime = Long.parseLong(args[3]);
            double damage = Double.parseDouble(args[4]);
            boolean useRaycast = Boolean.parseBoolean(args[5]);
            String particleType = args[6];

            Gun gun = new Gun(name, magazineSize, currentMagazineLoad, shootingSpeed, 
                            reloadTime, damage, useRaycast, particleType);
            
            Gun.toItem(item, gun);
            player.setItemInHand(item);
            player.sendMessage("Gun created: " + name + " with " + magazineSize + " ammo!");

        } catch (NumberFormatException e) {
            player.sendMessage("Invalid number format!");
        }

        return true;
    }
}
