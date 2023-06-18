package io.github.realguystuff.ChatServer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.jar.JarFile;

import org.yaml.snakeyaml.Yaml;

public class PluginManager {
	private static PluginManager instance;
    private List<ChatPlugin> loadedPlugins;

    public PluginManager() {
        loadedPlugins = new ArrayList<>();
    }

    public static PluginManager getInstance() {
        if (instance == null) {
            instance = new PluginManager();
        }
        return instance;
    }
    
    public void loadPlugins(String pluginDirectoryPath) {
        File pluginDirectory = new File(pluginDirectoryPath);

        if (pluginDirectory.exists() && pluginDirectory.isDirectory()) {
            File[] pluginFiles = pluginDirectory.listFiles();

            if (pluginFiles != null) {
                for (File pluginFile : pluginFiles) {
                    if (pluginFile.isFile() && pluginFile.getName().endsWith(".jar")) {
                        loadPlugin(pluginFile);
                    }
                }
            }
        }
    }

    private void loadPlugin(File pluginFile) {
    	if (loadedPlugins.isEmpty()) {
            System.out.println("No plugins loaded.");
            return;
        }
    	
        // Check if plugin.yml exists
        File pluginYamlFile = new File(pluginFile.getParentFile(), "plugin.yml");
        if (!pluginYamlFile.exists()) {
            throw new RuntimeException("Error: Missing plugin.yml for plugin: " + pluginFile.getName());
        }

        // Load the plugin
        URLClassLoader classLoader = null;
        JarFile jarFile = null;

        try {
            URL pluginURL = pluginFile.toURI().toURL();
            classLoader = new URLClassLoader(new URL[]{pluginURL});
            jarFile = new JarFile(pluginFile);

            // Read plugin.yml file
            InputStream pluginYamlStream = jarFile.getInputStream(jarFile.getEntry("plugin.yml"));
            Yaml yaml = new Yaml();
            Map<String, Object> pluginYamlData = yaml.load(pluginYamlStream);

            // Get the main class from plugin.yml
            String mainClass = (String) pluginYamlData.get("main");

            if (mainClass == null) {
                System.err.println("Error: Missing 'main' field in plugin.yml for plugin: " + pluginFile.getName());
                return;
            }

            // Load the main class dynamically
            Class<?> pluginClass = classLoader.loadClass(mainClass);

            // Create an instance of the plugin class
            ChatPlugin chatPlugin = (ChatPlugin) pluginClass.getDeclaredConstructor().newInstance();

            // Initialize the plugin
            chatPlugin.onEnable();

            // Add the plugin to the loaded plugins list
            loadedPlugins.add(chatPlugin);

            System.out.println("Loaded plugin: " + (String) pluginYamlData.get("name") + " by " + (String) pluginYamlData.get("author") + ".");
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (classLoader != null) {
                try {
                    classLoader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (jarFile != null) {
                try {
                    jarFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    public void unloadPlugins() {
        for (ChatPlugin plugin : loadedPlugins) {
            plugin.onDisable();
        }

        loadedPlugins.clear();
    }

    // Invoke a method in all loaded plugins
    public void invokeMethodInPlugins(String methodName, Object... args) {
        for (ChatPlugin plugin : loadedPlugins) {
            try {
                plugin.getClass().getMethod(methodName, getParameterTypes(args)).invoke(plugin, args);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private Class<?>[] getParameterTypes(Object[] args) {
        Class<?>[] parameterTypes = new Class<?>[args.length];
        for (int i = 0; i < args.length; i++) {
            parameterTypes[i] = args[i].getClass();
        }
        return parameterTypes;
    }
}
