package fr.skyzoxs.main.SpinVillager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Villager;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.Plugin;

import java.util.List;

//Create a custom Villager with custom metadata, name and location
public class SpinVillager {

    private final Plugin plugin;

    // Locations of each villager
    static double[] villagerLocations = {
            2859.5, 110.0, 2751.5  //Spin
    };



    public SpinVillager(Plugin plugin){
        this.plugin = plugin;
    }


    //Remove villager
    public static void removeSpin(World world) {
            List<Entity> entities = world.getEntities();

                world.setChunkForceLoaded((int) (villagerLocations[0]/16), (int) (villagerLocations[2]/16), true);

            for (Entity entity : entities) {
                if (entity.getType().equals(EntityType.VILLAGER)) {

                    // On vérifie toutes les métadatas
                    List<String> knownMetaKeys = List.of("spin-npc"); // List of Villager

                    for (String key : knownMetaKeys) {
                        if (entity.hasMetadata(key)) {
                            do {
                                entity.setInvulnerable(false);
                                entity.remove();
                            }while(!entity.isDead());
                        }
                    }
                }
            }

    }

    //Create SpinVillager
    public  void SpawnSpin() {
        Location loc = new Location(Bukkit.getWorld("world"), villagerLocations[0], villagerLocations[1], villagerLocations[2]);
        World world = loc.getWorld();

        if(world == null){
            throw new NullPointerException("world is cannot be null.");
        }



        //Make the villager with custom name, invulnerability and silent.
        world.getBlockAt(loc.clone().subtract(0, 1, 0)).setType(Material.QUARTZ_BLOCK);

        Villager villager = world.spawn(loc, Villager.class);

        villager.setCustomName("§dMaurice la roue"); //Custom name
        villager.setCustomNameVisible(true); //Set name visible
        villager.setInvulnerable(true); // Set invulnerable
        villager.setAI(false); // Set AI false
        villager.setSilent(true); // Set silent
        villager.setProfession(Villager.Profession.NITWIT);

        //Add a tag
        villager.setMetadata("spin-npc", new FixedMetadataValue(plugin, true));

    }
}
