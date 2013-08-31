package com.oresomecraft.creaturehunt;

import java.util.HashMap;
import java.util.logging.Logger;

import net.milkbowl.vault.economy.Economy;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import com.oresomecraft.creaturehunt.data.HuntStatStorage;
import com.oresomecraft.creaturehunt.listeners.CreatureKillListener;
import com.oresomecraft.creaturehunt.listeners.CreatureSpawnListener;
import com.oresomecraft.creaturehunt.listeners.EntrantDeathListener;

public class CreatureHunt extends JavaPlugin {

    private final Logger logger = Logger.getLogger("Minecraft");
    private PluginDescriptionFile pdfFile;
    
    public static final Object LOCK = new Object();
    
    public static Economy econ = null;
    
    public static volatile HashMap<String, HuntStatStorage> enteredPlayers;
    
    public static volatile String leadingPlayer;
    public static volatile short leadingScore;
    
    public static CreatureHunt instance = null;
    
    public static CreatureHuntAsyncTask asyncTask;
    
	@Override
	public void onEnable() {
        instance = this;
        
        pdfFile = this.getDescription();
        logger.info(pdfFile.getName() + " [V" + pdfFile.getVersion() + "] is enabling...");
        saveDefaultConfig();
        
        if (!setupEconomy()) {
            logger.severe(String.format("[%s] - Disabled due to no Vault dependency found!", this.getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        
        enteredPlayers = new HashMap<String, HuntStatStorage>();
        
        getServer().getPluginManager().registerEvents(new CreatureSpawnListener(), this);
        getServer().getPluginManager().registerEvents(new CreatureKillListener(), this);
        getServer().getPluginManager().registerEvents(new EntrantDeathListener(), this);
        
        getCommand("hunt").setExecutor(new CreatureHuntCommands());
        
        asyncTask = new CreatureHuntAsyncTask(getConfig().getString("Overworld"), getConfig().getString("Nether"));
        asyncTask.runTaskTimerAsynchronously(this, 20L, 40L);
        
        logger.info(pdfFile.getName() + " [V" + pdfFile.getVersion() + "] is enabled!");
        
	}
	
	@Override
	public void onDisable() {
        logger.info(pdfFile.getName() + " [V" + pdfFile.getVersion() + "] is disabling...");
        Bukkit.getServer().getScheduler().cancelTasks(this);
        synchronized (LOCK) {
	        if (asyncTask.state == 2) {
	        	logger.info("Returning balances to players...");
	        	for (String player : enteredPlayers.keySet()) {
	        		econ.depositPlayer(player, getConfig().getDouble("EntryFee"));
	        	}
	        }
        }
        instance = null;
        logger.info(pdfFile.getName() + " [V" + pdfFile.getVersion() + "] is disabled!");
	}
	
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }
}
