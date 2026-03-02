package at.flori4n.gunsystem;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class Gun {
    private String name;
    private int magazineSize;
    private int currentMagazineLoad;
    private long shootingSpeed;
    private long reloadTime;
    private double damage;
    private boolean useRaycast;
    private String particleType;
    private boolean isReloading;
    private boolean isShooting;
    private boolean isLegacy;

    public Gun(String name, int magazineSize, int currentMagazineLoad, long shootingSpeed, 
               long reloadTime, double damage, boolean useRaycast, String particleType) {
        this.name = name;
        this.magazineSize = magazineSize;
        this.currentMagazineLoad = currentMagazineLoad;
        this.shootingSpeed = shootingSpeed;
        this.reloadTime = reloadTime;
        this.damage = damage;
        this.useRaycast = useRaycast;
        this.particleType = particleType;
        this.isLegacy = false;
    }

    public static boolean isGun(ItemStack item) {
        if (item == null || item.getType() == Material.AIR || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        if (!meta.hasLore()) {
            return false;
        }
        List<String> lore = meta.getLore();
        if (lore.isEmpty()) {
            return false;
        }
        String lastLine = lore.get(lore.size() - 1);
        if (lastLine.startsWith("§kGUN_SYSTEM")) {
            return true;
        }
        for (String line : lore) {
            if (line.contains("at.flori4n_.GunSystemV2")) {
                return true;
            }
        }
        return false;
    }

    public static Gun fromItem(ItemStack item) {
        if (!isGun(item)) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        
        String lastLine = lore.get(lore.size() - 1);
        
        if (!lastLine.startsWith("§kGUN_SYSTEM")) {
            return parseLegacy(item, meta, lore);
        }
        
        String name = meta.getDisplayName().replace("§f", "");
        int magazineSize = Integer.parseInt(lore.get(0).replace("§7Magazine Size: §f", ""));
        long shootingSpeed = Long.parseLong(lore.get(1).replace("§7Shooting Speed: §f", ""));
        long reloadTime = Long.parseLong(lore.get(2).replace("§7Reload Time: §f", ""));
        double damage = Double.parseDouble(lore.get(3).replace("§7Damage: §f", ""));
        
        boolean useRaycast = Boolean.parseBoolean(lore.get(4).replace("§kRaycast: ", ""));
        String particleType = lore.get(5).replace("§kType: ", "");
        int currentMagazineLoad = Integer.parseInt(lore.get(lore.size() - 2).replace("§kAmmo: ", ""));

        Gun gun = new Gun(name, magazineSize, currentMagazineLoad, shootingSpeed, reloadTime, 
                      damage, useRaycast, particleType);
        gun.isLegacy = false;
        return gun;
    }

    private static Gun parseLegacy(ItemStack item, ItemMeta meta, List<String> lore) {
        String name = meta.getDisplayName();
        
        String[] ammoParts = lore.get(0).split("/");
        int currentAmmo = Integer.parseInt(ammoParts[0].trim());
        int magazineSize = Integer.parseInt(ammoParts[1].trim());
        
        double damage = 1.0;
        long reloadTime = 20;
        long shootingSpeed = 5;
        String particleType = "FLAME";
        
        for (String line : lore) {
            if (line.startsWith("Damage:")) {
                damage = Double.parseDouble(line.replace("Damage:", "").trim());
            } else if (line.startsWith("Reload_Time:")) {
                reloadTime = Long.parseLong(line.replace("Reload_Time:", "").trim());
            } else if (line.startsWith("Speed:")) {
                shootingSpeed = Long.parseLong(line.replace("Speed:", "").trim());
            } else if (line.startsWith("Effect:")) {
                particleType = line.replace("Effect:", "").trim().toUpperCase();
            }
        }
        
        Gun gun = new Gun(name, magazineSize, currentAmmo, shootingSpeed, reloadTime, 
                      damage, true, particleType);
        gun.isLegacy = true;
        return gun;
    }

    public static ItemStack toItem(ItemStack item, Gun gun) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§f" + gun.getName());
        List<String> lore = new ArrayList<>();
        lore.add("§7Magazine Size: §f" + gun.getMagazineSize());
        lore.add("§7Shooting Speed: §f" + gun.getShootingSpeed());
        lore.add("§7Reload Time: §f" + gun.getReloadTime());
        lore.add("§7Damage: §f" + gun.getDamage());
        lore.add("§kRaycast: " + gun.isUseRaycast());
        lore.add("§kType: " + gun.getParticleType());
        lore.add("§kAmmo: " + gun.getCurrentMagazineLoad());
        lore.add("§kGUN_SYSTEM");
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public String getName() { return name; }
    public int getMagazineSize() { return magazineSize; }
    public int getCurrentMagazineLoad() { return currentMagazineLoad; }
    public long getShootingSpeed() { return shootingSpeed; }
    public long getReloadTime() { return reloadTime; }
    public double getDamage() { return damage; }
    public boolean isUseRaycast() { return useRaycast; }
    public String getParticleType() { return particleType; }
    public boolean isReloading() { return isReloading; }
    public boolean isShooting() { return isShooting; }
    public boolean isLegacy() { return isLegacy; }

    public void setCurrentMagazineLoad(int load) { this.currentMagazineLoad = load; }
    public void setReloading(boolean reloading) { this.isReloading = reloading; }
    public void setShooting(boolean shooting) { this.isShooting = shooting; }
    
    public String getActionBarText() {
        if (isReloading) {
            return "§c§lReloading...";
        }
        if (isShooting) {
             return "§c" + currentMagazineLoad + "/" + magazineSize;
        }
        return "§a" + currentMagazineLoad + "§7/§f" + magazineSize;
    }
    
    public void sendActionBar(Player player) {
        GunSystem.sendActionBar(player, getActionBarText());
    }
}
