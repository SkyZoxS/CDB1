package fr.skyzoxs.main;

import fr.skyzoxs.main.Food.FoodListener;
import fr.skyzoxs.main.Grade.*;
import fr.skyzoxs.main.Points.*;
import fr.skyzoxs.main.Product.*;
import fr.skyzoxs.main.SpinVillager.*;
import fr.skyzoxs.main.SpinVillager.reward.RewardManager;
import fr.skyzoxs.main.Team.TeamCommands;
import fr.skyzoxs.main.Team.TeamManager;
import fr.skyzoxs.main.Team.TeamPvpListener;
import fr.skyzoxs.main.Trader.*;
import fr.skyzoxs.main.utils.Join;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.event.*;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class Main extends JavaPlugin implements Listener {

    private static Main instance;
    private static final HashMap<String, PointsTraderVillager> traders = new HashMap<>();
    private static GlobalContri globalContri;
    private static final double[][] VILLAGER_LOCATIONS = {
            {2828.5, 110.0, 2718.5}, // basic
            {2834.5, 110.0, 2718.5}, // decorator
            {2826.5, 110.0, 2736.5}, // garden
            {2821.5, 110.0, 2772.5}, // nether
            {2810.5, 110.0, 2772.5}, // end
            {2833.5, 110.0, 2737.5}, // hunter
            {2841.5, 110.0, 2728.5}, // food
            {2819.5, 110.0, 2724.5}, // resources
            {2820.5, 110.0, 2731.5}, // wood
            {1432.5, 98.0, 1759.5}  // head
    };

    private PointsDataManager pointsDataManager;
    private PointsManager pointsManager;
    private SpinData spinData;

    @Override
    public void onLoad() {
        instance = this;
        globalContri = new GlobalContri();

        traders.put("basic_blocks", new BasicBlocksVillager("basic_blocks", "§2Recycleur"));
        traders.put("decorator", new DecoratorVillager("decorator", "§bDécorateur"));
        traders.put("garden", new GardenVillager("garden", "§aJardinier"));
        traders.put("neither", new NetherVillager("neither", "§4Spécialiste du Nether"));
        traders.put("end", new EndVillager("end", "§eSpécialiste de l'End"));
        traders.put("hunter", new HostileLootVillager("hunter", "§cChasseur"));
        traders.put("food", new FoodVillager("food", "§6Restaurateur"));
        traders.put("resources", new ResourcesVillager("resources", "§8Mineur"));
        traders.put("wood", new WoodVillager("wood", "§6Bucheron"));
        traders.put("head", new HeadVillager("head", "§dMarché noir"));
    }

    @Override
    public void onEnable() {
        instance = this;

        getLogger().info("Plugin is starting...");
        World world = Bukkit.getWorld("world");

        if (world == null) {
            getLogger().severe("World cannot be null.");
            return;
        }

        // Listeners
        getServer().getPluginManager().registerEvents(new FoodListener(this), this);

        // Trader locations
        Map<String, Location> traderLocations = Map.ofEntries(
                Map.entry("basic_blocks", new Location(world, VILLAGER_LOCATIONS[0][0], VILLAGER_LOCATIONS[0][1], VILLAGER_LOCATIONS[0][2], 0, 0)), //
                Map.entry("decorator", new Location(world, VILLAGER_LOCATIONS[1][0], VILLAGER_LOCATIONS[1][1], VILLAGER_LOCATIONS[1][2], 0, 0)), //
                Map.entry("garden", new Location(world, VILLAGER_LOCATIONS[2][0], VILLAGER_LOCATIONS[2][1], VILLAGER_LOCATIONS[2][2], 180, 0)), //
                Map.entry("neither", new Location(world, VILLAGER_LOCATIONS[3][0], VILLAGER_LOCATIONS[3][1], VILLAGER_LOCATIONS[3][2], 90, 0)), //
                Map.entry("end", new Location(world, VILLAGER_LOCATIONS[4][0], VILLAGER_LOCATIONS[4][1], VILLAGER_LOCATIONS[4][2], 270, 0)), //
                Map.entry("hunter", new Location(world, VILLAGER_LOCATIONS[5][0], VILLAGER_LOCATIONS[5][1], VILLAGER_LOCATIONS[5][2], 180, 0)), //
                Map.entry("food", new Location(world, VILLAGER_LOCATIONS[6][0], VILLAGER_LOCATIONS[6][1], VILLAGER_LOCATIONS[6][2], 90, 0)),
                Map.entry("resources", new Location(world, VILLAGER_LOCATIONS[7][0], VILLAGER_LOCATIONS[7][1], VILLAGER_LOCATIONS[7][2], 270, 0)),
                Map.entry("wood", new Location(world, VILLAGER_LOCATIONS[8][0], VILLAGER_LOCATIONS[8][1], VILLAGER_LOCATIONS[8][2], 270, 0)),
                Map.entry("head", new Location(world, VILLAGER_LOCATIONS[9][0], VILLAGER_LOCATIONS[9][1], VILLAGER_LOCATIONS[9][2], 0, 0))

        );

        removeAllEntityPointsTrader(world);

        try {
            for (Map.Entry<String, Location> entry : traderLocations.entrySet()) {
                Location loc = entry.getValue();
                world.getBlockAt(loc.clone().subtract(0, 1, 0)).setType(Material.QUARTZ_BLOCK);
                world.setChunkForceLoaded(loc.getBlockX() / 16, loc.getBlockZ() / 16, true);
                traders.get(entry.getKey()).create(world, loc, Villager.Profession.NITWIT);
            }
        } catch (Exception e) {
            getLogger().warning("Erreur d'initialisation des PNJs: " + e.getMessage());
        }

        // Initialisation inflation
        pointsDataManager = new PointsDataManager(this);
        pointsDataManager.loadAllTraders(traders);

        // Tâche d'inflation
        new RunnableDecreaseInflation(traders)
                .runTaskTimer(this, 20L * 10800, 20L * 10800);

        // Gestion des points
        pointsManager = new PointsManager(this);
        globalContri = pointsManager.loadPoints();

        // Listener joueur
        getServer().getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent event) {
                Player player = event.getPlayer();
                pointsManager.createIfAbsent(player.getUniqueId());
                ShowGrade.setPlayerPointsGrade(player, globalContri);
            }
        }, this);

        // Spin Villager
        try {
            SpinVillager.removeSpin(world);
            new SpinVillager(this).SpawnSpin();
            spinData = new SpinData(this);
            RewardManager rewardManager = new RewardManager(getDataFolder(), this);
            Spin spin = new Spin(spinData, rewardManager, this);
            getServer().getPluginManager().registerEvents(new SpinListener(spin), this);
            getLogger().info("Spin villager has been enabled!");
        } catch (Exception e) {
            getLogger().severe("Spin villager could not be enabled!");
        }

        // Commandes
        getCommand("resetspin").setExecutor(new ResetSpin(spinData));
        TeamManager teamManager = new TeamManager(this);
        TeamPvpListener pvpListener = new TeamPvpListener(teamManager);
        new TeamCommands(this, teamManager, pvpListener);

        // Events généraux
        Bukkit.getPluginManager().registerEvents(new Shop(traders), this);
        Bukkit.getPluginManager().registerEvents(new Sell(globalContri, traders), this);
        Bukkit.getPluginManager().registerEvents(new Join(globalContri, teamManager), this);
        Bukkit.getPluginManager().registerEvents(new GradeChat(globalContri, teamManager), this);
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(pvpListener, this);


        // Mise à jour Scoreboard / Grade
        Bukkit.getScheduler().runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                PointsScoreboard.setScoreBoard(player, globalContri, teamManager);
                ShowGrade.setPlayerPointsGrade(player, globalContri);
            }
        }, 0L, 20L);
    }

    @Override
    public void onDisable() {
        World world = Bukkit.getWorld("world");
        if (world != null) {
            SpinVillager.removeSpin(world);
            removeAllEntityPointsTrader(world);
        }

        pointsManager.savePoints(globalContri);
        pointsDataManager.saveAllTraders(traders);
        getLogger().info("Plugin is stopping...");
    }

    private void removeAllEntityPointsTrader(World world) {
        NamespacedKey key = new NamespacedKey(this, "points_trader");
        for (Entity entity : world.getEntities()) {
            if (entity instanceof Villager villager &&
                    villager.getPersistentDataContainer().has(key, PersistentDataType.INTEGER)) {
                villager.setInvulnerable(false);
                villager.remove();
            } else if (!(entity instanceof Player)) {
                Location loc = entity.getLocation();
                for (double[] coords : VILLAGER_LOCATIONS) {
                    if (loc.getX() == coords[0] && loc.getY() == coords[1] && loc.getZ() == coords[2]) {
                        entity.remove();
                        break;
                    }
                }
            }
        }
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();

        Player killer = player.getKiller();
        if(killer == null) return;

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD, 1);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player); // Attribue la tête au joueur mort
            meta.setDisplayName(player.getName() + "'s Head");
            List<String> lore = new ArrayList<>();
            lore.add("Une tête appartenant à " + player.getName());
            meta.setLore(lore);
            skull.setItemMeta(meta);

        }
        e.getDrops().add(skull);
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_DEATH, 10.0f, 1.0f);
    }

    public static Main getInstance() {
        return instance;
    }

    public static GlobalContri getGlobalContri() {
        return globalContri;
    }

    public static HashMap<String, PointsTraderVillager> getTraders() {
        return traders;
    }
}
