package fr.skyzoxs.main.SpinVillager;

import fr.skyzoxs.main.SpinVillager.reward.RewardItem;
import fr.skyzoxs.main.SpinVillager.reward.RewardManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.List;
import java.util.Random;

public class SpinGUI {
    private final Plugin plugin;
    private final RewardManager rewardManager;
    private final Random random = new Random();

    public SpinGUI(Plugin plugin, RewardManager rewardManager) {
        this.plugin = plugin;
        this.rewardManager = rewardManager;
    }

    // Open GUI
    public void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, ChatColor.DARK_PURPLE + "Roulette");

        // vitres sauf centre
        for (int i = 0; i < 9; i++) {
            if (i != 4) {
                inv.setItem(i, new ItemStack(Material.GRAY_STAINED_GLASS_PANE));
            }
        }

        player.openInventory(inv);

        List<RewardItem> rewards = rewardManager.getRewards();

        if (rewards.isEmpty()) {
            player.sendMessage("Pas de récompenses configurées !");
            return;
        }

        long delayBetweenTicks = 2 + random.nextInt(4);

        new BukkitRunnable() {
            int ticks = 0;
            // Durée aléatoire entre 40 et 80 ticks (~2 à 4 secondes si runTaskTimer à 1 tick)
            final int maxTicks = 40 + random.nextInt(40);
            final List<RewardItem> rewards = rewardManager.getRewards();
            int index = new Random().nextInt(rewards.size());

            @Override
            public void run() {
                if (!player.isOnline() || !player.getOpenInventory().getTopInventory().equals(inv)) {
                    cancel();
                    return;
                }

                RewardItem current = rewards.get(index % rewards.size());
                ItemStack currentItemStack = current.toItemStack();
                inv.setItem(4, currentItemStack);
                player.updateInventory();

                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1, 1);

                ticks++;
                index++;

                if (ticks >= maxTicks) {
                    cancel();

                    // Tirage  aléatoire
                    RewardItem reward = rewardManager.getRandomReward();

                    if (reward != null) {
                        ItemStack rewardItemStack = reward.toItemStack();
                        inv.setItem(4, rewardItemStack);
                        player.updateInventory();

                        player.getInventory().addItem(rewardItemStack);
                        player.sendMessage("§aTu as gagné §6" + rewardItemStack.getItemMeta().getDisplayName());
                        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, 1);
                    } else {
                        System.out.println("[SpinGUI] Attention : aucun item gagné (reward null)");
                    }
                }
            }
        }.runTaskTimer(plugin, delayBetweenTicks, 3L);
    }
}
