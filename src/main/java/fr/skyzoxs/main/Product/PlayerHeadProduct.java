package fr.skyzoxs.main.Product;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.Collections;

public class PlayerHeadProduct extends Product{

    private final String playerName;

    public PlayerHeadProduct(String playerName, int pointsGiven, int requestedAmount, int recentSell, int amountSold) {
        super(
                playerName + "'s Head",
                Material.PLAYER_HEAD,
                pointsGiven,
                requestedAmount,
                recentSell,
                amountSold,
                new ArrayList<>(Collections.singletonList("Une tête appartenant à " + playerName))
        );
        this.playerName = playerName;
    }

    public PlayerHeadProduct(String playerName, int pointsGiven, int requestedAmount) {
        this(playerName, pointsGiven, requestedAmount, 0, 0);
    }

    @Override
    public ItemStack toItemStack() {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, this.getRequestedAmount());
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(this.playerName);
            meta.setOwningPlayer(offlinePlayer);
            meta.setDisplayName(this.getDisplayedName());
            meta.setLore(this.getLore());
            skull.setItemMeta(meta);
        }
        return skull;
    }

    public String getPlayerName() {
        return this.playerName;
    }

}
