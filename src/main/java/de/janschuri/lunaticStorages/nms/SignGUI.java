package de.janschuri.lunaticStorages.nms;


import de.janschuri.lunaticStorages.LunaticStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundOpenSignEditorPacket;
import net.minecraft.network.protocol.game.ServerboundSignUpdatePacket;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_20_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.util.Consumer;

public class SignGUI {

    private final LunaticStorage plugin;
    public SignGUI(LunaticStorage plugin) {

        this.plugin = plugin;
    }

    public void sendSign(Player p, Consumer<String[]> lines) {

        PacketHandler packetHandler = new PacketHandler(plugin, p);

        packetHandler.addPacketInjector(p); // Ensure a packet play is present

        Location l = p.getLocation();
        BlockPos pos = new BlockPos(l.getBlockX(), l.getBlockY(), l.getBlockZ()); // Create a sign GUI on the player
        BlockState old = ((CraftWorld) l.getWorld()).getHandle().getBlockState(pos); // Get the old block state for that position

        ClientboundBlockUpdatePacket sent1 = new ClientboundBlockUpdatePacket(pos, Blocks.OAK_SIGN.defaultBlockState());
        ((CraftPlayer) p).getHandle().connection.send(sent1); // Set that position to a sign

        ClientboundOpenSignEditorPacket sent2 = new ClientboundOpenSignEditorPacket(pos, true);
        ((CraftPlayer) p).getHandle().connection.send(sent2); // Open the sign editor

        PacketHandler.PACKET_HANDLERS.put(p.getUniqueId(), packetO -> {
            if (!(packetO instanceof ServerboundSignUpdatePacket packet)) return false; // Only intercept sign packets

            ClientboundBlockUpdatePacket sent3 = new ClientboundBlockUpdatePacket(pos, old);
            ((CraftPlayer) p).getHandle().connection.send(sent3); // Reset the block state for that packet

            lines.accept(packet.getLines()); // Accept the consumer here
            return true;
        });
    }
}
