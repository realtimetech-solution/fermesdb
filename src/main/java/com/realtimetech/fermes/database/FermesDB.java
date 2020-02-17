package com.realtimetech.fermes.database;

import java.io.File;
import java.lang.instrument.Instrumentation;

import com.realtimetech.fermes.database.exception.DatabaseReadException;

public class FermesDB {
	private static volatile Instrumentation globalInstrumentation;

	protected static Instrumentation getGlobalInstrumentation() {
		return globalInstrumentation;
	}

	public static void premain(final String agentArgs, final Instrumentation instrumentation) {
		globalInstrumentation = instrumentation;
	}

	public static Database loadDatabase(File databaseDirectory) throws DatabaseReadException {
		return new Database(databaseDirectory, Long.MAX_VALUE);
	}

	public static Database loadDatabase(File databaseDirectory, long maxMemory) throws DatabaseReadException {
		return new Database(databaseDirectory, maxMemory);
	}

	public static Database createDatabase(File databaseDirectory, int pageSize, int blockSize, long maxMemory) throws DatabaseReadException {
		return new Database(databaseDirectory, pageSize, blockSize, maxMemory);
	}

	public static boolean deleteDatabase(File databaseDirectory) {
		if (databaseDirectory.exists() && databaseDirectory.isDirectory()) {
			File[] listFiles = databaseDirectory.listFiles();
			for (File file : listFiles) {
				file.delete();
			}
			databaseDirectory.delete();

			return true;
		}

		return false;
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

	public static Database get(File databaseDirectory, int pageSize, int blockSize, long maxMemory) throws DatabaseReadException {
		if (existDatabase(databaseDirectory)) {
			return loadDatabase(databaseDirectory);
		}

		return createDatabase(databaseDirectory, pageSize, blockSize, maxMemory);
	}
}
