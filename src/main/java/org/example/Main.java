package org.example;

import org.bukkit.plugin.java.*;
import java.net.*;
import java.io.*;
import org.bukkit.*;
import org.bukkit.plugin.*;

import java.nio.charset.Charset;
import java.util.*;
import org.bukkit.command.*;

public class Main extends JavaPlugin
{
    public void onEnable() {
        this.saveDefaultConfig();
        this.runURLChecks();
        this.getCommand("syncbans").setExecutor(this);

    }

    public boolean onCommand(final CommandSender sender, final Command cmd, final String string, final String[] args) {
        if (cmd.getName().equals("syncbans")) {
            if (sender.hasPermission("mcba.syncbans")) {
                this.pullWebsiteData();
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&fMCBanAppeal&7] &cSync command sent!"));
            }
            else {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', "&7[&fMCBanAppeal&7] &cInsufficient permissions: mcba.syncbans"));
            }
            return true;
        }
        return false;
    }

    private void runURLChecks() {

        try {
            URLConnection checkurl = new URL("http://cobramc.rip/unban/tounban.txt").openConnection();
            checkurl.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            checkurl.connect();
            final BufferedReader checktext = new BufferedReader(new InputStreamReader(checkurl.getInputStream(), Charset.forName("UTF-8")));
            final String checkconnection = checktext.readLine();
            if (checkconnection == null) {
                this.getServer().getConsoleSender().sendMessage("[AZ] " + ChatColor.RED + "Connection was made, but the 'tounban.txt' file appears empty, therefore, the application will not function correctly!");
                checktext.close();
            }
            else {
                checktext.close();
                this.getServer().getConsoleSender().sendMessage("[AZ] " + ChatColor.DARK_GREEN + "Checking bans every " + this.getConfig().getInt("UnbanInterval") + " seconds...");
                Bukkit.getScheduler().scheduleSyncRepeatingTask(this, () -> this.pullWebsiteData(), 0L, (this.getConfig().getInt("UnbanInterval") * 20));
            }
        }
        catch (Exception e) {
            this.getServer().getConsoleSender().sendMessage("[AZ] " + ChatColor.RED + "The connection could not be made! Is your URL in the config correct?");
            Bukkit.getScheduler().cancelTasks(this);
        }
    }

    private void pullWebsiteData() {
        try {
            URLConnection checkusers = new URL("http://cobramc.rip/unban/tounban.txt").openConnection();
            checkusers.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
            checkusers.connect();
            final BufferedReader textin = new BufferedReader(new InputStreamReader(checkusers.getInputStream(), Charset.forName("UTF-8")));
            String line;
            while ((line = textin.readLine()) != null) {
                if (!line.equals("**** PLEASE DO NOT EDIT OR DELETE THIS FILE ****") && !line.equals("")) {
                    URLConnection removeURL = new URL("http://cobramc.rip/unban/" + "unban.php?username=" + line.trim() + "&key=" + this.getConfig().getString("URLKey")).openConnection();
                    removeURL.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.11 (KHTML, like Gecko) Chrome/23.0.1271.95 Safari/537.11");
                    removeURL.connect();

                    final BufferedReader removeText = new BufferedReader(new InputStreamReader(removeURL.getInputStream(), Charset.forName("UTF-8")));
                    final String removeLine = removeText.readLine();
                    if (removeLine == null) {
                        this.getLogger().info("Running commands for " + line.trim());
                        final StringTokenizer command = new StringTokenizer(this.getConfig().getString("Command"), "-");
                        while (command.hasMoreTokens()) {
                            Bukkit.getServer().dispatchCommand(Bukkit.getServer().getConsoleSender(), command.nextToken().replace("%player%", line.trim()));
                            removeText.close();
                        }
                    }
                    else {
                        removeText.close();
                        this.getServer().getConsoleSender().sendMessage("[MCBanAppeal] " + ChatColor.RED + "Wrong Admin Key! Edit your config with the correct key and reload the plugin!");
                        Bukkit.getScheduler().cancelTasks(this);
                    }
                }
            }
            textin.close();
        }
        catch (Exception e) {
            this.getServer().getConsoleSender().sendMessage("[MCBanAppeal] " + ChatColor.RED + "The connection could not be made! Is your URL in the config correct?");
            Bukkit.getScheduler().cancelTasks(this);
            if (this.getConfig().getBoolean("StackTrace")) {
                e.printStackTrace();
            }
        }
    }
}