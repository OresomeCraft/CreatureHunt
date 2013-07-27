package com.oresomecraft.creaturehunt;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class CreatureHuntAsyncTask extends BukkitRunnable {

    private String mainWorld;
    
    public int state;
    
    private long signupStart = CreatureHunt.instance.getConfig().getLong("SignUpTimeStart");
    private long signupEnd = CreatureHunt.instance.getConfig().getLong("SignUpTimeEnd");
    private long huntStart = CreatureHunt.instance.getConfig().getLong("HuntTimeStart");
    private long huntEnd = CreatureHunt.instance.getConfig().getLong("HuntTimeEnd");
    private long midTime = (signupEnd - signupStart) / 2 + signupStart;
    
    private double entryFee = CreatureHunt.instance.getConfig().getDouble("EntryFee");
    
    private boolean midTimeMessage;
    private boolean lastMinuteMessage;
    
    public CreatureHuntAsyncTask(String main) {
        mainWorld = main;
        
        state = 0;
    }
    
    @Override
    public void run() {
        synchronized (CreatureHunt.lock){ 
            //System.out.println("State: " + state + ", MidTimeMessage: " + midTimeMessage);
            long currentWorldTime = -1;
            if (Bukkit.getWorld(mainWorld) != null) {
                currentWorldTime = Bukkit.getWorld(mainWorld).getTime();
            } else {
                CreatureHunt.instance.getLogger().severe("Cannot find world '" + mainWorld + "'!");
                return;
            }
            // state 0 >> yet to begin
            if (state == 0) {
                if (currentWorldTime >= signupStart && currentWorldTime <= signupEnd) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendMessage(ChatColor.DARK_RED + "Prepare your swords and ready your armour...");
                        p.sendMessage(ChatColor.DARK_RED + "Type " + ChatColor.RED + "/hunt join" + ChatColor.DARK_RED + " and pay $" + ChatColor.RED +
                                String.format("%.2f", (float) CreatureHunt.instance.getConfig().getDouble("EntryFee")) + ChatColor.DARK_RED + " to join the Mob Hunt!");
                    }
                    midTimeMessage = false;
                    lastMinuteMessage = false;
                    state = 1;
                }
            // state 1 >> ready to accept players
            } else if (state == 1) {
                if (currentWorldTime >= signupEnd) {
                    state = 2;
                } else if (currentWorldTime >= midTime && !midTimeMessage) {
                    midTimeMessage = true;
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        p.sendMessage(ChatColor.DARK_RED + "Only " + ChatColor.RED + ((signupEnd - currentWorldTime) / 20) + ChatColor.DARK_RED
                                + " seconds remain to signup for the Mob Hunt!");
                    }
                } else if (currentWorldTime < signupStart) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (CreatureHunt.enteredPlayers.containsKey(p.getName())) {
                            p.sendMessage(ChatColor.DARK_RED + "The Mob Hunt has been cancelled due to a change in time.");
                            p.sendMessage(ChatColor.RED + "Your entry money has been refunded.");
                            CreatureHunt.econ.depositPlayer(p.getName(), entryFee);
                        } else {
                            p.sendMessage(ChatColor.DARK_RED + "The Mob Hunt has been cancelled due to a change in time.");
                        }
                    }
                    CreatureHunt.lead = 0;
                    CreatureHunt.enteredPlayers.clear();
                    state = 0;
                }
                
                if (currentWorldTime >= huntStart) {
                    if (CreatureHunt.enteredPlayers.size() >= CreatureHunt.instance.getConfig().getInt("MinPlayers")) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (CreatureHunt.enteredPlayers.containsKey(p.getName())) {
                                p.sendMessage(ChatColor.DARK_GREEN + "The Mob Hunt as begun! Go and kill those evil creatures!");
                            } else {
                                p.sendMessage(ChatColor.DARK_RED + "You can no longer sign-up for the Mob Hunt...");
                            }
                        }
                        state = 3;
                    } else {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (CreatureHunt.enteredPlayers.containsKey(p.getName())) {
                                p.sendMessage(ChatColor.RED + "Not enough people signed up for the Mob Hunt in time...");
                                p.sendMessage(ChatColor.RED + "You have been refunded your entry fee.");
                                CreatureHunt.econ.depositPlayer(p.getName(), CreatureHunt.instance.getConfig().getDouble("EntryFee"));
                            } else {
                                p.sendMessage(ChatColor.DARK_RED + "You can no longer sign-up for the Mob Hunt...");
                            }
                        }
                        CreatureHunt.lead = 0;
                        CreatureHunt.enteredPlayers.clear();
                        state = 0;
                    }
                }
            // state 2 >> ready to begin!
            } else if (state == 2) {
                if (currentWorldTime >= huntStart) {
                    if (CreatureHunt.enteredPlayers.size() >= 3) {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (CreatureHunt.enteredPlayers.containsKey(p.getName())) {
                                p.sendMessage(ChatColor.DARK_GREEN + "The Mob Hunt as begun! Go and kill those evil creatures!");
                            } else {
                                p.sendMessage(ChatColor.DARK_RED + "You can no longer sign-up for the Mob Hunt...");
                            }
                        }
                        state = 3;
                    } else {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            if (CreatureHunt.enteredPlayers.containsKey(p.getName())) {
                                p.sendMessage(ChatColor.RED + "Not enough people signed up for the Mob Hunt in time...");
                                p.sendMessage(ChatColor.RED + "You have been refunded your entry fee.");
                                CreatureHunt.econ.depositPlayer(p.getName(), CreatureHunt.instance.getConfig().getDouble("EntryFee"));
                            } else {
                                p.sendMessage(ChatColor.DARK_RED + "You can no longer sign-up for the Mob Hunt...");
                            }
                        }
                        CreatureHunt.enteredPlayers.clear();
                        CreatureHunt.lead = 0;
                        state = 0;
                    }
                } else if ((currentWorldTime > huntEnd && huntEnd > huntStart)
                        || (currentWorldTime > huntEnd && currentWorldTime < signupStart && huntEnd < huntStart)) {
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (CreatureHunt.enteredPlayers.containsKey(p.getName())) {
                            p.sendMessage(ChatColor.DARK_RED + "The Mob Hunt has been cancelled due to a change in time.");
                        }
                    }
                    CreatureHunt.enteredPlayers.clear();
                    CreatureHunt.lead = 0;
                    state = 0;
                }
            // state 3 >> currently playing
            } else if (state == 3) {
                if (((currentWorldTime >= huntEnd || currentWorldTime < huntStart) && huntEnd > huntStart)
                        || (currentWorldTime > huntEnd && currentWorldTime < huntStart && huntEnd < huntStart)) {
                    state = 4;
                } else if (huntEnd > huntStart && currentWorldTime >= huntEnd - 1200 && !lastMinuteMessage) {
                    lastMinuteMessage = true;
                    for (Player p : Bukkit.getOnlinePlayers()) {
                        if (CreatureHunt.enteredPlayers.containsKey(p.getName())) {
                            p.sendMessage(ChatColor.RED + "There is less than one minute remaining!");
                        }
                    }
                }
            // state 4 >> finished and giving out rewards
            } else if (state == 4) {
                String firstName = null;
                short firstScore = 0;
                String secondName = null;
                short secondScore = 0;
                String thirdName = null;
                short thirdScore = 0;
                
                int size = CreatureHunt.enteredPlayers.size();
                for (String entree : CreatureHunt.enteredPlayers.keySet()) {
                    if (CreatureHunt.enteredPlayers.get(entree).getScore() > firstScore) {
                        if (firstName == null) {
                            firstScore = CreatureHunt.enteredPlayers.get(entree).getScore();
                            firstName = entree;
                        } else if (secondName != null) {
                            thirdName = secondName;
                            thirdScore = secondScore;
                            secondName = firstName;
                            secondScore = firstScore;
                            firstScore = CreatureHunt.enteredPlayers.get(entree).getScore();
                            firstName = entree;
                        } else {
                            secondName = firstName;
                            secondScore = firstScore;
                            firstScore = CreatureHunt.enteredPlayers.get(entree).getScore();
                            firstName = entree;
                        }
                    } else if (CreatureHunt.enteredPlayers.get(entree).getScore() > secondScore) {
                        if (secondName != null) {
                            thirdName = secondName;
                            thirdScore = secondScore;
                            secondScore = CreatureHunt.enteredPlayers.get(entree).getScore();
                            secondName = entree;
                        } else {
                            secondScore = CreatureHunt.enteredPlayers.get(entree).getScore();
                            secondName = entree;
                        }
                    } else if (CreatureHunt.enteredPlayers.get(entree).getScore() > thirdScore) {
                        thirdScore = CreatureHunt.enteredPlayers.get(entree).getScore();
                        thirdName = entree;
                    }
                }
                
                float firstMoney = (float) (CreatureHunt.instance.getConfig().getDouble("FirstBase") + CreatureHunt.instance.getConfig().getDouble("FirstModifier") * size);
                float secondMoney = (float) (CreatureHunt.instance.getConfig().getDouble("SecondBase") + CreatureHunt.instance.getConfig().getDouble("SecondModifier") * size);
                float thirdMoney = (float) (CreatureHunt.instance.getConfig().getDouble("ThirdBase") + CreatureHunt.instance.getConfig().getDouble("ThirdModifier") * size);
                
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (CreatureHunt.enteredPlayers.containsKey(p.getName())) {
                        p.sendMessage(ChatColor.RED + "The Mob Hunt is now over! The results are:");
                        if (firstName != null) {
                            p.sendMessage(ChatColor.GOLD + "1st: " + ChatColor.GREEN + firstName + ChatColor.DARK_GREEN + " with a score of: " + ChatColor.GREEN + firstScore + ChatColor.DARK_GREEN + "!");
                        } else {
                            p.sendMessage(ChatColor.GOLD + "1st: " + ChatColor.GREEN + "no one!");
                        }
                        if (secondName != null) {
                            p.sendMessage(ChatColor.GRAY + "2nd: " + ChatColor.GREEN + secondName + ChatColor.DARK_GREEN + " with a score of: " + ChatColor.GREEN + secondScore + ChatColor.DARK_GREEN + "!");
                        } else {
                            p.sendMessage(ChatColor.GRAY + "2nd: " + ChatColor.GREEN + "no one!");
                        }
                        if (thirdName != null) {
                            p.sendMessage(ChatColor.RED + "3rd: " + ChatColor.GREEN + thirdName + ChatColor.DARK_GREEN + " with a score of: " + ChatColor.GREEN + thirdScore + ChatColor.DARK_GREEN + "!");
                        } else {
                            p.sendMessage(ChatColor.RED + "3rd: " + ChatColor.GREEN + "no one!");
                        }
                        if (firstName != null && firstName.equalsIgnoreCase(p.getName())) {
                            p.sendMessage(String.format(ChatColor.GOLD + "Congratulations! You won! You have won: $" + ChatColor.WHITE + "%.2f" + ChatColor.GOLD + "!", firstMoney));
                            CreatureHunt.econ.depositPlayer(firstName, firstMoney);
                        } else if (secondName != null && secondName.equalsIgnoreCase(p.getName())) {
                            p.sendMessage(String.format(ChatColor.GREEN + "Congratulations! You came second! You have won: $" + ChatColor.WHITE + "%.2f" + ChatColor.GREEN + "!", secondMoney));
                            CreatureHunt.econ.depositPlayer(secondName, secondMoney);
                        } else if (thirdName != null && thirdName.equalsIgnoreCase(p.getName())) {
                            p.sendMessage(String.format(ChatColor.GREEN + "Congratulations! You came third! You have won: $" + ChatColor.WHITE + "%.2f" + ChatColor.GREEN + "!", thirdMoney));
                            CreatureHunt.econ.depositPlayer(thirdName, thirdMoney);
                        } else {
                            p.sendMessage(ChatColor.RED + "Unfortunately you didn't place...");
                        }
                        float pot = (float) CreatureHunt.enteredPlayers.get(p.getName()).getPot();
                        if (pot > 0) {
                            p.sendMessage(String.format(ChatColor.DARK_AQUA + "Your money pot of $" + ChatColor.AQUA + "%.2f" + ChatColor.DARK_AQUA + " has been deposited into your", pot));
                            p.sendMessage(ChatColor.DARK_AQUA + "account. Well done!");
                            CreatureHunt.econ.depositPlayer(p.getName(), pot);
                        }
                    }
                }
                CreatureHunt.lead = 0;
                CreatureHunt.enteredPlayers.clear();
                state = 0;
            }
        }
    }

}
