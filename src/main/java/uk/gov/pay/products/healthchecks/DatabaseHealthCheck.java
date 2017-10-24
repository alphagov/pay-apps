package uk.gov.pay.products.healthchecks;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.db.DataSourceFactory;

import javax.inject.Inject;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

public class DatabaseHealthCheck extends HealthCheck {

    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private static final Map<String, Long> longDatabaseStatsMap;
    private static final Map<String, Double> doubleDatabaseStatsMap;
    private Integer statsHealthy = 0;

    static {
        longDatabaseStatsMap = new HashMap<String, Long>();
        longDatabaseStatsMap.put("numbackends", 0l);
        longDatabaseStatsMap.put("numbackends", 0l);
        longDatabaseStatsMap.put("xact_commit", 0l);
        longDatabaseStatsMap.put("xact_rollback", 0l);
        longDatabaseStatsMap.put("blks_read", 0l);
        longDatabaseStatsMap.put("blks_hit", 0l);
        longDatabaseStatsMap.put("tup_returned", 0l);
        longDatabaseStatsMap.put("tup_fetched", 0l);
        longDatabaseStatsMap.put("tup_inserted", 0l);
        longDatabaseStatsMap.put("tup_updated", 0l);
        longDatabaseStatsMap.put("tup_deleted", 0l);
        longDatabaseStatsMap.put("conflicts", 0l);
        longDatabaseStatsMap.put("temp_files", 0l);
        longDatabaseStatsMap.put("temp_bytes", 0l);
        longDatabaseStatsMap.put("deadlocks", 0l);
        doubleDatabaseStatsMap = new HashMap<>();
        doubleDatabaseStatsMap.put("blk_read_time", 0.0);
        doubleDatabaseStatsMap.put("blk_write_time", 0.0);
    }

    @Inject
    public DatabaseHealthCheck(DataSourceFactory dataSourceFactory, MetricRegistry metricRegistry) {
        this.dbUrl = dataSourceFactory.getUrl();
        this.dbUser = dataSourceFactory.getUser();
        this.dbPassword = dataSourceFactory.getPassword();
        initialiseMetrics(metricRegistry);
    }

    private void initialiseMetrics(MetricRegistry metricRegistry) {
        for (String key : longDatabaseStatsMap.keySet()) {
            metricRegistry.<Gauge<Long>>register("productsdb." + key, () -> longDatabaseStatsMap.get(key));
        }
        for (String key : doubleDatabaseStatsMap.keySet()) {
            metricRegistry.<Gauge<Double>>register("productsdb." + key, () -> doubleDatabaseStatsMap.get(key));
        }
        metricRegistry.<Gauge<Integer>>register("productsdb.stats_healthy", () -> statsHealthy);
    }

    @Override
    protected Result check() throws Exception {
        Connection connection = null;
        try {
            connection = DriverManager.getConnection(dbUrl, dbUser, dbPassword);
            connection.setReadOnly(true);
            updateMetricData(connection);
            return connection.isValid(2) ? Result.healthy() : Result.unhealthy("Could not validate the DB connection.");
        } catch (Exception e) {
            return Result.unhealthy(e.getMessage());
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
    }

    private void updateMetricData(Connection connection) {
        try (Statement statement = connection.createStatement()) {
            statement.execute("select * from pg_stat_database where datname='products';");
            try (ResultSet resultSet = statement.getResultSet()) {
                resultSet.next();
                for (String key : longDatabaseStatsMap.keySet()) {
                    longDatabaseStatsMap.put(key, resultSet.getLong(key));
                }
                for (String key : doubleDatabaseStatsMap.keySet()) {
                    doubleDatabaseStatsMap.put(key, resultSet.getDouble(key));
                }
            }
            statsHealthy = 1;
        } catch (SQLException e) {
            statsHealthy = 0;
        }
    }
}
