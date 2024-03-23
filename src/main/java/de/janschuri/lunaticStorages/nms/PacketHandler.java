package de.janschuri.lunaticStorages.nms;
import de.janschuri.lunaticStorages.LunaticStorage;
import io.netty.channel.Channel;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.ServerCommonPacketListenerImpl;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Predicate;

public final class PacketHandler extends ChannelDuplexHandler {

    private final LunaticStorage plugin;
    private final String PACKET_INJECTOR_ID = "lunaticstorage:packet_handler";
    public static final Map<UUID, Predicate<Packet<?>>> PACKET_HANDLERS = new HashMap<>();

    private final Player p; // Store your target player

    public PacketHandler(LunaticStorage plugin, Player p) {
        this.plugin = plugin;
        this.p = p;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object packetO) throws Exception {
        if (!(packetO instanceof Packet<?> packet)) { // Utilize Java 17 features for pattern matching; Only intercept Packet Data
            super.channelRead(ctx, packetO);
            return;
        }

        Predicate<Packet<?>> handler = PACKET_HANDLERS.get(p.getUniqueId());
        if (handler != null) new BukkitRunnable() {
            public void run() {
                boolean success = handler.test(packet); // Check to make sure that the predicate works
                if (success) PACKET_HANDLERS.remove(p.getUniqueId()); // If successful, remove the packet handler
            }
        }.runTask(plugin);

        super.channelRead(ctx, packetO); // Perform default actions done by the duplex handler
    }

    public void addPacketInjector(Player p) {
        ServerPlayer sp = ((CraftPlayer) p).getHandle();

        try {
            Field connection = ServerCommonPacketListenerImpl.class.getDeclaredField("c");
            connection.setAccessible(true);
            Channel ch = ((Connection) connection.get(sp.connection)).channel;

            if (ch.pipeline().get(PACKET_INJECTOR_ID) != null) return;
            ch.pipeline().addAfter("decoder", PACKET_INJECTOR_ID, new PacketHandler(plugin, p));
        } catch (ReflectiveOperationException e) {
            // handle errors
        }
    }

    public void removePacketInjector(Player p) {
        ServerPlayer sp = ((CraftPlayer) p).getHandle();

        try {
            Field connection = ServerCommonPacketListenerImpl.class.getDeclaredField("c");
            connection.setAccessible(true);
            Channel ch = ((Connection) connection.get(sp.connection)).channel;

            if (ch.pipeline().get(PACKET_INJECTOR_ID) == null) return;
            ch.pipeline().remove(PACKET_INJECTOR_ID);
        } catch (ReflectiveOperationException e) {
            // handle errors
        }
    }

}