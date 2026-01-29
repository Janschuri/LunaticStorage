package de.janschuri.lunaticstorage.external;

import de.diddiz.LogBlock.Actor;
import de.janschuri.lunaticstorage.LunaticStorage;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;

public class LogBlock {

    private LogBlock() {
    }

    public static void logChestRemove(Entity entity, Block block, ItemStack itemStack) {
        if (!LunaticStorage.isInstalledLogBlock()) {
            return;
        }

        de.diddiz.LogBlock.LogBlock.getInstance().getConsumer().queueChestAccess(Actor.actorFromEntity(entity), block.getState(), itemStack, true);
    }

    public static void logChestInsert(Entity entity, Block block, ItemStack itemStack) {
        if (!LunaticStorage.isInstalledLogBlock()) {
            return;
        }

        de.diddiz.LogBlock.LogBlock.getInstance().getConsumer().queueChestAccess(Actor.actorFromEntity(entity), block.getState(), itemStack, false);
    }
}
