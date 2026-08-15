package de.janschuri.lunaticstorage.support;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import de.janschuri.lunaticstorage.LunaticStorage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

public abstract class MockBukkitTestBase {

    protected ServerMock server;
    protected LunaticStorage plugin;

    @BeforeEach
    void setUpMockBukkit() {
        System.setProperty("lunaticstorage.testMode", "true");

        server = MockBukkit.mock();
        plugin = MockBukkit.load(LunaticStorage.class);
    }

    @AfterEach
    void tearDownMockBukkit() {
        MockBukkit.unmock();
        System.clearProperty("lunaticstorage.testMode");
    }
}
