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
        if(!enabled) return;
        if (!(event.getEntity() instanceof Player victim)) return;
        if (!(event.getDamager() instanceof Player attacker)) return;

        UUID attackerUUID = attacker.getUniqueId();
        UUID victimUUID = victim.getUniqueId();

        String attackerTeam = teamManager.getTeamOfPlayer(attackerUUID);
        String victimTeam = teamManager.getTeamOfPlayer(victimUUID);

        // Si les deux sont dans la meme equipe
        if (attackerTeam != null && attackerTeam.equals(victimTeam)) {
            event.setCancelled(true);
            attacker.sendMessage("§cVous ne pouvez pas frapper un joueur de votre équipe !");
        }
    }

}
