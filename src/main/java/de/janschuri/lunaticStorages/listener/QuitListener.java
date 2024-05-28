package de.janschuri.lunaticStorages.listener;

import de.janschuri.lunaticStorages.LunaticStorage;
import de.janschuri.lunaticlib.nms.PacketHandler;
import de.janschuri.lunaticlib.platform.bukkit.nms.Version;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    @EventHandler
    public void onLeave(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        PacketHandler packetHandler = Version.getPacketHandler(LunaticStorage.getInstance(), p);
        packetHandler.removePacketInjector(p);
    }
}
