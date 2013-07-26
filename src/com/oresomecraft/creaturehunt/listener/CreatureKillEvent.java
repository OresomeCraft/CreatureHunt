package com.oresomecraft.creaturehunt.listener;

import java.util.HashMap;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.oresomecraft.creaturehunt.CreatureHunt;
import com.oresomecraft.creaturehunt.GameStorage;

public class CreatureKillEvent implements Listener {
    
    private HashMap<String, EntityDrops> mobDrops;
    
    private String mainWorld, netherWorld, endWorld;
    
    public CreatureKillEvent() {
        mainWorld = CreatureHunt.instance.getConfig().getString("Overworld");
        netherWorld = CreatureHunt.instance.getConfig().getString("Nether");
        endWorld = CreatureHunt.instance.getConfig().getString("End");
        
        mobDrops = new HashMap<String, EntityDrops>();
        for (String mob : CreatureHunt.instance.getConfig().getStringList("Mobs.MobList")) {
            EntityDrops entityDrops = new EntityDrops();
            entityDrops.maxPoint = (short) CreatureHunt.instance.getConfig().getInt("Mobs." + mob + ".MaxPoint");
            entityDrops.minPoint = (short) CreatureHunt.instance.getConfig().getInt("Mobs." + mob + ".MinPoint");
            entityDrops.maxMoney = CreatureHunt.instance.getConfig().getInt("Mobs." + mob + ".MaxMoney");
            entityDrops.minMoney = CreatureHunt.instance.getConfig().getInt("Mobs." + mob + ".MinMoney");
            if (CreatureHunt.instance.getConfig().getStringList("Mobs." + mob + ".AdditionalDrops") != null) {
                for (String drop : CreatureHunt.instance.getConfig().getStringList("Mobs." + mob + ".AdditionalDrops")) {
                    EntityItemDrop itemDrop = new EntityItemDrop();
                    String[] information = drop.split(",");
                    try {
                        itemDrop.itemID = Integer.parseInt(information[0].split(":")[0]);
                        itemDrop.dataValue = Short.parseShort(information[0].split(":")[1]);
                        itemDrop.amount = Integer.parseInt(information[1]);
                        itemDrop.dropChance = Short.parseShort(information[2]);
                    } catch (Exception e) {
                        CreatureHunt.instance.getLogger().severe("Could not parse information '" + drop + "' for CreatureHunt. Check your config!");
                        itemDrop = null;
                    }
                    if (itemDrop != null) {
                        entityDrops.itemDrops.add(itemDrop);
                    }
                }
            }
            mobDrops.put(mob, entityDrops);
        }
    }
    
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        String worldName = event.getEntity().getWorld().getName();
        if (!worldName.equalsIgnoreCase(endWorld) && (worldName.equalsIgnoreCase(mainWorld) || worldName.equalsIgnoreCase(netherWorld))) {
            boolean spawnedInArea = false;
            for (BadAreas area : CreatureSpawnListener.badAreas) {
                if (area.isInArea(event.getEntity().getLocation().getBlockX(), event.getEntity().getLocation().getBlockY(), event.getEntity().getLocation().getBlockZ())) {
                    spawnedInArea = true;
                    break;
                }
            }
            if (!spawnedInArea) {
                if (event.getEntity().getKiller() instanceof Player) {
                    Player damager = (Player) event.getEntity().getKiller();
                    synchronized (CreatureHunt.lock) {
                        if (CreatureHunt.asyncTask.state == 3 && CreatureHunt.enteredPlayers.containsKey(damager.getName())) {
                            if ((event.getEntity().hasMetadata("BadMobSpawn") && !event.getEntity().getMetadata("BadMobSpawn").get(0).asBoolean()) ||
                                    !event.getEntity().hasMetadata("BadMobSpawn")) {
                                if (mobDrops.containsKey(event.getEntityType().toString())) {
                                    short points = mobDrops.get(event.getEntityType().toString()).getPoints();
                                    if (points != -1) {
                                        GameStorage playerData = CreatureHunt.enteredPlayers.get(damager.getName());
                                        
                                        damager.sendMessage(ChatColor.DARK_GREEN + "You gain " + ChatColor.GREEN + points + ChatColor.DARK_GREEN + " points!");
                                        playerData.incrementScore(points);
                                        
                                        double money = mobDrops.get(event.getEntityType().toString()).getMoney();
                                        if (money > 0) {
                                            damager.sendMessage(String.format(ChatColor.DARK_AQUA + "$" + ChatColor.AQUA + "%.2f" + ChatColor.DARK_AQUA + " is added to your money pot.", money));
                                            playerData.incrementPot(money);
                                        }
                                        
                                        ItemStack item = mobDrops.get(event.getEntityType().toString()).getItem();
                                        if (item != null) {
                                            damager.sendMessage(ChatColor.DARK_GREEN + "You receive some additional loot from this mob!");
                                            damager.getWorld().dropItem(event.getEntity().getLocation(), item);
                                        }
    
                                        CreatureHunt.enteredPlayers.put(damager.getName(), playerData);
                                        
                                        if (playerData.getScore() > CreatureHunt.leadingScore) {
                                            CreatureHunt.leadingScore = playerData.getScore();
                                            if ((CreatureHunt.leadingPlayer != null && !damager.getName().equalsIgnoreCase(CreatureHunt.leadingPlayer)) || (CreatureHunt.leadingPlayer == null)) {
                                                CreatureHunt.leadingPlayer = damager.getName();
                                                for (Player p : Bukkit.getOnlinePlayers()) {
                                                    if (CreatureHunt.enteredPlayers.containsKey(p.getName())) {
                                                        if (p.getName().equalsIgnoreCase(damager.getName())) {
                                                            p.sendMessage(ChatColor.DARK_GREEN + "You are now in the lead with a score of: " + ChatColor.GREEN + playerData.getScore() + ChatColor.DARK_GREEN + "!");
                                                        } else {
                                                            p.sendMessage(ChatColor.RED + damager.getName() + ChatColor.DARK_RED + " is now in the lead with a score of: " + ChatColor.RED + playerData.getScore() + ChatColor.DARK_RED + "!");
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    } else {
                                        damager.sendMessage(ChatColor.RED + "Killing this type of creature does not earn you points!");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
