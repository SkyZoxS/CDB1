package fr.skyzoxs.main.Trader;

import fr.skyzoxs.main.Product.PlayerHeadProduct;
import fr.skyzoxs.main.Product.Product;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.List;

public class HeadVillager extends PointsTraderVillager{

    private static final int HIGHEST_PRICE = 1000;
    private static final int LOWEST_PRICES = 100;

    public HeadVillager(String id, String name) {
        super(id, name, 27);

        this.materials.put(
                1,
                new PlayerHeadProduct("NoixDeCocoBlanc", HIGHEST_PRICE, PointsTraderVillager.UNIQUE)
        );

        this.materials.put(
                3,
                new PlayerHeadProduct("TheInf3ct3d", HIGHEST_PRICE, PointsTraderVillager.UNIQUE)
        );

        this.materials.put(
                5,
                new PlayerHeadProduct("AllThirteen", HIGHEST_PRICE, PointsTraderVillager.UNIQUE)
        );

        this.materials.put(
                7,
                new PlayerHeadProduct("Redy_Lovy", HIGHEST_PRICE, PointsTraderVillager.UNIQUE)
        );

        this.materials.put(
                9,
                new PlayerHeadProduct("Oclapse", HIGHEST_PRICE, PointsTraderVillager.UNIQUE)
        );

        this.materials.put(
                11,
                new PlayerHeadProduct("1uREM", HIGHEST_PRICE, PointsTraderVillager.UNIQUE)
        );

        this.materials.put(
                13,
                new PlayerHeadProduct("Sapphire_Aurae", HIGHEST_PRICE, PointsTraderVillager.UNIQUE)
        );

        this.materials.put(
                15,
                new PlayerHeadProduct("Philomena_Yuu", HIGHEST_PRICE, PointsTraderVillager.UNIQUE)
        );

        this.materials.put(
                17,
                new PlayerHeadProduct("Ayana_L0vy", LOWEST_PRICES, PointsTraderVillager.UNIQUE)
        );

        this.materials.put(
                21,
                new PlayerHeadProduct("MissNoopy", LOWEST_PRICES, PointsTraderVillager.UNIQUE)
        );

        this.materials.put(
                23,
                new PlayerHeadProduct("Gedeon39", LOWEST_PRICES, PointsTraderVillager.UNIQUE)
        );

    }


    @Override
    public boolean isSameItem(ItemStack item, Product product) {
        if (item == null || product == null) return false;

            if (!product.getAcceptedMaterials().contains(item.getType())) return false;
            if (item.getAmount() != product.getRequestedAmount()) return false;

            ItemMeta meta = item.getItemMeta();
            if (meta == null || !meta.hasDisplayName()) {
                System.out.println("Item type: " + item.getType());
                System.out.println("Meta class: " + meta.getClass().getSimpleName());

                return false;
            }

            String itemName = ChatColor.stripColor(meta.getDisplayName());
            String productName = ChatColor.stripColor(product.getDisplayedName());
            if (!itemName.equalsIgnoreCase(productName)) return false;

            List<String> lore = meta.getLore();
            List<String> expectedLore = product.getLore();

            if (lore == null || expectedLore == null || lore.size() != expectedLore.size()) return false;

            for (int i = 0; i < lore.size(); i++) {
                if (!ChatColor.stripColor(lore.get(i)).equalsIgnoreCase(ChatColor.stripColor(expectedLore.get(i)))) {
                    return false;
                }
            }

            // Cas spécifique : tête de joueur pour ce marchand
            if (item.getType() == Material.PLAYER_HEAD && product instanceof PlayerHeadProduct) {
                System.out.println("Player head detected");
                SkullMeta skullMeta = (SkullMeta) meta;
                OfflinePlayer owner = skullMeta.getOwningPlayer();

                if (owner == null) {
                    System.out.println("Owner is null");
                    return false;
                }
                String expectedPlayer = ((PlayerHeadProduct) product).getPlayerName();
                System.out.println("Owner: " + owner.getName() + " / Expected: " + expectedPlayer);
                System.out.println( owner.getName() != null && owner.getName().equalsIgnoreCase(expectedPlayer));
                return owner.getName() != null && owner.getName().equalsIgnoreCase(expectedPlayer);
            }


            return true;

    }

}
