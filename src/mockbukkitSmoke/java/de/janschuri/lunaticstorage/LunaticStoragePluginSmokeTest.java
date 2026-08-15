package de.janschuri.lunaticstorage;

import de.janschuri.lunaticstorage.support.MockBukkitSmokeTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LunaticStoragePluginSmokeTest extends MockBukkitSmokeTestBase {

    @Test
    void loadsPluginJarAndDefaultConfigurationOnMockServer() {
        assertTrue(plugin.isEnabled());
        assertEquals("LunaticStorage", plugin.getName());
        assertEquals("DIAMOND", plugin.getConfig().getString("storage_item"));
        assertEquals("LODESTONE", plugin.getConfig().getString("panel_block"));
        assertEquals(5, plugin.getConfig().getInt("default_range"));
    }
}
