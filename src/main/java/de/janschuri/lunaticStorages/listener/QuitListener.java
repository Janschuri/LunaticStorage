package de.janschuri.lunaticStorages.listener;

import de.janschuri.lunaticStorages.LunaticStorage;
import de.janschuri.lunaticStorages.nms.PacketHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    private final LunaticStorage plugin;

    public QuitListener(LunaticStorage plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        PacketHandler packetHandler = new PacketHandler(plugin, p);
        packetHandler.removePacketInjector(p);
    }
}
