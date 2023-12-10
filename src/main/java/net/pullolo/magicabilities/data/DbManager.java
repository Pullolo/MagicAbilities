package net.pullolo.magicabilities.data;

import net.pullolo.magicabilities.powers.PowerType;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.*;
import java.util.HashMap;

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
            String sql = "create table if not exists powers (name TEXT PRIMARY KEY NOT NULL, power TEXT NOT NULL, enabled BOOLEAN NOT NULL);";
            stmt.execute(sql);
            String sql2 = "create table if not exists binds (name TEXT PRIMARY KEY NOT NULL," +
                    " ab0 INT NOT NULL, ab1 INT NOT NULL, ab2 INT NOT NULL, ab3 INT NOT NULL, ab4 INT NOT NULL, ab5 INT NOT NULL," +
                    " ab6 INT NOT NULL, ab7 INT NOT NULL, ab8 INT NOT NULL);";
            stmt.execute(sql2);
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
            String insert = "insert into powers (name, power, enabled) values" +
                    " (?, ?, 1);";
            String insert2 = "insert into binds (name, ab0, ab1, ab2, ab3, ab4, ab5, ab6, ab7, ab8) values" +
                    " (?, 0, 1, 2, 3, 4, 5, 6, 7, 8);";

            PreparedStatement stmt = conn.prepareStatement(insert);
            stmt.setString(1, name);
            stmt.setString(2, powerType.toString());
            stmt.execute();
            stmt.close();
            PreparedStatement stmt2 = conn.prepareStatement(insert2);
            stmt2.setString(1, name);
            stmt2.execute();
            stmt2.close();
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
            PreparedStatement stmt2 = conn.prepareStatement("select * from binds where name=?;");
            stmt2.setString(1, playerName);

            ResultSet rs = stmt.executeQuery();
            ResultSet rs2 = stmt2.executeQuery();
            HashMap<Integer, Integer> binds = new HashMap<>();
            for (int i = 0; i<9; i++){
                binds.put(i, rs2.getInt("ab"+i));
            }
            pd = new PlayerData(playerName, PowerType.valueOf(rs.getString("power")), binds, rs.getBoolean("enabled"));

            stmt.close();
            stmt2.close();
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

            String update = "update powers set power=?, enabled=? where name=?;";
            PreparedStatement stmt = conn.prepareStatement(update);
            stmt.setString(1, pd.getPower().toString());
            stmt.setBoolean(2, pd.isEnabled());
            stmt.setString(3, name);
            stmt.execute();
            stmt.close();

            String sql = "update binds set ";
            for (int i = 0; i < 9; i++){
                if (i==8){
                    sql+="ab"+i+"="+pd.getBinds().get(i);
                } else {
                    sql+="ab"+i+"="+pd.getBinds().get(i)+", ";
                }
            }
            sql += " where name=?;";

            PreparedStatement stmt2 = conn.prepareStatement(sql);
            stmt2.setString(1, name);
            stmt2.execute();
            stmt2.close();
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
