package com.emanuelvini.feastcore.bukkit.loader.finder;


import com.emanuelvini.feastcore.bukkit.MainFeast;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import lombok.val;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.HashMap;

@AllArgsConstructor
public class DependencyFinder {


    private final MainFeast plugin;


    private final HashMap<String, String> dependencies = new HashMap<>();

    public void addDependency(String name, String url) {

        if (!dependencies.containsKey(name)) {
            dependencies.put(name, url);
        }
    }

    public void disableAll () {
       for (String name : dependencies.keySet()) {
           val plugin = Bukkit.getPluginManager().getPlugin(name);
           if (plugin != null) {
               try {
                   Bukkit.getPluginManager().disablePlugin(plugin);

                   Bukkit.getConsoleSender().
                           sendMessage(String.format(
                                   "§9[FeastCore] §aDependência §f%s§a desabilitada com sucesso!", name)
                           );
               } catch (Exception e) {
                   Bukkit.getConsoleSender().
                           sendMessage(String.format
                                   ("§9[FeastCore] §cOcorreu um erro ao desabilitar a dependência §f%s§c:", name)
                           );
                   e.printStackTrace();
               }
           }
       }
       dependencies.clear();
    }

    @SneakyThrows
    public void downloadAndLoadDependencies() {
        val dependenciesDirectory = new File(plugin.getDataFolder(), "dependencies");
        if (!dependenciesDirectory.exists()) dependenciesDirectory.mkdirs();
        for (String name : dependencies.keySet()) {
            val url = dependencies.get(name);
            val dependencyFile = new File(dependenciesDirectory,
                    name + ".jar"
            );
            if (!dependencyFile.exists()) {
                val download = new URL(url);
                ReadableByteChannel rbc = Channels.newChannel(
                        download.openStream()
                );
                FileOutputStream fos = new FileOutputStream(dependencyFile);
                fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
                Bukkit.getConsoleSender().
                        sendMessage(String.format(
                                "§9[FeastCore] §aDependência §f%s§a baixada com sucesso!",
                                name));
            }
            val plugin = Bukkit.getPluginManager().loadPlugin(dependencyFile);
            if (!plugin.isEnabled()) {
                Bukkit.getPluginManager().enablePlugin(plugin);
            }
            Bukkit.getConsoleSender().
                    sendMessage(String.format(
                            "§9[FeastCore] §aDependência §f%s§a carregada com sucesso!",
                            name));
        }
    }

}
