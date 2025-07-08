package fr.skyzoxs.main.utils;

import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Shrekphone implements Listener {

    private final Set<UUID> activePlayers = new HashSet<>();
    private static final String SOUND_NAME = "custom.shrekphone";
    private static final SoundCategory CATEGORY = SoundCategory.AMBIENT; // changé ici
    private static final double RADIUS = 25.0;

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        activePlayers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerUseDisc(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND ||
                !event.getAction().name().contains("RIGHT_CLICK")) {
            return;
        }

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() != Material.MUSIC_DISC_13) return;

        ItemMeta meta = item.getItemMeta();
        if (meta == null || !ChatColor.stripColor(meta.getDisplayName()).equals("Disque du Son")) return;

        event.setCancelled(true);

        if (player.isSneaking()) {
            stopSoundForAll();
            player.sendMessage(ChatColor.RED + "Son arrêté pour tout le monde");
        } else {
            playSoundForAll(player.getLocation());
            player.sendMessage(ChatColor.GREEN + "♪ Shrekophone lancé ♪");
        }
    }

    private void playSoundForAll(Location location) {
        stopSoundForAll(); // Optionnel : stop tous les sons avant d’en jouer un

        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.getWorld().equals(location.getWorld())) continue;

            double distanceSquared = online.getLocation().distanceSquared(location);
            if (distanceSquared <= RADIUS * RADIUS) {
                online.playSound(location, SOUND_NAME, CATEGORY, 3.0f, 1.0f);
            }
        }
    }


    private void stopSoundForAll() {
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.stopSound(SOUND_NAME, CATEGORY);
        }
    }
}
