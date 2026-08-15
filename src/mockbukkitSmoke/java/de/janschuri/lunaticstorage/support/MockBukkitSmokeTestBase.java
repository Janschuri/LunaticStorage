package de.janschuri.lunaticstorage.support;

import be.seeseemelk.mockbukkit.MockBukkit;
import be.seeseemelk.mockbukkit.ServerMock;
import be.seeseemelk.mockbukkit.plugin.MockBukkitConfiguredPluginClassLoader;
import de.janschuri.lunaticstorage.LunaticStorage;
import org.bukkit.plugin.InvalidDescriptionException;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public abstract class MockBukkitSmokeTestBase {

    private static final String PLUGIN_JAR_PROPERTY = "lunaticstorage.test.pluginJar";

    protected ServerMock server;
    protected JavaPlugin plugin;
    private JarFile pluginJarFile;

    @BeforeEach
    void setUpMockBukkit() {
        System.setProperty("lunaticstorage.testMode", "true");

        server = MockBukkit.mock();

        try {
            String pluginJarPath = System.getProperty(PLUGIN_JAR_PROPERTY);

            if (pluginJarPath != null && !pluginJarPath.isBlank()) {
                plugin = loadPluginFromJar(new File(pluginJarPath));
            } else {
                plugin = MockBukkit.load(LunaticStorage.class);
            }

            if (plugin == null) {
                throw new IllegalStateException("MockBukkit did not load the LunaticStorage plugin.");
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not load LunaticStorage in MockBukkit", e);
        }
    }

    @AfterEach
    void tearDownMockBukkit() throws IOException {
        MockBukkit.unmock();
        if (pluginJarFile != null) {
            pluginJarFile.close();
            pluginJarFile = null;
        }
        System.clearProperty(PLUGIN_JAR_PROPERTY);
        System.clearProperty("lunaticstorage.testMode");
    }

    private JavaPlugin loadPluginFromJar(File pluginJar)
            throws IOException, InvalidDescriptionException, NoSuchMethodException,
            InvocationTargetException, InstantiationException, IllegalAccessException {
        pluginJarFile = new JarFile(pluginJar);

        JarEntry pluginYml = pluginJarFile.getJarEntry("plugin.yml");
        if (pluginYml == null) {
            throw new IllegalStateException("plugin.yml was not found in " + pluginJar.getAbsolutePath());
        }

        PluginDescriptionFile description;
        try (InputStream inputStream = pluginJarFile.getInputStream(pluginYml)) {
            description = new PluginDescriptionFile(inputStream);
        }

        File dataFolder = server.getPluginManager().createTemporaryDirectory(description.getName());
        MockBukkitConfiguredPluginClassLoader classLoader =
                new MockBukkitConfiguredPluginClassLoader(server, description, dataFolder, pluginJar);
        classLoader.setJarFile(pluginJarFile);

        Class<? extends JavaPlugin> proxyClass = classLoader.loadProxyClass(LunaticStorage.class);
        JavaPlugin loadedPlugin = proxyClass.getDeclaredConstructor().newInstance();

        server.getPluginManager().registerLoadedPlugin(loadedPlugin);
        server.getPluginManager().enablePlugin(loadedPlugin);
        return loadedPlugin;
    }
}
