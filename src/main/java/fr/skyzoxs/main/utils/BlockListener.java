package fr.skyzoxs.main.utils;

import fr.skyzoxs.main.Main;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener implements Listener {

    private int x = 2710;
    private int y = 87;
    private int z = 2640;

    @EventHandler
    private void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        Location location = block.getLocation();
        Player player = event.getPlayer();
        boolean firstTime = !Main.getInstance().getConfig().getBoolean("déjà-déclenché", false);


        if(block.getType() == Material.CAKE
                && location.getBlockX() == x
                && location.getBlockZ() == z
                && location.getBlockY() == y
                && firstTime) {

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "givecustomitem treasure_compass " + player.getName());
            event.getPlayer().sendMessage("Félicitation ! Tu as trouvé l'item mystère !");
            Main.getInstance().getConfig().set("déjà-déclenché", true);
            Main.getInstance().saveConfig();

        }
    }
}
