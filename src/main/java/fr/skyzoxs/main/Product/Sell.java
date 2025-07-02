package fr.skyzoxs.main.Product;

import fr.skyzoxs.main.Grade.ShowGrade;
import fr.skyzoxs.main.Points.GlobalContri;
import fr.skyzoxs.main.Points.PointsScoreboard;
import fr.skyzoxs.main.Trader.PointsTraderVillager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;


public class Sell implements Listener {

    private final GlobalContri globalContri;
    private final HashMap<String, PointsTraderVillager> traders;

    public Sell(GlobalContri globalContri, HashMap<String, PointsTraderVillager> traders) {
        this.globalContri = globalContri;
        this.traders = traders;
    }

    private String idTrader(Inventory cliked_inventory) {
        String result = "";
        Iterator<Map.Entry<String, PointsTraderVillager>> it = this.traders.entrySet().iterator();
        while (it.hasNext() && result.isEmpty()) {
            Map.Entry<String, PointsTraderVillager> pair = it.next();
            if (pair.getValue().isMyInventory(cliked_inventory)) {
                result = pair.getKey();
            }
        }
        return result;
    }

    private ArrayList<Integer> indicesPresentMaterial(Inventory inventory, Material wanted_material) {
        if (inventory == null) {
            throw new IllegalArgumentException("The inventory cannot be null.");
        }
        if (wanted_material == null) {
            throw new IllegalArgumentException("The wanted items cannot be null.");
        }

        ArrayList<Integer> result = new ArrayList<>();
        ItemStack[] items = inventory.getContents();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null) {
                Material present_material = items[i].getType();
                if (present_material.equals(wanted_material)) {
                    result.add(i);
                }
            }
        }
        return result;
    }


    private boolean haveEnoughItems(Inventory inventory, ArrayList<Integer> indices_items, int desired_amount) {
        if (inventory == null) {
            throw new IllegalArgumentException("The inventory cannot be null.");
        }
        if (desired_amount <= 0) {
            throw new IllegalArgumentException("The desired amount must be strictly positive.");
        }
        if (indices_items.isEmpty()) {
            return false;
        }

        int i = 0;
        while (i < indices_items.size() && desired_amount > 0) {
            ItemStack item = inventory.getItem(indices_items.get(i));
            if (item != null) {
                desired_amount -= item.getAmount();
            }
            i++;
        }

        return desired_amount <= 0;
    }

    private boolean removeMaterialInPlayerInventory(Inventory player_inventory, Material removed_material, int desired_amount) {
        if (player_inventory == null) {
            throw new IllegalArgumentException("The player's inventory cannot be null.");
        }
        if (removed_material == null) {
            throw  new IllegalArgumentException("The removed material cannot be null.");
        }
        if (desired_amount <= 0) {
            throw new IllegalArgumentException("The desired amount must be strictly positive.");
        }

        ArrayList<Integer> items_indices = indicesPresentMaterial(player_inventory, removed_material);
        if (!haveEnoughItems(player_inventory, items_indices, desired_amount)) {
            return false;
        }

        int i = 0;
        while (i < items_indices.size() && desired_amount > 0) {
            ItemStack item = player_inventory.getItem(items_indices.get(i));
            if (item != null && item.getType().equals(removed_material)) {
                int actual_amount = item.getAmount();
                if (actual_amount >= desired_amount) {
                    item.setAmount(actual_amount - desired_amount);
                    desired_amount = 0;
                }
                else {
                    item.setAmount(0);
                    desired_amount -= actual_amount;
                }
            }
            i++;
        }

        return true;
    }


    @EventHandler
    public void sellItem(InventoryClickEvent event) {
        if (event == null) {
            return;
        }

        Inventory inventory = event.getClickedInventory();
        if (inventory == null) {
            return;
        }

        String id_trader = this.idTrader(inventory);
        if (id_trader.isEmpty() || !this.traders.containsKey(id_trader)) {
            if (event.getClick() == ClickType.SHIFT_LEFT) {
                Inventory destination = event.getView().getTopInventory();
                String id_trader_dest = this.idTrader(destination);
                if (!id_trader_dest.isEmpty() && this.traders.containsKey(id_trader_dest)) {
                    event.setCancelled(true);
                }
            }
            return;
        }

        ItemStack wanted_item = event.getCurrentItem();
        if (wanted_item == null) {
            return;
        }
        Material wanted_material = wanted_item.getType();

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        Inventory player_inventory = player.getInventory();
        int slot_position = event.getRawSlot();

        PointsTraderVillager trader = this.traders.get(id_trader);
        int amount_required = trader.getAmountRequired(slot_position);

        boolean is_removed = false;

        if (wanted_material == Material.PLAYER_HEAD) {
            // Supprime les têtes précisément (avec vérification des métadonnées)
            is_removed = removePlayerHeadInInventory(player_inventory, wanted_item, amount_required);
        } else {
            // Supprime les autres matériaux normalement
            ArrayList<Material> accepted_materials = trader.getAcceptedMaterials(slot_position);
            for (Material material : accepted_materials) {
                if (is_removed) break;
                is_removed = this.removeMaterialInPlayerInventory(player_inventory, material, amount_required);
            }
        }

        if (is_removed) {
            player.updateInventory();
            trader.madeASales(slot_position);
            player.openInventory(trader.createInventory());

            int price = trader.getPrice(slot_position);
            this.globalContri.increaseContribution(String.valueOf(player.getUniqueId()), id_trader, price);

            PointsScoreboard.updatePersonalPoints(player, this.globalContri);
            for (Player online : Bukkit.getOnlinePlayers()) {
                PointsScoreboard.updateGlobalPoints(online, this.globalContri);
            }
            ShowGrade.setPlayerPointsGrade(player, this.globalContri);

            System.out.printf("Le joueur %s a vendu %d de %s.\n", player.getDisplayName(), amount_required, wanted_material);
        } else {
            player.sendRawMessage(String.format("Impossible de vendre l'item suivant : %s.", wanted_material));
        }
    }

    // Méthode spécifique pour supprimer les têtes d'un joueur dans son inventaire
    private boolean removePlayerHeadInInventory(Inventory inventory, ItemStack targetHead, int amount) {
        int amountToRemove = amount;
        if (targetHead == null || !targetHead.hasItemMeta()) return false;

        SkullMeta targetMeta = (SkullMeta) targetHead.getItemMeta();
        if (targetMeta == null) return false;

        OfflinePlayer targetOwner = targetMeta.getOwningPlayer();

        for (int i = 0; i < inventory.getSize(); i++) {
            ItemStack item = inventory.getItem(i);
            if (item != null && item.getType() == Material.PLAYER_HEAD && item.hasItemMeta()) {
                SkullMeta meta = (SkullMeta) item.getItemMeta();

                if (meta != null) {
                    OfflinePlayer owner = meta.getOwningPlayer();

                    // Vérifie que la tête correspond bien au joueur attendu
                    if (owner != null && targetOwner != null && owner.getUniqueId().equals(targetOwner.getUniqueId())) {
                        if (item.getAmount() >= amountToRemove) {
                            item.setAmount(item.getAmount() - amountToRemove);
                            if (item.getAmount() == 0) {
                                inventory.clear(i);
                            } else {
                                inventory.setItem(i, item);
                            }
                            return true;
                        } else {
                            amountToRemove -= item.getAmount();
                            inventory.clear(i);
                        }
                    }
                }
            }
        }
        return false;
    }


    @EventHandler
    public void cannotDragItemInPointsTraderInventory(InventoryDragEvent event) {
        Inventory destination = event.getView().getTopInventory();
        String id_trader = this.idTrader(destination);
        if (!id_trader.isEmpty() && this.traders.containsKey(id_trader)) {
            event.setCancelled(true);
        }
    }
}
