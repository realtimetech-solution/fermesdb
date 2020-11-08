package com.realtimetech.fermes.example;

import com.realtimetech.fermes.database.Database;
import com.realtimetech.fermes.database.FermesDB;
import com.realtimetech.fermes.database.Link;
import com.realtimetech.fermes.example.objects.Dummy;
import com.realtimetech.fermes.example.objects.DummyManager;

import java.io.File;

public class TestForSaveIncreaseException {
    public static void main(String[] args) throws Exception {
        FermesDB.deleteDatabase(new File("save_increaseException_db/"));

        Database database = FermesDB.get(new File("save_increaseException_db/"), 1024 * 1024, 1024 * 128, Long.MAX_VALUE);

        Link<DummyManager> dummyManager = database.getLink("dummy_manager", () -> new DummyManager());

        dummyManager.get().addDummy(new Dummy(1234));
        dummyManager.get().addDummy(new Dummy(1234));

        File backupTestFile = new File("test.zip");
        long lastSize = -1;

        for (int i = 0; i < 100; i++) {
            backupTestFile.delete();

            database.saveAndBackup(backupTestFile);

            if (lastSize != -1) {
                if (backupTestFile.length() != lastSize) {
                    System.out.println("Diff size " + backupTestFile.length() + " vs " + lastSize + " (" + (backupTestFile.length() - lastSize) + ")");
                }
            }

            lastSize = backupTestFile.length();
        }
        backupTestFile.delete();
    }
}
