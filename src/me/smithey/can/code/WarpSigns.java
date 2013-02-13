package me.smithey.can.code;

import java.io.File;
import java.io.IOException;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

public class WarpSigns extends JavaPlugin implements Listener {
	public void onEnable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		getServer().getPluginManager().registerEvents(this, this);
		System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is enabled!" );
		try {
			Metrics metrics = new Metrics(this);
			metrics.start();
		} catch (IOException e) {
			// Failed to submit the stats :-(
		}
	}

	public void onDisable() {
		PluginDescriptionFile pdfFile = this.getDescription();
		System.out.println( pdfFile.getName() + " version " + pdfFile.getVersion() + " is Disabled!" );
	}

	@EventHandler
	public void onSignChange(SignChangeEvent e) {
		Player p = e.getPlayer();
		if (e.getLine(0).equalsIgnoreCase("[Warp]"))
			p.sendMessage("Debug: Made sign with [Warp] on line 0");
			if(p.hasPermission("warpsigns.create")){
				p.sendMessage("Debug: Has warpsigns.create perm");
				if (e.getLine(1).isEmpty()) {
					p.sendMessage(ChatColor.RED + "Please enter a warp name on line 2!");
					e.setCancelled(true);
				} else {
					e.setLine(0, "�1[Warp]");
					p.sendMessage(ChatColor.GREEN + "WarpSign Created");
				}
			}else{
				p.sendMessage("Debug: No perm for warpsigns.create");
				p.sendMessage(ChatColor.RED + "You don't have Permission to create a WarpSign");
				e.setCancelled(true);
			}
	}

	@EventHandler
	public void onPlayerInteract(PlayerInteractEvent ev) {
		if (ev.getAction() == Action.RIGHT_CLICK_BLOCK) {
			Block b = ev.getClickedBlock();
			if ((b.getType() == Material.SIGN_POST) || (b.getType() == Material.WALL_SIGN)) {
				Sign s = (Sign)b.getState();
				Player p = ev.getPlayer();
				if (s.getLine(0).equalsIgnoreCase("�1[Warp]")){
					p.sendMessage("Debug: Clicked sign with [Warp] as line 0");
					if(s.getLine(3).startsWith("{") && s.getLine(3).endsWith("}")) { // Detects if they want to use per sign permission
						p.sendMessage("Debug: Found PsP on sign");
						String perm = s.getLine(3).replace("{", ""); // Removes {
						String fperm = perm.replace("}", ""); // Removes }
						if(p.hasPermission("WarpSigns.use.*") || p.hasPermission("WarpSigns.use." + fperm)) { // Detects if they have the per sign permission
							p.sendMessage("Debug: Has perm for sign and warping");
							ev.getPlayer().performCommand("warp " + s.getLine(1)); // Warps them if they have the per sign perm
						}else{
							p.sendMessage("Debug: Dont have perm for PsP for clicked sign");
							p.sendMessage(ChatColor.RED + "You are missing the permission WarpSigns.use." + fperm);
						}
					}else{
						if (p.hasPermission("warpsigns.use") || p.hasPermission("warpsigns.use.*")){
							p.sendMessage("Debug: Has warpsigns.use or warpsigns.use.*");
							ev.getPlayer().performCommand("warp " + s.getLine(1));
						} else {
							p.sendMessage("Debug: No Perm and isnt using PsP");
							p.sendMessage(ChatColor.RED + "You don't have Permission to use a WarpSign");
						}
					}
				}
			}
		}
	}
	
	public void createConfig() { // Generate config
		YamlConfiguration config = YamlConfiguration.loadConfiguration(new File("plugins/WarpSigns/config.yml"));
		config.set("Enable Per Sign Permission", "true");
		try {
			config.save("plugins/WarpSigns/config.yml");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}