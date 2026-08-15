package de.janschuri.lunaticstorage;

import de.janschuri.lunaticstorage.support.MockBukkitTestBase;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LunaticStoragePluginTest extends MockBukkitTestBase {

    @Test
    void loadsPluginAndDefaultConfigurationOnMockServer() {
        assertTrue(plugin.isEnabled());
        assertNotNull(LunaticStorage.getPluginConfig());
        assertNotNull(LunaticStorage.getLanguageConfig());
        assertEquals("DIAMOND", plugin.getConfig().getString("storage_item"));
        assertEquals("LODESTONE", plugin.getConfig().getString("panel_block"));
        assertEquals(5, plugin.getConfig().getInt("default_range"));
    }
}
