package at.flori4n.gunsystem;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Player;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class GunListener implements Listener {

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        ItemStack item = player.getItemInHand();
        if (Gun.isGun(item) && GunSystem.getInstance().getPlayerGun(playerId) == null) {
            Gun gun = Gun.fromItem(item);
            GunSystem.getInstance().setPlayerGun(playerId, gun);
        }
    }

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
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        
        Gun gun = GunSystem.getInstance().getPlayerGun(playerId);
        
        if (gun != null && Gun.isGun(droppedItem)) {
            Gun.toItem(droppedItem, gun);
            event.getItemDrop().setItemStack(droppedItem);
            GunSystem.getInstance().removePlayerGun(playerId);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        UUID playerId = player.getUniqueId();
        
        Gun gun = GunSystem.getInstance().getPlayerGun(playerId);
        
        if (gun != null) {
            ItemStack item = player.getItemInHand();
            if (item != null) {
                Gun.toItem(item, gun);
                player.setItemInHand(item);
                GunSystem.getInstance().removePlayerGun(playerId);
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getPlayer();
        UUID playerId = player.getUniqueId();
        
        Gun gun = GunSystem.getInstance().getPlayerGun(playerId);
        
        if (gun == null) {
            ItemStack item = player.getItemInHand();
            if (Gun.isGun(item)) {
                Gun newGun = Gun.fromItem(item);
                GunSystem.getInstance().setPlayerGun(playerId, newGun);
            }
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
        
        if (gun.isUseRaycast()) {
            org.bukkit.Location start = player.getEyeLocation();
            org.bukkit.util.Vector direction = start.getDirection().normalize();
            double maxDistance = 300.0;
            org.bukkit.entity.LivingEntity closestHit = null;
            double closestDistance = maxDistance;
            
            for (double d = 0; d < maxDistance; d += 0.5) {
                org.bukkit.Location particleLoc = start.clone().add(direction.clone().multiply(d));
                player.getWorld().playEffect(particleLoc, getParticle(gun.getParticleType()), 1);
            }
            
            for (org.bukkit.entity.Entity entity : player.getWorld().getEntities()) {
                if (entity == player) continue;
                if (!(entity instanceof org.bukkit.entity.LivingEntity)) continue;
                
                org.bukkit.entity.LivingEntity living = (org.bukkit.entity.LivingEntity) entity;
                org.bukkit.Location entityLoc = living.getEyeLocation();
                
                double dx = entityLoc.getX() - start.getX();
                double dy = entityLoc.getY() - start.getY();
                double dz = entityLoc.getZ() - start.getZ();
                org.bukkit.util.Vector toEntity = new org.bukkit.util.Vector(dx, dy, dz);
                double dot = toEntity.dot(direction);
                
                if (dot <= 0) continue;
                
                org.bukkit.Location closestPoint = start.clone().add(direction.clone().multiply(dot));
                double distance = closestPoint.distance(entityLoc);
                
                if (distance < 0.5 && dot < closestDistance) {
                    closestDistance = dot;
                    closestHit = living;
                }
            }
            
            if (closestHit != null) {
                closestHit.damage(gun.getDamage(), player);
            }
        } else {
            Arrow arrow = player.launchProjectile(Arrow.class);
            arrow.setVelocity(player.getLocation().getDirection().multiply(2.0));
            arrow.setMetadata("gunDamage", new org.bukkit.metadata.FixedMetadataValue(GunSystem.getInstance(), gun.getDamage()));
        gun.sendActionBar(player);
        }
        
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
        gun.sendActionBar(player);
        GunSystem.getInstance().getServer().getScheduler().runTaskLater(GunSystem.getInstance(), () -> {
            gun.setCurrentMagazineLoad(gun.getMagazineSize());
            gun.setReloadingDebounce(0);
            player.sendMessage("§aReloaded! Magazine full.");
        }, gun.getReloadTime());
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

    private org.bukkit.Effect getParticle(String particleName) {
        String name = particleName.toUpperCase();
        try {
            return org.bukkit.Effect.valueOf(name);
        } catch (IllegalArgumentException e) {
            return org.bukkit.Effect.FLAME;
        }
    }
}
