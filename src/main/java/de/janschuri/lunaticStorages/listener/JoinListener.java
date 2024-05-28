package de.janschuri.lunaticStorages.listener;

import de.janschuri.lunaticStorages.LunaticStorage;
import de.janschuri.lunaticlib.nms.PacketHandler;
import de.janschuri.lunaticlib.platform.bukkit.nms.Version;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinListener implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        PacketHandler packetHandler = Version.getPacketHandler(LunaticStorage.getInstance(), p);
        packetHandler.addPacketInjector(LunaticStorage.getInstance(), p);
    }
}
