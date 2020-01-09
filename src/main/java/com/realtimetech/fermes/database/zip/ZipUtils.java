package com.realtimetech.fermes.database.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ZipUtils {
	private static int BUFFER_SIZE = 1024 * 4;

	public static void zipFolder(final File folder, final File zipFile) throws IOException {
		zipFolder(folder, new FileOutputStream(zipFile));
	}

	public static void zipFolder(final File folder, final OutputStream outputStream) throws IOException {
		try (ZipOutputStream zipOutputStream = new ZipOutputStream(outputStream)) {
			processFolder(folder, zipOutputStream, folder.getPath().length() + 1);
		}
	}

	private static void processFolder(final File folder, final ZipOutputStream zipOutputStream, final int prefixLength) throws IOException {
		for (final File file : folder.listFiles()) {
			if (file.isFile()) {
				final ZipEntry zipEntry = new ZipEntry(file.getPath().substring(prefixLength));
				zipOutputStream.putNextEntry(zipEntry);
				try (FileInputStream inputStream = new FileInputStream(file)) {
					byte[] buffer = new byte[BUFFER_SIZE];
					int read = 0;
					while ((read = inputStream.read(buffer)) != -1) {
						zipOutputStream.write(buffer, 0, read);
					}
				}
				zipOutputStream.closeEntry();
			} else if (file.isDirectory()) {
				processFolder(file, zipOutputStream, prefixLength);
			}
		}
	}
}