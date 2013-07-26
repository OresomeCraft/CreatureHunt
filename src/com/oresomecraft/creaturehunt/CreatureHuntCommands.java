package com.oresomecraft.creaturehunt;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CreatureHuntCommands implements CommandExecutor {

    private long signupStart = CreatureHunt.instance.getConfig().getLong("SignUpTimeStart");
    private long signupEnd = CreatureHunt.instance.getConfig().getLong("SignUpTimeEnd");
    //private long huntStart = CreatureHunt.instance.getConfig().getLong("HuntTimeStart");
    private long huntEnd = CreatureHunt.instance.getConfig().getLong("HuntTimeEnd");
    private double entryFee = CreatureHunt.instance.getConfig().getDouble("EntryFee");
    private String mainWorld = CreatureHunt.instance.getConfig().getString("Overworld");
    
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("hunt")) {
            if (args.length > 0) {
                if (args[0].equalsIgnoreCase("join")) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        synchronized (CreatureHunt.lock) {
                            if (!CreatureHunt.enteredPlayers.containsKey(player.getName())) {
                                if (CreatureHunt.asyncTask.state == 0) {
                                    player.sendMessage(ChatColor.DARK_RED + "You can not yet signup for the Mob Hunt.");
                                    if (Bukkit.getWorld(mainWorld) != null) {
                                        long worldTime = Bukkit.getWorld(mainWorld).getTime();
                                        long time = worldTime < signupStart ? worldTime - signupStart : 24000 - worldTime + signupStart;
                                        player.sendMessage(ChatColor.DARK_RED + "You will be able to signup in: " + getTime(time, ChatColor.DARK_RED, ChatColor.RED) + ChatColor.DARK_RED + ".");
                                    } else {
                                        CreatureHunt.instance.getLogger().severe("Could not find world '" + mainWorld + "'");
                                    }
                                } else if (CreatureHunt.asyncTask.state == 1) {
                                    String entryFeeString = String.format("%.2f", entryFee);
                                    if (CreatureHunt.econ.getBalance(player.getName()) >= entryFee) {
                                        player.sendMessage(ChatColor.DARK_GREEN + "You have signed up for the Mob Hunt and have paid $" + ChatColor.GREEN + entryFeeString + ChatColor.DARK_GREEN + ".");
                                        player.sendMessage(ChatColor.DARK_AQUA + "If you wish to leave the Mob Hunt (and be refunded) use: " + ChatColor.AQUA + "/hunt leave" + ChatColor.DARK_AQUA + ".");
                                        CreatureHunt.econ.withdrawPlayer(player.getName(), entryFee);
                                        CreatureHunt.enteredPlayers.put(player.getName(), new GameStorage());
                                    } else {
                                        player.sendMessage(String.format(ChatColor.DARK_RED + "You need $" + ChatColor.RED + entryFeeString + ChatColor.DARK_RED
                                                + " to join the Mob Hunt. You have: $" + ChatColor.RED + "%.2f" + ChatColor.DARK_RED + ".", CreatureHunt.econ.getBalance(player.getName())));
                                    }
                                } else {
                                    player.sendMessage(ChatColor.DARK_RED + "The Mob Hunt has already started. Try again another night.");
                                }
                            } else {
                                player.sendMessage(ChatColor.DARK_RED + "You've already signed up for the Mob Hunt!");
                            }
                        }
                    } else {
                        sender.sendMessage(ChatColor.DARK_RED + "You must be in-game to use this command.");
                    }
                } else if (args[0].equalsIgnoreCase("leave")) {
                    if (sender instanceof Player) {
                        synchronized (CreatureHunt.lock) {
                            Player player = (Player) sender;
                            if (CreatureHunt.enteredPlayers.containsKey(player.getName())) {
                                if (CreatureHunt.asyncTask.state == 0 || CreatureHunt.asyncTask.state == 1) {
                                    player.sendMessage(ChatColor.DARK_GREEN + "You have left the Mob Hunt.");
                                    player.sendMessage(ChatColor.DARK_GREEN + "$" + ChatColor.GREEN + entryFee + ChatColor.DARK_GREEN + " has been refunded into your account.");
                                    CreatureHunt.econ.depositPlayer(player.getName(), entryFee);
                                    CreatureHunt.enteredPlayers.remove(player.getName());
                                } else {
                                    player.sendMessage(ChatColor.DARK_RED + "You cannot leave the Mob Hunt after it has begun!");
                                }
                            } else {
                                player.sendMessage(ChatColor.DARK_RED + "You aren't currently signed up for the Mob Hunt!");
                            }
                        }
                    } else {
                        sender.sendMessage(ChatColor.DARK_RED + "You must be in-game to use this command.");
                    }
                } else if (args[0].equalsIgnoreCase("status")) {
                    if (sender instanceof Player) {
                        Player player = (Player) sender;
                        synchronized (CreatureHunt.lock) {
                            if (CreatureHunt.enteredPlayers.containsKey(player.getName())) {
                                if (CreatureHunt.asyncTask.state == 0) {
                                    player.sendMessage(ChatColor.DARK_AQUA + "The Mob Hunt is yet to begin.");
                                    if (Bukkit.getWorld(mainWorld) != null) {
                                        long worldTime = Bukkit.getWorld(mainWorld).getTime();
                                        long time = worldTime < signupStart ? worldTime - signupStart : 24000 - worldTime + signupStart;
                                        String timeAddon = time == 0 ? "" : " in";
                                        player.sendMessage(ChatColor.DARK_AQUA + "You will be able to signup" + timeAddon + ": " + getTime(time, ChatColor.DARK_AQUA, ChatColor.AQUA) + ChatColor.DARK_AQUA + ".");
                                    } else {
                                        CreatureHunt.instance.getLogger().severe("Could not find world '" + mainWorld + "'");
                                    }
                                } else if (CreatureHunt.asyncTask.state == 1) {
                                    player.sendMessage(ChatColor.DARK_AQUA + "You are currently signed up for the Mob Hunt!");
                                    if (Bukkit.getWorld(mainWorld) != null) {
                                        long worldTime = Bukkit.getWorld(mainWorld).getTime();
                                        long time = worldTime < signupEnd ? worldTime - signupEnd : 0;
                                        String timeAddon = time == 0 ? "" : " in";
                                        player.sendMessage(ChatColor.DARK_AQUA + "Signups will end" + timeAddon + ": " + getTime(time, ChatColor.DARK_AQUA, ChatColor.AQUA) + ChatColor.DARK_AQUA + ".");
                                    } else {
                                        CreatureHunt.instance.getLogger().severe("Could not find world '" + mainWorld + "'");
                                    }
                                } else if (CreatureHunt.asyncTask.state == 2) {
                                    player.sendMessage(ChatColor.DARK_AQUA + "The Mob Hunt will begin soon...");
                                } else if (CreatureHunt.asyncTask.state == 3) {
                                    player.sendMessage(ChatColor.GRAY + "========= " + ChatColor.DARK_GRAY + "Creature Hunt Status" + ChatColor.GRAY + " =========");
                                    player.sendMessage(ChatColor.RED + "The Mob Hunt is currently underway...");
                                    if (Bukkit.getWorld(mainWorld) != null) {
                                        long worldTime = Bukkit.getWorld(mainWorld).getTime();
                                        long time = worldTime < huntEnd ? worldTime - huntEnd : 24000 - worldTime + huntEnd;
                                        player.sendMessage(ChatColor.DARK_GRAY + "Time Remaining: " + getTime(time, ChatColor.DARK_GREEN, ChatColor.GREEN) + ChatColor.DARK_GRAY + ".");
                                    } else {
                                        CreatureHunt.instance.getLogger().severe("Could not find world '" + mainWorld + "'");
                                    }
                                    player.sendMessage(ChatColor.DARK_GRAY + "Your current score: " + ChatColor.WHITE + CreatureHunt.enteredPlayers.get(player.getName()).getScore());
                                    player.sendMessage(String.format(ChatColor.DARK_GRAY + "Your current money pot: $" + ChatColor.WHITE + "%.2f", CreatureHunt.enteredPlayers.get(player.getName()).getPot()));
                                    player.sendMessage(ChatColor.DARK_GRAY + "Your deaths: " + ChatColor.RED + CreatureHunt.enteredPlayers.get(player.getName()).getDeaths());
                                    player.sendMessage("");
                                    if (CreatureHunt.leadingPlayer != null && CreatureHunt.leadingPlayer.equalsIgnoreCase(player.getName())) {
                                        player.sendMessage(ChatColor.DARK_GRAY + "Current Leader: " + ChatColor.GREEN + "you!");
                                        player.sendMessage(ChatColor.DARK_GRAY + "With a score of: " + ChatColor.DARK_GREEN + CreatureHunt.leadingScore);
                                    } else if (CreatureHunt.leadingPlayer != null) {
                                        player.sendMessage(ChatColor.DARK_GRAY + "Current Leader: " + ChatColor.RED + CreatureHunt.leadingPlayer);
                                        player.sendMessage(ChatColor.DARK_GRAY + "With a score of: " + ChatColor.RED + CreatureHunt.leadingScore);
                                    } else {
                                        player.sendMessage(ChatColor.DARK_GRAY + "Current Leader: " + ChatColor.GRAY + "no one!");
                                        player.sendMessage(ChatColor.DARK_GRAY + "With a score of: " + ChatColor.GRAY + "nothing!");
                                    }
                                    player.sendMessage(ChatColor.GRAY + "=====================================");
                                }
                            } else {
                                if (CreatureHunt.asyncTask.state == 0) {
                                    player.sendMessage(ChatColor.DARK_AQUA + "The Mob Hunt is yet to begin.");
                                    if (Bukkit.getWorld(mainWorld) != null) {
                                        long worldTime = Bukkit.getWorld(mainWorld).getTime();
                                        long time = worldTime < signupStart ? worldTime - signupStart : 24000 - worldTime + signupStart;
                                        String timeAddon = time == 0 ? "" : " in";
                                        player.sendMessage(ChatColor.DARK_AQUA + "You will be able to signup" + timeAddon + ": " + getTime(time, ChatColor.DARK_AQUA, ChatColor.AQUA) + ChatColor.DARK_AQUA + ".");
                                    } else {
                                        CreatureHunt.instance.getLogger().severe("Could not find world '" + mainWorld + "'");
                                    }
                                } else if (CreatureHunt.asyncTask.state == 1) {
                                    player.sendMessage(ChatColor.DARK_AQUA + "You are not currently signed up for the Mob Hunt!");
                                    if (Bukkit.getWorld(mainWorld) != null) {
                                        long worldTime = Bukkit.getWorld(mainWorld).getTime();
                                        long time = worldTime < signupEnd ? worldTime - signupEnd : 0;
                                        String timeAddon = time == 0 ? "" : " in";
                                        player.sendMessage(ChatColor.DARK_AQUA + "Signups will end" + timeAddon + ": " + getTime(time, ChatColor.DARK_AQUA, ChatColor.AQUA) + ChatColor.DARK_AQUA + ".");
                                    } else {
                                        CreatureHunt.instance.getLogger().severe("Could not find world '" + mainWorld + "'");
                                    }
                                } else if (CreatureHunt.asyncTask.state == 2) {
                                    player.sendMessage(ChatColor.DARK_AQUA + "The Mob Hunt will begin soon...");
                                } else if (CreatureHunt.asyncTask.state == 3) {
                                    player.sendMessage(ChatColor.DARK_AQUA + "The Mob Hunt is currently underway.");
                                    if (Bukkit.getWorld(mainWorld) != null) {
                                        long worldTime = Bukkit.getWorld(mainWorld).getTime();
                                        long time = worldTime < signupStart ? worldTime - signupStart : 24000 - worldTime + signupStart;
                                        String timeAddon = time == 0 ? "" : " in";
                                        player.sendMessage(ChatColor.DARK_AQUA + "Signups will reopen" + timeAddon + ": " + getTime(time, ChatColor.DARK_AQUA, ChatColor.AQUA) + ChatColor.DARK_AQUA + ".");
                                    } else {
                                        CreatureHunt.instance.getLogger().severe("Could not find world '" + mainWorld + "'");
                                    }
                                }
                            }
                        }
                    } else {
                        sender.sendMessage(ChatColor.DARK_RED + "You must be in-game to use this command.");
                    }
                } else {
                    sender.sendMessage(ChatColor.DARK_RED + "Invalid command. Use: " + ChatColor.RED + "/hunt" + ChatColor.DARK_RED + " to see all available commands.");
                }
            } else {
                sender.sendMessage(ChatColor.GRAY + "========= " + ChatColor.DARK_GRAY + "Creature Hunt Commands" + ChatColor.GRAY + " =========");
                sender.sendMessage(ChatColor.DARK_GRAY + " - " + ChatColor.RED + "/hunt join" + ChatColor.DARK_GRAY + " >> " + ChatColor.GRAY + "join the hunt!");
                sender.sendMessage(ChatColor.DARK_GRAY + " - " + ChatColor.RED + "/hunt leave" + ChatColor.DARK_GRAY + " >> " + ChatColor.GRAY + "leave the hunt!");
                sender.sendMessage(ChatColor.DARK_GRAY + " - " + ChatColor.RED + "/hunt status" + ChatColor.DARK_GRAY + " >> " + ChatColor.GRAY + "info about the hunt!");
                sender.sendMessage(ChatColor.GRAY + "=========================================");
            }
            return true;
        }
        return false;
    }

    private String getTime(long time, ChatColor primary, ChatColor secondary) {
        if (time < 0) {
            time = time * -1;
        }
        long seconds = time / 20;
        long minutes = seconds / 60;
        seconds = seconds - minutes * 60;
        String secondString = seconds == 1 ? "second" : "seconds";
        String minuteString = minutes == 1 ? "minute" : "minutes";
        if (time == 0) {
            return secondary + "soon";
        }
        String timeString = "";
        if (minutes > 0) {
            timeString += "" + secondary + minutes + " " + primary + minuteString;
        }
        if (seconds > 0 && minutes > 0) {
            timeString += " and " + secondary + seconds + " " + primary + secondString;
        } else if (seconds > 0 && minutes == 0) {
            timeString += "" + secondary + seconds + " " + primary + secondString;
        }
        return timeString;
    }

}
