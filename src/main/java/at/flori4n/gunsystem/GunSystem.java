package at.flori4n.gunsystem;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
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

    public static void sendActionBar(Player player, String message) {
        try {
            Object craftPlayer = player.getClass().getMethod("getHandle").invoke(player);
            Object playerConnection = craftPlayer.getClass().getField("playerConnection").get(craftPlayer);
            
            String nmsVersion = org.bukkit.Bukkit.getServer().getClass().getPackage().getName().split("\\.")[3];
            
            Class<?> packetClass = Class.forName("net.minecraft.server." + nmsVersion + ".PacketPlayOutChat");
            Class<?> iChatBaseComponentClass = Class.forName("net.minecraft.server." + nmsVersion + ".IChatBaseComponent");
            
            Object component = iChatBaseComponentClass.getDeclaredClasses()[0].getMethod("a", String.class).invoke(null, "{\"text\":\"" + message + "\"}");
            
            Object packet = packetClass.getConstructor(iChatBaseComponentClass, byte.class).newInstance(component, (byte) 2);
            
            playerConnection.getClass().getMethod("sendPacket", Class.forName("net.minecraft.server." + nmsVersion + ".Packet")).invoke(playerConnection, packet);
        } catch (Exception e) {
            player.sendMessage(message);
        }
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
                    
                    long remainingShooting = gun.getShootingDebounce() - 1;
                    long remainingReloading = gun.getReloadingDebounce() - 1;
                    
                    if (remainingShooting < 0) remainingShooting = 0;
                    if (remainingReloading < 0) remainingReloading = 0;
               
                    gun.setShootingDebounce(remainingShooting);
                    gun.setReloadingDebounce(remainingReloading);
                    
                    Player player = getServer().getPlayer(playerId);
                    if (player != null) {
                        gun.sendActionBar(player);
                    }
                }
            }
        }.runTaskTimer(this, 1L, 10L);
    }

    @Override
    public void onDisable() {
        getLogger().info("GunSystem disabled!");
    }
}
