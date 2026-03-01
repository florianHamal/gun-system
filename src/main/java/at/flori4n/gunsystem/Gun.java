package at.flori4n.gunsystem;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Gun {
    private String name;
    private int magazineSize;
    private int currentMagazineLoad;
    private long shootingSpeed;
    private long reloadTime;
    private double damage;
    private long shootingDebounce;
    private long reloadingDebounce;

    public Gun(String name, int magazineSize, int currentMagazineLoad, long shootingSpeed, 
               long reloadTime, double damage, long shootingDebounce, long reloadingDebounce) {
        this.name = name;
        this.magazineSize = magazineSize;
        this.currentMagazineLoad = currentMagazineLoad;
        this.shootingSpeed = shootingSpeed;
        this.reloadTime = reloadTime;
        this.damage = damage;
        this.shootingDebounce = shootingDebounce;
        this.reloadingDebounce = reloadingDebounce;
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
        if (lore.size() < 8) {
            return false;
        }
        return lore.get(0).startsWith("§c§lGUN_SYSTEM");
    }

    public static Gun fromItem(ItemStack item) {
        if (!isGun(item)) {
            return null;
        }
        ItemMeta meta = item.getItemMeta();
        List<String> lore = meta.getLore();
        
        String name = lore.get(1).replace("§7Name: §f", "");
        int magazineSize = Integer.parseInt(lore.get(2).replace("§7Magazine Size: §f", ""));
        int currentMagazineLoad = Integer.parseInt(lore.get(3).replace("§7Current Ammo: §f", ""));
        long shootingSpeed = Long.parseLong(lore.get(4).replace("§7Shooting Speed: §f", ""));
        long reloadTime = Long.parseLong(lore.get(5).replace("§7Reload Time: §f", ""));
        double damage = Double.parseDouble(lore.get(6).replace("§7Damage: §f", ""));
        long shootingDebounce = Long.parseLong(lore.get(7).replace("§7Shooting Debounce: §f", ""));
        long reloadingDebounce = Long.parseLong(lore.get(8).replace("§7Reloading Debounce: §f", ""));

        return new Gun(name, magazineSize, currentMagazineLoad, shootingSpeed, reloadTime, 
                      damage, shootingDebounce, reloadingDebounce);
    }

    public static ItemStack toItem(ItemStack item, Gun gun) {
        ItemMeta meta = item.getItemMeta();
        List<String> lore = new ArrayList<>();
        lore.add("§c§lGUN_SYSTEM");
        lore.add("§7Name: §f" + gun.getName());
        lore.add("§7Magazine Size: §f" + gun.getMagazineSize());
        lore.add("§7Current Ammo: §f" + gun.getCurrentMagazineLoad());
        lore.add("§7Shooting Speed: §f" + gun.getShootingSpeed());
        lore.add("§7Reload Time: §f" + gun.getReloadTime());
        lore.add("§7Damage: §f" + gun.getDamage());
        lore.add("§7Shooting Debounce: §f" + gun.getShootingDebounce());
        lore.add("§7Reloading Debounce: §f" + gun.getReloadingDebounce());
        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    public void updateItem(ItemStack item) {
        toItem(item, this);
    }

    public String getName() { return name; }
    public int getMagazineSize() { return magazineSize; }
    public int getCurrentMagazineLoad() { return currentMagazineLoad; }
    public long getShootingSpeed() { return shootingSpeed; }
    public long getReloadTime() { return reloadTime; }
    public double getDamage() { return damage; }
    public long getShootingDebounce() { return shootingDebounce; }
    public long getReloadingDebounce() { return reloadingDebounce; }

    public void setCurrentMagazineLoad(int load) { this.currentMagazineLoad = load; }
    public void setShootingDebounce(long debounce) { this.shootingDebounce = debounce; }
    public void setReloadingDebounce(long debounce) { this.reloadingDebounce = debounce; }
}
