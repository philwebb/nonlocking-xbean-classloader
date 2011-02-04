package org.apache.xbean.classloader;

import java.io.File;
import java.net.URL;

import org.junit.Test;

public class TimerTestIT {

	private static final int X = 100;
	private static final int COUNT = 10000;

	@Test
	public void timeConstructor() throws Exception {
		File file = TstUtils.createTempJarFile();
		try {
			URL[] urls = new URL[X];
			for (int i = 0; i < urls.length; i++) {
				urls[i] = file.toURI().toURL();
			}
			long start = System.currentTimeMillis();
			for (int i = 0; i < COUNT; i++) {
				new NonLockingJarFileClassLoader("test", urls);
			}
			long total = System.currentTimeMillis() - start;
			System.out.println("Loaded " + COUNT + " times in " + total + "ms");
		} finally {
			TstUtils.deleteTempFile(file);
		}
	}
}
