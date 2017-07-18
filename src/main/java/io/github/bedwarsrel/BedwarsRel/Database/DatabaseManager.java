package io.github.bedwarsrel.BedwarsRel.Database;

import com.zaxxer.hikari.HikariDataSource;
import io.github.bedwarsrel.BedwarsRel.ChatHelper;
import io.github.bedwarsrel.BedwarsRel.Main;
import org.bukkit.ChatColor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class DatabaseManager {

    private String host = null;
    private int port = 3306;
    private String user = null;
    private String password = null;
    private String database = null;
    private HikariDataSource dataSource = null;

    private static DatabaseManager instance = null;

    public static String DBPrefix = "bw_";

    public DatabaseManager(String host, int port, String user, String password, String database) {
        this.host = host;
        this.port = port;
        this.user = user;
        this.password = password;
        this.database = database;
    }

    public void initialize() {
        this.initializePooledDataSource(this.getMinPoolSizeConfig(), this.getMaxPoolSizeConfig());
        DatabaseManager.instance = this;
    }

    public static DatabaseManager getInstance() {
        return DatabaseManager.instance;
    }

    private int getMinPoolSizeConfig() {
        return Main.getInstance().getIntConfig("database.connection-pooling.min-pool-size", 3);
    }

    private int getMaxPoolSizeConfig() {
        return Main.getInstance().getIntConfig("database.connection-pooling.max-pool-size", 15);
    }

    private void initializePooledDataSource(int minPoolSize, int maxPoolSize) {
        try {
            dataSource = new HikariDataSource();

            dataSource.setJdbcUrl(
                    "jdbc:mysql://" + this.host + ":" + String.valueOf(this.port) + "/" + this.database);

            dataSource.setUsername(this.user);
            dataSource.setPassword(this.password);

            // connection pool configuration
            dataSource.setIdleTimeout(600);
            dataSource.setMinimumIdle(minPoolSize);
            dataSource.setMaximumPoolSize(maxPoolSize);
        } catch (Exception ex) {
            Main.getInstance().getServer().getConsoleSender().sendMessage(ChatHelper
                    .with(ChatColor.RED + "Couldn't create pooled datasource: " + ex.getMessage()));
        }
    }

    public Connection getDataSourceConnection() {
        try {
            return dataSource.getConnection();
        } catch (SQLException e) {
            Main.getInstance().getServer().getConsoleSender().sendMessage(ChatHelper
                    .with(ChatColor.RED + "Couldn't get a pooled connection: " + e.getMessage()));
        }

        return null;
    }

    public static Connection getConnection() {
        return DatabaseManager.instance.getDataSourceConnection();
    }

    public void cleanUp() {
        if (dataSource != null) {
            dataSource.close();
        }
    }

    public void clean(Connection dbConnection) {
        try {
            if (dbConnection == null) {
                return;
            }

            if (!dbConnection.isClosed()) {
                dbConnection.close();
            }
        } catch (Exception ex) {
            Main.getInstance().getBugsnag().notify(ex);
            ex.printStackTrace();
        }
    }

    public void cleanStatement(Statement statement) {
        try {
            if (statement == null) {
                return;
            }

            if (!statement.isClosed()) {
                statement.close();
            }
        } catch (Exception ex) {
            Main.getInstance().getBugsnag().notify(ex);
            ex.printStackTrace();
        }
    }

    public void cleanResult(ResultSet result) {
        try {
            if (result == null) {
                return;
            }

            if (!result.isClosed()) {
                result.close();
            }
        } catch (Exception ex) {
            Main.getInstance().getBugsnag().notify(ex);
            ex.printStackTrace();
        }
    }

    public void execute(String... sqls) throws SQLException {
        Connection con = null;
        Statement statement = null;

        if (sqls.length == 0) {
            return;
        }

        try {
            con = this.getDataSourceConnection();
            statement = con.createStatement();

            if (sqls.length == 1) {
                statement.execute(sqls[0]);
            } else {
                for (String sql : sqls) {
                    statement.addBatch(sql);
                }

                statement.executeBatch();
            }
        } finally {
            this.clean(con);
        }
    }

    public ResultSet query(String sql) {
        Connection con = null;
        Statement statement = null;
        ResultSet result = null;

        try {
            con = this.getDataSourceConnection();
            statement = con.createStatement();
            result = statement.executeQuery(sql);

            return result;
        } catch (Exception ex) {
            Main.getInstance().getBugsnag().notify(ex);
            ex.printStackTrace();
            this.clean(con);
        }

        return null;
    }

    public int getRowCount(ResultSet result) {
        int size = 0;
        try {
            result.last();
            size = result.getRow();
            result.beforeFirst();

            return size;
        } catch (Exception ex) {
            Main.getInstance().getBugsnag().notify(ex);
            return 0;
        }
    }

    public void update(String sql) {
        Connection con = null;
        Statement statement = null;

        try {
            con = this.getDataSourceConnection();
            statement = con.createStatement();

            statement.executeUpdate(sql);
        } catch (Exception ex) {
            Main.getInstance().getBugsnag().notify(ex);
            ex.printStackTrace();
        } finally {
            this.clean(con);
            this.cleanStatement(statement);
        }
    }

    public void insert(String sql) {
        this.update(sql);
    }

    public void delete(String sql) {
        this.update(sql);
    }
}
