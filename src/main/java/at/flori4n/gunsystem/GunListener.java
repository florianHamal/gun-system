package at.flori4n.gunsystem;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.block.Action;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Projectile;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerKickEvent;

import java.util.UUID;

public class GunListener implements Listener {


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        ItemStack item = player.getItemInHand();
        if (Gun.isGun(item)) {
          GunSystem.getInstance().setPlayerGun(player.getUniqueId(),Gun.fromItem(item));
        }
    }
    @EventHandler 
    public void onPlayerQuit(PlayerQuitEvent event){
       GunSystem.getInstance().removePlayerGun(event.getPlayer().getUniqueId());    
    }
    @EventHandler 
    public void onPlayerKick(PlayerKickEvent event){
       GunSystem.getInstance().removePlayerGun(event.getPlayer().getUniqueId());    
    }


    @EventHandler
    public void onItemSwitch(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        int previousSlot = event.getPreviousSlot();
        int currentSlot = event.getNewSlot();
        
        ItemStack previousItem = player.getInventory().getItem(previousSlot);
        ItemStack currentItem = player.getInventory().getItem(currentSlot);
        
        Gun previousGun = GunSystem.getInstance().getPlayerGun(player.getUniqueId());

        if (previousGun != null ){
          if(Gun.isGun(previousItem) && previousGun.isEqual(Gun.fromItem(previousItem))){
            Gun.toItem(previousItem, previousGun);
            player.getInventory().setItem(previousSlot, previousItem);
          }
          GunSystem.getInstance().removePlayerGun(player.getUniqueId());
        }
          
        if (Gun.isGun(currentItem)) {
          Gun newGun = Gun.fromItem(currentItem);
          player.getInventory().setItem(currentSlot, Gun.toItem(currentItem, newGun));
          GunSystem.getInstance().setPlayerGun(player.getUniqueId(),newGun);
        }

    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        ItemStack droppedItem = event.getItemDrop().getItemStack();
        
        Gun loadedGun = GunSystem.getInstance().getPlayerGun(player.getUniqueId());

//ondrop reinit because if canceled = problems


        if (loadedGun != null ){ // if item in hand?
          if(Gun.isGun(droppedItem) && loadedGun.isEqual(Gun.fromItem(droppedItem))){
            Gun.toItem(droppedItem, loadedGun);
            event.getItemDrop().setItemStack(droppedItem);
            GunSystem.getInstance().removePlayerGun(player.getUniqueId());
          }
        }




    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getWhoClicked();
        
        ItemStack clickedItem = event.getCurrentItem();

        Gun loadedGun = GunSystem.getInstance().getPlayerGun(player.getUniqueId());

        if (loadedGun != null ){
          if(Gun.isGun(clickedItem) && loadedGun.isEqual(Gun.fromItem(clickedItem))){
            Gun.toItem(clickedItem, loadedGun);
            event.setCurrentItem(clickedItem);
          }
          GunSystem.getInstance().removePlayerGun(player.getUniqueId());
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        
        if (!(event.getPlayer() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getPlayer();
        ItemStack itemInHand = player.getItemInHand();

        if(Gun.isGun(itemInHand)){
          Gun gunInHand = Gun.fromItem(itemInHand);
          Gun loadedGun = GunSystem.getInstance().getPlayerGun(player.getUniqueId());
          if (loadedGun== null || (!loadedGun.isEqual(gunInHand))) {
            player.setItemInHand(Gun.toItem(itemInHand, gunInHand));
            GunSystem.getInstance().setPlayerGun(player.getUniqueId(), gunInHand);
          }
        }else {
          //just fallback 
          Gun loadedGun = GunSystem.getInstance().getPlayerGun(player.getUniqueId());
          if (loadedGun!=null) GunSystem.getInstance().removePlayerGun(player.getUniqueId()); 
        }
    }

    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event){
      Player player = event.getPlayer();


      ItemStack itemInHand = player.getItemInHand();

      if(Gun.isGun(itemInHand)) {
         Gun gunInHand = Gun.fromItem(itemInHand);
         Gun loadedGun = GunSystem.getInstance().getPlayerGun(player.getUniqueId());
         if (loadedGun== null || (!loadedGun.isEqual(gunInHand))) {
           player.setItemInHand(Gun.toItem(itemInHand, gunInHand));
           GunSystem.getInstance().setPlayerGun(player.getUniqueId(), gunInHand);
         }
      }else{
        Gun loadedGun = GunSystem.getInstance().getPlayerGun(player.getUniqueId());
        if (loadedGun!=null) GunSystem.getInstance().removePlayerGun(player.getUniqueId());       }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        Gun gun = GunSystem.getInstance().getPlayerGun(player.getUniqueId());
        
        if (gun == null) {
            return;
        }

        if (event.getAction() == Action.LEFT_CLICK_AIR || event.getAction() == Action.LEFT_CLICK_BLOCK) {
            shoot(player, gun);
        } else if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
          //todo ignore chests 
          reload(player, gun);
        }
    }



    private boolean canUseGun(Gun gun) {
        return !gun.isShooting() && !gun.isReloading();
    }

    private void shoot(Player player, Gun gun) {
        if (!canUseGun(gun)) {
            return;
        }
        
        if (gun.getCurrentMagazineLoad() <= 0) {
            return;
        }
        
        gun.setCurrentMagazineLoad(gun.getCurrentMagazineLoad() - 1);
        
        gun.setShooting(true);
        gun.sendActionBar(player);
        if (gun.isUseRaycast()) {
            org.bukkit.Location start = player.getEyeLocation();
            org.bukkit.util.Vector direction = start.getDirection().normalize();
            double maxDistance = 300.0;
            
            double blockHitDistance = maxDistance;
            
            for (double d = 0; d < maxDistance; d += 0.5) {
                org.bukkit.Location particleLoc = start.clone().add(direction.clone().multiply(d));
                org.bukkit.block.Block block = particleLoc.getBlock();
                if (block.getType() != org.bukkit.Material.AIR && block.getType() != org.bukkit.Material.LONG_GRASS && block.getType() != org.bukkit.Material.RED_ROSE && block.getType() != org.bukkit.Material.YELLOW_FLOWER) {
                    blockHitDistance = d;
                    break;
                }
                if (gun.getParticleType() != null) {
                    player.getWorld().playEffect(particleLoc, getParticle(gun.getParticleType()), 1);
                }
            }
            
            double entityHitDistance = blockHitDistance;
            org.bukkit.entity.LivingEntity closestHit = null;
            
            for (org.bukkit.entity.Entity entity : player.getWorld().getEntities()) {
                if (entity == player) continue;
                if (!(entity instanceof org.bukkit.entity.LivingEntity)) continue;
                
                org.bukkit.entity.LivingEntity living = (org.bukkit.entity.LivingEntity) entity;
                
                try {
                    Object craftEntity = living.getClass().getMethod("getHandle").invoke(living);
                    java.lang.reflect.Method getBoundingBoxMethod = craftEntity.getClass().getMethod("getBoundingBox");
                    Object boundingBox = getBoundingBoxMethod.invoke(craftEntity);
                    
                    java.lang.reflect.Field minX = boundingBox.getClass().getField("a");
                    java.lang.reflect.Field minY = boundingBox.getClass().getField("b");
                    java.lang.reflect.Field minZ = boundingBox.getClass().getField("c");
                    java.lang.reflect.Field maxX = boundingBox.getClass().getField("d");
                    java.lang.reflect.Field maxY = boundingBox.getClass().getField("e");
                    java.lang.reflect.Field maxZ = boundingBox.getClass().getField("f");
                    
                    minX.setAccessible(true);
                    minY.setAccessible(true);
                    minZ.setAccessible(true);
                    maxX.setAccessible(true);
                    maxY.setAccessible(true);
                    maxZ.setAccessible(true);
                    
                    double bbMinX = (double) minX.get(boundingBox);
                    double bbMinY = (double) minY.get(boundingBox);
                    double bbMinZ = (double) minZ.get(boundingBox);
                    double bbMaxX = (double) maxX.get(boundingBox);
                    double bbMaxY = (double) maxY.get(boundingBox);
                    double bbMaxZ = (double) maxZ.get(boundingBox);
                    
                    double[] intersection = rayIntersectsBox(start.getX(), start.getY(), start.getZ(),
                            direction.getX(), direction.getY(), direction.getZ(),
                            bbMinX, bbMinY, bbMinZ, bbMaxX, bbMaxY, bbMaxZ);
                    
                    if (intersection != null && intersection[0] < entityHitDistance) {
                        entityHitDistance = intersection[0];
                        closestHit = living;
                    }
                } catch (Exception e) {
                    continue;
                }
            }
            
            if (closestHit != null) {
                closestHit.damage(gun.getDamage(), player);
            }
        } else {
            Arrow arrow = player.launchProjectile(Arrow.class);
            arrow.setVelocity(player.getLocation().getDirection().multiply(2.0));
            arrow.setMetadata("gunDamage", new org.bukkit.metadata.FixedMetadataValue(GunSystem.getInstance(), gun.getDamage()));
        }
        player.getLocation().getWorld().playSound(player.getLocation(), Sound.PISTON_RETRACT,2f,2f);
        GunSystem.getInstance().getServer().getScheduler().runTaskLater(GunSystem.getInstance(), () -> {
            gun.setShooting(false);
            gun.sendActionBar(player);
        }, gun.getShootingSpeed());
    }

    private void reload(Player player, Gun gun) {
        if (!canUseGun(gun)) {
            return;
        }
        if (gun.getCurrentMagazineLoad() == gun.getMagazineSize()) {
            return;
        }
        
        gun.setReloading(true);
        
        gun.sendActionBar(player);
        GunSystem.getInstance().getServer().getScheduler().runTaskLater(GunSystem.getInstance(), () -> {
            gun.setCurrentMagazineLoad(gun.getMagazineSize());
            gun.setReloading(false);
            gun.sendActionBar(player);
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

    private double[] rayIntersectsBox(double rayX, double rayY, double rayZ,
                                      double dirX, double dirY, double dirZ,
                                      double minX, double minY, double minZ,
                                      double maxX, double maxY, double maxZ) {
        double invDirX = 1.0 / dirX;
        double invDirY = 1.0 / dirY;
        double invDirZ = 1.0 / dirZ;
        
        double t1 = (minX - rayX) * invDirX;
        double t2 = (maxX - rayX) * invDirX;
        double t3 = (minY - rayY) * invDirY;
        double t4 = (maxY - rayY) * invDirY;
        double t5 = (minZ - rayZ) * invDirZ;
        double t6 = (maxZ - rayZ) * invDirZ;
        
        double tmin = Math.max(Math.max(Math.min(t1, t2), Math.min(t3, t4)), Math.min(t5, t6));
        double tmax = Math.min(Math.min(Math.max(t1, t2), Math.max(t3, t4)), Math.max(t5, t6));
        
        if (tmax < 0) return null;
        if (tmin > tmax) return null;
        
        if (tmin < 0) {
            return new double[]{tmax, (maxX + minX) / 2, (maxY + minY) / 2, (maxZ + minZ) / 2};
        }
        
        return new double[]{tmin, 0, 0, 0};
    }

    private org.bukkit.Effect getParticle(String particleName) {
        String name = particleName.toUpperCase();
        try {
            return org.bukkit.Effect.valueOf(name);
        } catch (IllegalArgumentException e) {
            return org.bukkit.Effect.COLOURED_DUST;
        }
    }
}
