package com.fintech.Config;

import liquibase.Liquibase;
import liquibase.database.Database;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.resource.ClassLoaderResourceAccessor;

import javax.sql.DataSource;
import java.sql.Connection;

public class LiquidBaseConfig {

    public static void runMigration(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            Database database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(new JdbcConnection(connection));

            Liquibase liquibase =new Liquibase("db/changelog/db.changelog-master.xml", new ClassLoaderResourceAccessor(),database);
            liquibase.update("");

        } catch (Exception e) {
            throw new RuntimeException("Liquibase migration failed", e);
        }
    }
}