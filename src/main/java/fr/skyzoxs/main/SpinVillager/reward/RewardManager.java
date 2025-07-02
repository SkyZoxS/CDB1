package fr.skyzoxs.main.SpinVillager.reward;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.File;
import java.util.*;

//Load reward
public class RewardManager {
    private final List<RewardItem> rewardItems = new ArrayList<>();

    //Create every rewards from rewardItem.yml
    public RewardManager(File dataFolder, Plugin plugin) {
        File file = new File(dataFolder, "rewardItem.yml");

        //copy rewardItem.yml if not already existing in the plugin folder
        copyResourceIfNotExists(plugin, "rewardItem.yml", file);

        if (!file.exists()) {
            System.out.println("[Roulette] Le fichier rewardItem.yml n'existe pas dans " + file.getAbsolutePath() + "\nCréation en cours...");
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        List<?> sections = config.getMapList("rewards");
        if (sections.isEmpty()) {
            System.out.println("[Roulette] Aucune récompense trouvée dans rewardItem.yml");
        } else {
            System.out.println("[Roulette] Chargement de " + sections.size() + " récompenses.");
        }
        for (var section : config.getMapList("rewards")) {
            try {
                Map<String, Object> rewardData = (Map<String, Object>) section;

                RewardItem reward = new RewardItem(rewardData);
                rewardItems.add(reward);

                System.out.println("[Roulette] Item chargé : " + rewardData.get("id") + ", " + rewardData.get("name"));
            } catch (Exception e) {
                System.out.println("[Roulette] Erreur de chargement d’un item : " + e.getMessage());
                e.printStackTrace();
            }
        }
    }

    //Copy default rewarditem.yml if doesn't exist in the folder
    private void copyResourceIfNotExists(Plugin plugin, String resourcePath, File destination) {
        if (!destination.exists()) {
            try {
                destination.getParentFile().mkdirs();
                plugin.saveResource(resourcePath, false);
                System.out.println("[Roulette] Fichier " + resourcePath + " copié vers " + destination.getAbsolutePath());
            } catch (IllegalArgumentException e) {
                System.out.println("[Roulette] Ressource introuvable dans le JAR: " + resourcePath);
            }
        }
    }


    public RewardItem getRandomReward() {
        List<RewardItem> weightedList = new ArrayList<>();
        for (RewardItem reward : rewardItems) {
            for (int i = 0; i < reward.getChance(); i++) {
                weightedList.add(reward);
            }
        }

        if (weightedList.isEmpty()) return null;

        // Mélange la liste pondérée avant de choisir
        Collections.shuffle(weightedList);
        for(RewardItem reward : weightedList) {
        }
        // Choisit un item aléatoire dans la liste mélangée
        return weightedList.get(new Random().nextInt(weightedList.size()));
    }




    //Getter for rewards
    public List<RewardItem> getRewards() {
        return rewardItems;
    }

}
