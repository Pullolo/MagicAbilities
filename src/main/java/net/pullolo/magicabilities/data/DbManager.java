package net.pullolo.magicabilities.data;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class DbManager {
    private Connection conn;
    private final JavaPlugin plugin;
    public DbManager(JavaPlugin plugin){
        this.plugin=plugin;
    }
    public void init(){
        File file = new File(plugin.getDataFolder(), "data.db");
        if (!file.exists()){
            file.getParentFile().mkdirs();
            plugin.saveResource("data.db", false);
        }
    }

    public boolean isDbEnabled(){
        return conn!=null;
    }

    public boolean connect(){
        try{
            Class.forName("org.sqlite.JDBC");
            this.conn = DriverManager.getConnection("jdbc:sqlite:plugins/"+plugin.getDataFolder().getName()+"/data.db");
//            Statement stmt = conn.createStatement();
//            String sql = "create table if not exists plugin_data (name TEXT PRIMARY KEY NOT NULL, level INT NOT NULL, xp TEXT NOT NULL," +
//                    " star_essence INT NOT NULL, wishes INT NOT NULL, dungeon_essence INT NOT NULL, updated BOOLEAN NOT NULL);";
//            stmt.execute(sql);
//            stmt.close();
            conn.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void disconnect(){
        try {
            conn.close();
            conn=null;
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
