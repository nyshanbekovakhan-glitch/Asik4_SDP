package org.example.infra;

import org.example.error.StorageException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class Database {
    private final String url;

    public Database(String filePath) {
        this.url = "jdbc:sqlite:" + filePath;
    }

    public Connection open() {
        try {
            return DriverManager.getConnection(url);
        } catch (SQLException e) {
            throw new StorageException("Cannot open database " + url, e);
        }
    }
}