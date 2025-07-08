package fr.skyzoxs.main.utils;

import fr.skyzoxs.main.Points.PointsScoreboard;
import fr.skyzoxs.main.Team.TeamManager;
import fr.skyzoxs.main.Team.TeamPvpListener;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class Commands implements CommandExecutor {

    private final TeamManager teamManager;
    private final TeamPvpListener teamPvpListener;
    private JavaPlugin plugin;

    public Commands(JavaPlugin plugin, TeamManager teamManager, TeamPvpListener teamPvpListener) {
        this.teamManager = teamManager;
        this.teamPvpListener = teamPvpListener;
        this.plugin = plugin;
        plugin.getCommand("teamcreate").setExecutor(this);
        plugin.getCommand("teamdelete").setExecutor(this);
        plugin.getCommand("teamlist").setExecutor(this);
        plugin.getCommand("teamreveal").setExecutor(this);
        plugin.getCommand("teampvp").setExecutor(this);
        plugin.getCommand("givecd").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) return false;

        if (command.getName().equalsIgnoreCase("teamcreate")) {
            return handleCreateTeam(sender, args);
        }

        if (command.getName().equalsIgnoreCase("teamdelete")) {
            return handleDeleteTeam(sender, args);
        }

        if (command.getName().equalsIgnoreCase("teamlist")) {
            return handleTeamList(sender);
        }

        if (command.getName().equalsIgnoreCase("teamreveal")) {
            return handleTeamReveal(sender, args);
        }

        if (command.getName().equalsIgnoreCase("teampvp")) {
            return handleTeamPvp(sender, args);
        }

        if(command.getName().equalsIgnoreCase("givecd")) {
            return handleGiveCd(sender, args);

        }

        return false;
    }

    private boolean handleGiveCd(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Seul un joueur peut exécuter cette commande.");
            return true;
        }

        ItemStack disc = new ItemStack(Material.MUSIC_DISC_13);
        ItemMeta meta = disc.getItemMeta();
        meta.setDisplayName("Disque du Son");
        disc.setItemMeta(meta);

        player.getInventory().addItem(disc);
        player.sendMessage(ChatColor.GREEN + "Disque du Son donné !");
        return true;
    }

    private boolean handleTeamReveal(CommandSender sender, String[] args) {


        if(args.length == 1 && (Objects.equals(args[0], "false") || Objects.equals(args[0], "true"))){
            PointsScoreboard.reveal(args[0]);
            sender.sendMessage(ChatColor.GREEN + "Revealed : " + args[0]);
            return true;
        }else {
            sender.sendMessage(ChatColor.RED + "Usage: /teamreveal true/false");
            return true;
        }
    }

    private boolean handleTeamPvp(CommandSender sender, String[] args) {
        if (args.length != 1 || !(args[0].equalsIgnoreCase("true") || args[0].equalsIgnoreCase("false"))) {
            sender.sendMessage(ChatColor.RED + "Usage: /teampvp true/false");
            return true;
        }

        boolean enabled = Boolean.parseBoolean(args[0]);
        teamPvpListener.setEnabled(enabled);

        sender.sendMessage(ChatColor.GREEN + "Protection PVP entre équipes : " + (enabled ? "activée ✅" : "désactivée ❌"));
        return true;
    }



    private boolean handleTeamList(CommandSender sender) {
        Map<String, List<UUID>> teams = teamManager.getAllTeams();

        if (teams.isEmpty()) {
            sender.sendMessage(ChatColor.YELLOW + "Aucune équipe n'a été créée.");
            return true;
        }

        sender.sendMessage(ChatColor.GOLD + "Liste des équipes :");
        for (String teamName : teams.keySet()) {
            String colorName = teamManager.getTeamColor(teamName);
            ChatColor color;

            try {
                color = ChatColor.valueOf(colorName.toUpperCase());
            } catch (IllegalArgumentException e) {
                color = ChatColor.WHITE;
            }

            int size = teams.get(teamName).size();
            sender.sendMessage(color + "- " + teamName + ChatColor.GRAY + " (" + size + " joueur" + (size > 1 ? "s" : "") + ")");
        }

        return true;
    }

    private boolean handleCreateTeam(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.RED + "Usage: /teamcreate <NomEquipe> <Couleur> <Joueur1> <Joueur2> ...");
            return true;
        }

        String teamName = args[0];
        String color = args[1].toUpperCase();

        ChatColor chatColor;
        try {
            chatColor = ChatColor.valueOf(color);
        } catch (IllegalArgumentException e) {
            sender.sendMessage(ChatColor.RED + "Couleur invalide. Ex: RED, BLUE, GREEN, etc.");
            return true;
        }

        List<UUID> members = new ArrayList<>();
        for (int i = 2; i < args.length; i++) {
            Player target = Bukkit.getPlayerExact(args[i]);
            if (target != null) {
                members.add(target.getUniqueId());
            } else {
                sender.sendMessage(ChatColor.YELLOW + args[i] + " est hors ligne ou introuvable.");
            }
        }

        if (members.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Aucun joueur valide spécifié !");
            return true;
        }

        teamManager.createTeam(teamName, color, members);
        sender.sendMessage(chatColor + "Équipe '" + teamName + "' créée avec succès avec " + members.size() + " joueurs !");
        return true;
    }

    private boolean handleDeleteTeam(CommandSender sender, String[] args) {
        if (args.length != 1) {
            sender.sendMessage(ChatColor.RED + "Usage: /teamdelete <NomEquipe>");
            return true;
        }

        String teamName = args[0];
        if (teamManager.deleteTeam(teamName)) {
            sender.sendMessage(ChatColor.GREEN + "Équipe '" + teamName + "' supprimée avec succès !");
        } else {
            sender.sendMessage(ChatColor.RED + "Équipe '" + teamName + "' introuvable !");
        }

        return true;
    }
}
