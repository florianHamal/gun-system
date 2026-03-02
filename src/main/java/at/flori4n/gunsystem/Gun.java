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
        if (lore.size() < 6) {
            return false;
        }
        return lore.get(lore.size() - 1).startsWith("§kGUN_SYSTEM");
    }

    public static Gun fromItem(ItemStack item) {
        if (!isGun(item)) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        
        String name = meta.getDisplayName().replace("§f", "");
        int magazineSize = Integer.parseInt(lore.get(0).replace("§7Magazine Size: §f", ""));
        long shootingSpeed = Long.parseLong(lore.get(1).replace("§7Shooting Speed: §f", ""));
        long reloadTime = Long.parseLong(lore.get(2).replace("§7Reload Time: §f", ""));
        double damage = Double.parseDouble(lore.get(3).replace("§7Damage: §f", ""));
        
        boolean useRaycast = Boolean.parseBoolean(lore.get(4).replace("§7UseRaycast: §f", ""));
        String particleType = lore.get(5).replace("§7ParticleType: §f", "");
        int currentMagazineLoad = Integer.parseInt(lore.get(lore.size() - 2).replace("§7Current Ammo: §f", ""));

        return new Gun(name, magazineSize, currentMagazineLoad, shootingSpeed, reloadTime, 
                      damage, useRaycast, particleType);
    }

    public static ItemStack toItem(ItemStack item, Gun gun) {
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName("§f" + gun.getName());
        List<String> lore = new ArrayList<>();
        lore.add("§7Magazine Size: §f" + gun.getMagazineSize());
        lore.add("§7Shooting Speed: §f" + gun.getShootingSpeed());
        lore.add("§7Reload Time: §f" + gun.getReloadTime());
        lore.add("§7Damage: §f" + gun.getDamage());
        lore.add("§7UseRaycast: §f" + gun.isUseRaycast());
        lore.add("§7ParticleType: §f" + gun.getParticleType());
        lore.add("§7Current Ammo: §f" + gun.getCurrentMagazineLoad());
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

    public void setCurrentMagazineLoad(int load) { this.currentMagazineLoad = load; }
    public void setReloading(boolean reloading) { this.isReloading = reloading; }
    public void setShooting(boolean shooting) { this.isShooting = shooting; }
    
    public String getActionBarText() {
        if (isReloading) {
            return "§c§lReloading...";
        }
        if (isShooting) {
            return "§eShooting...";
        }
        return "§a" + currentMagazineLoad + "§7/§f" + magazineSize;
    }
    
    public void sendActionBar(Player player) {
        GunSystem.sendActionBar(player, getActionBarText());
    }
}
