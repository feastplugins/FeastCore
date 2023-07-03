package com.emanuelvini.feastcore.bukkit.setup;


import com.emanuelvini.feastcore.bukkit.setup.loader.dependecies.DependencyFinder;
import com.emanuelvini.feastcore.bukkit.setup.loader.events.EventFinder;
import com.emanuelvini.feastcore.bukkit.setup.loader.plugin.PluginFinder;
import com.emanuelvini.feastcore.common.loader.MainFeast;
import com.emanuelvini.feastcore.common.logging.BridgeLogger;
import com.emanuelvini.feastcore.common.storage.MySQL;
import com.emanuelvini.feastcore.common.storage.configuration.MySQLConfiguration;
import com.henryfabio.sqlprovider.connector.SQLConnector;
import lombok.Getter;
import lombok.val;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class MainBukkit extends JavaPlugin {


    @Getter
    private static MainFeast instance;

    @Getter
    private static MainBukkit bukkitPluginInstance;

    @Override
    public void onEnable() {
        bukkitPluginInstance = this;

        super.onEnable();
        saveDefaultConfig();


        if (Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit") == null ||
                !Bukkit.getPluginManager().getPlugin("FastAsyncWorldEdit").isEnabled()) {

            Bukkit.getConsoleSender().
                    sendMessage("§9[FeastCore] §c§lERRO FATAL! §cFastAsyncWorldEdit não encontrado.");
            Bukkit.getPluginManager().disablePlugin(this);
            return;

        }



        Bukkit.getConsoleSender().
                sendMessage("§9[FeastCore] §bInicializando MySQL...");

        SQLConnector mysql;

        try {
            val mysqlSection = getConfig().getConfigurationSection("mysql");
            mysql = MySQL.of(MySQLConfiguration.builder().
                    host(mysqlSection.getString("host")).
                    port(mysqlSection.getInt("port")).
                    database(mysqlSection.getString("database")).
                    username(mysqlSection.getString("user")).
                    password(mysqlSection.getString("password")).build()
            );
            Bukkit.getConsoleSender().
                    sendMessage(
                            "§9[FeastCore] §aMySQL inicializado com sucesso!"
                    );
        } catch (Exception e) {
            Bukkit.getConsoleSender().
                    sendMessage(
                            "§9[FeastCore] §cOcorreu um erro ao inicializar o MySQL. Verifique os dados na configurações."
                    );

            Bukkit.getConsoleSender().
                    sendMessage("§9[FeastCore] §cInforme o erro abaixo: ");
            e.printStackTrace();
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        val dependencyFinder = new DependencyFinder(this);


        val pluginFinder = new PluginFinder(this);
        val eventFinder = new EventFinder();

        instance = new MainFeast(dependencyFinder, eventFinder, pluginFinder, mysql, new BridgeLogger(true), true);
        instance.enable();
    }

    @Override
    public void onDisable() {
        super.onDisable();
        if (instance != null && bukkitPluginInstance != null) {
            instance.disable();
        }
    }
}