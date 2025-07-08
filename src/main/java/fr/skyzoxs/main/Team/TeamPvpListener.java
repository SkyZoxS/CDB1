package fr.skyzoxs.main.Team;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.entity.Player;
import java.util.UUID;

public class TeamPvpListener implements Listener {

    private final TeamManager teamManager;
    private boolean enabled = true;


    public TeamPvpListener(TeamManager teamManager) {
        this.teamManager = teamManager;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isEnabled() {
        return enabled;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!enabled) return;
        if (!(event.getEntity() instanceof Player victim)) return;

        Player attacker = null;

        // Cas 1 : Attaque directe par un joueur
        if (event.getDamager() instanceof Player damagerPlayer) {
            attacker = damagerPlayer;
        }

        // Cas 2 : Projectile tiré par un joueur
        else if (event.getDamager() instanceof org.bukkit.entity.Projectile projectile &&
                projectile.getShooter() instanceof Player shooter) {
            attacker = shooter;
        }

        // Pas un joueur = pas concerné
        if (attacker == null) return;

        // Si le joueur se frappe lui-même (directement ou indirectement), on autorise
        if (attacker.getUniqueId().equals(victim.getUniqueId())) return;

        // Vérifie les équipes
        UUID attackerUUID = attacker.getUniqueId();
        UUID victimUUID = victim.getUniqueId();

        String attackerTeam = teamManager.getTeamOfPlayer(attackerUUID);
        String victimTeam = teamManager.getTeamOfPlayer(victimUUID);

        // Si les deux sont dans la même équipe et ce ne sont PAS la même personne
        if (attackerTeam != null && attackerTeam.equals(victimTeam)) {
            event.setCancelled(true);
            attacker.sendMessage("§cVous ne pouvez pas infliger de dégâts à un membre de votre équipe !");
        }
    }



}
