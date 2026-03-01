package at.flori4n.gunsystem;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GunSystem extends JavaPlugin {
    
    private static GunSystem instance;
    private Map<UUID, Gun> playerGuns = new HashMap<>();

    public static GunSystem getInstance() {
        return instance;
    }

    public Map<UUID, Gun> getPlayerGuns() {
        return playerGuns;
    }

    public void setPlayerGun(UUID playerId, Gun gun) {
        playerGuns.put(playerId, gun);
    }

    public Gun getPlayerGun(UUID playerId) {
        return playerGuns.get(playerId);
    }

    public void removePlayerGun(UUID playerId) {
        playerGuns.remove(playerId);
    }

    @Override
    public void onEnable() {
        instance = this;
        getLogger().info("GunSystem enabled!");
        
        getCommand("creategun").setExecutor(new GunCommand());
        getCommand("gunhelp").setExecutor(new GunCommand());
        
        getServer().getPluginManager().registerEvents(new GunListener(), this);
        
        new BukkitRunnable() {
            @Override
            public void run() {
                
                for (Map.Entry<UUID, Gun> entry : playerGuns.entrySet()) {
                    UUID playerId = entry.getKey();
                    Gun gun = entry.getValue();
                    
                    long remainingShooting = gun.getShootingDebounce() - 1;//currentTime;
                    long remainingReloading = gun.getReloadingDebounce() - 1;//currentTime;
                    
                    if (remainingShooting < 0) remainingShooting = 0;
                    if (remainingReloading < 0) remainingReloading = 0;
               
                    gun.setShootingDebounce(remainingShooting);
                    gun.setReloadingDebounce(remainingReloading);
                }
            }
        }.runTaskTimer(this, 1L, 1L);
    }

    @Override
    public void onDisable() {
        getLogger().info("GunSystem disabled!");
    }
}
