package com.oresomecraft.creaturehunt.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

import com.oresomecraft.creaturehunt.CreatureHunt;
import com.oresomecraft.creaturehunt.data.BadAreas;
import com.oresomecraft.creaturehunt.data.BadMobMeta;

public class CreatureSpawnListener implements Listener {

    public static ArrayList<BadAreas> badAreas;
    
    public CreatureSpawnListener() {
        badAreas = new ArrayList<BadAreas>();
        
        List<String> conBadAreas = CreatureHunt.instance.getConfig().getStringList("BadAreas");
        if (conBadAreas != null) {
            for (String area : conBadAreas) {
                String[] pos = area.split(",");
                if (pos.length == 6) {
                    boolean allIntegers = true;
                    for (String digit : pos) {
                        if (!isInteger(digit)) {
                            allIntegers = false;
                        }
                    }
                    if (allIntegers) {
                        int x1 = Integer.parseInt(pos[0]);
                        int y1 = Integer.parseInt(pos[1]);
                        int z1 = Integer.parseInt(pos[2]);
                        int x2 = Integer.parseInt(pos[3]);
                        int y2 = Integer.parseInt(pos[4]);
                        int z2 = Integer.parseInt(pos[5]);
                        int[] positions = {x1, y1, z1, x2, y2, z2};
                        badAreas.add(new BadAreas(positions));
                    } else {
                        CreatureHunt.instance.getLogger().severe("Invalid Bad Area listed in Creature Hunt config!");
                    }
                }
            }
        }
        
    }
    
    public boolean isInteger(String n) {
        try {
            Integer.parseInt(n);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
    
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (!event.getEntity().hasMetadata("BadMobSpawn")) {
            boolean spawnedInArea = false;
            for (BadAreas area : badAreas) {
                if (area.isInArea(event.getLocation().getBlockX(), event.getLocation().getBlockY(), event.getLocation().getBlockZ())) {
                    spawnedInArea = true;
                    break;
                }
            }
            if (spawnedInArea) {
                event.getEntity().setMetadata("BadMobSpawn", new BadMobMeta(true));
            } else {
                if (validSpawnReason(event.getSpawnReason())) {
                    event.getEntity().setMetadata("BadMobSpawn", new BadMobMeta(false));
                } else {
                    event.getEntity().setMetadata("BadMobSpawn", new BadMobMeta(true));
                }
            }
        }
    }

    private boolean validSpawnReason(SpawnReason spawnReason) {
        switch (spawnReason) {
        case BREEDING: return false;
        case BUILD_IRONGOLEM: return false;
        case BUILD_SNOWMAN: return false;
        case CUSTOM: return false;
        case DEFAULT: return false;
        case EGG: return false;
        case JOCKEY: return false;
        case LIGHTNING: return true;
        case NATURAL: return true;
        case SLIME_SPLIT: return true;
        case SPAWNER: return false;
        case SPAWNER_EGG: return false;
        case VILLAGE_DEFENSE: return true;
        case VILLAGE_INVASION: return true;
        }
        return false;
    }
}
