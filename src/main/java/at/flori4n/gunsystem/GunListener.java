package at.flori4n.gunsystem;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Player;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

import java.util.UUID;

public class GunListener implements Listener {

    @EventHandler
    public void onItemSwitch(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        int previousSlot = event.getPreviousSlot();
        int currentSlot = event.getNewSlot();
        
        ItemStack previousItem = player.getInventory().getItem(previousSlot);
        ItemStack currentItem = player.getInventory().getItem(currentSlot);
        
        Gun previousGun = GunSystem.getInstance().getPlayerGun(playerId);
        
        if (previousGun != null && previousItem != null) {
            Gun.toItem(previousItem, previousGun);
            player.getInventory().setItem(previousSlot, previousItem);
            GunSystem.getInstance().removePlayerGun(playerId);
        }
        
        if (Gun.isGun(currentItem)) {
            Gun gun = Gun.fromItem(currentItem);
            GunSystem.getInstance().setPlayerGun(playerId, gun);
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        Gun gun = GunSystem.getInstance().getPlayerGun(playerId);
        
        if (gun == null) {
            return;
        }
        
        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            shoot(player, gun);
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            reload(player, gun);
        }
    }

    private void shoot(Player player, Gun gun) {
        if (gun.getShootingDebounce() > 0) {
            player.sendMessage("§cTo fast");
            return;
        }
        if (gun.getReloadingDebounce() > 0) {
            player.sendMessage("§cStill reloading");
            return;
        }
        
        if (gun.getCurrentMagazineLoad() <= 0) {
            player.sendMessage("§cOut of ammo! Right click to reload.");
            return;
        }
        
        gun.setCurrentMagazineLoad(gun.getCurrentMagazineLoad() - 1);
        
        Arrow arrow = player.launchProjectile(Arrow.class);
        arrow.setVelocity(player.getLocation().getDirection().multiply(2.0));
        arrow.setMetadata("gunDamage", new org.bukkit.metadata.FixedMetadataValue(GunSystem.getInstance(), gun.getDamage()));
        
        gun.setShootingDebounce(gun.getShootingSpeed());
        
        player.sendMessage("§aBang! Ammo: " + gun.getCurrentMagazineLoad() + "/" + gun.getMagazineSize());
    }

    private void reload(Player player, Gun gun) {
        if (gun.getShootingDebounce() > 0) {
            player.sendMessage("§cTo fast" + gun.getShootingDebounce());
            return;
        }
        if (gun.getReloadingDebounce() > 0) {
            player.sendMessage("§cStill reloading");
            return;
        }        
        if (gun.getCurrentMagazineLoad() == gun.getMagazineSize()) {
            player.sendMessage("§aMagazine already full!");
            return;
        }
        
        gun.setReloadingDebounce(gun.getReloadTime());
        
        player.sendMessage("§eReloading...");
        
        GunSystem.getInstance().getServer().getScheduler().runTaskLater(GunSystem.getInstance(), () -> {
            gun.setCurrentMagazineLoad(gun.getMagazineSize());
            gun.setReloadingDebounce(0);
            player.sendMessage("§aReloaded! Magazine full.");
        }, gun.getReloadTime() / 50);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile projectile = event.getEntity();
        if (projectile instanceof Arrow) {
            Arrow arrow = (Arrow) projectile;
            if (arrow.hasMetadata("gunDamage")) {
                double damage = arrow.getMetadata("gunDamage").get(0).asDouble();
                Entity hitEntity = event.getEntity();
                if (hitEntity instanceof org.bukkit.entity.LivingEntity) {
                    org.bukkit.entity.LivingEntity livingEntity = (org.bukkit.entity.LivingEntity) hitEntity;
                    livingEntity.damage(damage);
                }
            }
        }
    }
}
