package org.apache.xbean.classloader;

import java.io.File;
import java.util.jar.JarFile;

import junit.framework.TestCase;

import org.apache.xbean.classloader.UrlResourceFinder.JarFileFactory;

/**
 * Test the {@link NonLockingJarFileClassLoader}.
 * 
 * @author Phillip Webb
 */
public class NonLockingJarFileClassLoaderTest extends TestCase {

	/**
	 * Given a {@link NonLockingJarFileClassLoader} when a newResourceFinder is constructed then it should be setup to
	 * use {@link NonLockingJarFile}s.
	 * 
	 * @throws Exception
	 */
	public void testURLResourceFinderIsNonLocking() throws Exception {
		NonLockingJarFileClassLoader loader = new NonLockingJarFileClassLoader("test", TstUtils.EMPTY_URLS);
		UrlResourceFinder resourceFinder = loader.newResourceFinder();
		JarFileFactory jarFileFactory = UrlResourceFinderTest.getJarFileFactory(resourceFinder);
		File tempJarFile = TstUtils.createTempJarFile();
		try {
			JarFile jarFile = jarFileFactory.newJarFile(tempJarFile);
			assertTrue(jarFile instanceof NonLockingJarFile);
		} finally {
			TstUtils.deleteTempFile(tempJarFile);
		}
	}
}
