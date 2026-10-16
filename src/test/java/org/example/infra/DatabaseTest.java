package org.example.infra;

import org.example.error.StorageException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DatabaseTest {

    @TempDir
    Path tempDir;

    @Test
    void aConnectionToARealFileCanBeOpened() throws SQLException {
        Database db = new Database(tempDir.resolve("station.db").toString());

        try (Connection connection = db.open()) {
            assertFalse(connection.isClosed());
        }
    }

    @Test
    void aPathInAFolderThatDoesNotExistIsReportedAsAStorageError() {
        Database db = new Database(tempDir.resolve("missing-folder").resolve("station.db").toString());

        assertThrows(StorageException.class, db::open);
    }
}