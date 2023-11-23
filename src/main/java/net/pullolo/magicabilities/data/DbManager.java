package net.pullolo.magicabilities.data;

import net.pullolo.magicabilities.powers.PowerType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;

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

    public boolean connect(){
        try{
            Class.forName("org.sqlite.JDBC");
            this.conn = DriverManager.getConnection("jdbc:sqlite:plugins/"+plugin.getDataFolder().getName()+"/data.db");
            Statement stmt = conn.createStatement();
            String sql = "create table if not exists powers (name TEXT PRIMARY KEY NOT NULL, power TEXT NOT NULL);";
            stmt.execute(sql);
            stmt.close();
            conn.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean isDbEnabled(){
        return conn!=null;
    }

    public boolean isPlayerInDb(String name){
        boolean is = false;
        try{
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:plugins/"+plugin.getDataFolder().getName()+"/data.db");
            PreparedStatement stmt = conn.prepareStatement("select * from powers where name=?;");
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.isClosed()){
                return false;
            }
            if(rs.getString("name") != null){
                is = true;
            }

            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return is;
    }

    public void addPlayer(String name, PowerType powerType){
        try{
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:plugins/"+plugin.getDataFolder().getName()+"/data.db");
            String insert = "insert into powers (name, power) values" +
                    " (?, ?);";

            PreparedStatement stmt = conn.prepareStatement(insert);
            stmt.setString(1, name);
            stmt.setString(2, powerType.toString());
            stmt.execute();

            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PlayerData getPlayerData(String playerName) {
        PlayerData pd = null;
        if (!isPlayerInDb(playerName)){
            addPlayer(playerName, PowerType.NONE);
        }
        try{
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:plugins/"+plugin.getDataFolder().getName()+"/data.db");
            PreparedStatement stmt = conn.prepareStatement("select * from powers where name=?;");
            stmt.setString(1, playerName);

            ResultSet rs = stmt.executeQuery();
            pd = new PlayerData(playerName, PowerType.valueOf(rs.getString("power")));

            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return pd;
    }

    public void updatePlayer(String name, PlayerData pd){
        try{
            Class.forName("org.sqlite.JDBC");
            Connection conn = DriverManager.getConnection("jdbc:sqlite:plugins/"+plugin.getDataFolder().getName()+"/data.db");

            String update = "update powers set power=? where name=?;";
            PreparedStatement stmt = conn.prepareStatement(update);
            stmt.setString(1, pd.getPower().toString());
            stmt.setString(2, name);
            stmt.execute();

            stmt.close();
            conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
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
