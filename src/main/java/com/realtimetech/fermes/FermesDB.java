package com.realtimetech.fermes;

import java.io.File;
import com.realtimetech.fermes.database.Database;
import com.realtimetech.fermes.exception.FermesDatabaseException;

public class FermesDB {
	public static Database loadDatabase(File databaseDirectory) throws FermesDatabaseException {
		return new Database(databaseDirectory);
	}

	public static Database createDatabase(File databaseDirectory, int pageSize, int blockSize, long maxMemory)
			throws FermesDatabaseException {
		return new Database(databaseDirectory, pageSize, blockSize, maxMemory);
	}

	public static boolean existDatabase(File databaseDirectory) {
		if (databaseDirectory.isDirectory() && databaseDirectory.exists()) {
			File configFile = new File(databaseDirectory, "database.config");

			if (configFile.exists()) {
				return true;
			}
		}

		return false;
	}

	public static Database get(File databaseDirectory, int pageSize, int blockSize, long maxMemory)
			throws FermesDatabaseException {
		if (existDatabase(databaseDirectory)) {
			return loadDatabase(databaseDirectory);
		}

		return createDatabase(databaseDirectory, pageSize, blockSize, maxMemory);
	}
}
