package org.apache.xbean.classloader;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;

public class TstUtils {

	private static final String SAMPLE_JAR = "sample.jar";

	public static final String SAMPLE_JAR_CLASS = "org/apache/commons/codec/BinaryDecoder.class";

	public static final URL[] EMPTY_URLS = new URL[] {};

	public static File createTempJarFile() throws IOException {
		File file = File.createTempFile("nlj", ".jar");
		OutputStream outputStream = new FileOutputStream(file);
		IOUtils.copy(TstUtils.class.getResourceAsStream(SAMPLE_JAR), outputStream);
		IOUtils.closeQuietly(outputStream);
		return file;
	}

	public static void deleteTempFile(File file) {
		if (file.exists()) {
			file.delete();
		}
	}

	public static void doWithTempFile(byte[] contents, TempFileCallback callback) throws Exception {
		File file = File.createTempFile("drh", "tmp");
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(file);
			try {
				IOUtils.write(contents, fileOutputStream);
			} finally {
				fileOutputStream.close();
			}
			callback.doWithFile(file);
		} finally {
			if (file.exists()) {
				file.delete();
			}
		}
	}

	public static interface TempFileCallback {
		void doWithFile(File file) throws Exception;
	}

	public static File getTempFolder() {
		return new File(System.getProperty("java.io.tmpdir"));
	}

	public static File createTempDir() throws IOException {
		File tempFolder = File.createTempFile("tmp", "tmp");
		if (!tempFolder.delete()) {
			throw new IOException("Unable to delete file " + tempFolder);
		}
		if (!tempFolder.mkdir()) {
			throw new IOException("Unable to create folder " + tempFolder);
		}
		return tempFolder;
	}

	public static void assertFileExists(File file) {
		Assert.assertTrue("File should exist: " + file, file.canRead());
	}

	public static void assertFileNotExists(File file) {
		Assert.assertTrue("File should not exist: " + file, !file.canRead());
	}

}
