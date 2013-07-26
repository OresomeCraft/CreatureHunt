package com.oresomecraft.creaturehunt.listener;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

import com.oresomecraft.creaturehunt.CreatureHunt;

public class EntrantDeathListener implements Listener {

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        synchronized (CreatureHunt.lock){
            if (CreatureHunt.asyncTask.state == 3 && CreatureHunt.enteredPlayers.containsKey(event.getEntity().getName())) {
                CreatureHunt.enteredPlayers.get(event.getEntity().getName()).deathPenalty();
                event.getEntity().sendMessage(ChatColor.RED + "Due to your death, your money pot has been halved!");
            }
        }
    }
}
