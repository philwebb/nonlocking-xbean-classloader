package org.apache.xbean.classloader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;

/**
 * Test the {@link ClassLoaderUtil}.
 * 
 * @author Phillip Webb
 */
public class ClassLoaderUtilTest extends TestCase {

	/**
	 * Given a class with a static Map based cache when clearSunSoftCache is called then the cache should be cleared.
	 * 
	 * @throws Exception
	 */
	public void testClearSunSoftCache() throws Exception {
		SampleSoftCache.getCache().put("test", "test");
		ClassLoaderUtil.clearSunSoftCache(SampleSoftCache.class, "cache");
		assertEquals(0, SampleSoftCache.getCache().size());
	}

	/**
	 * Given any class loader when destroy is called then no errors are thrown. This is not much of a test but most of
	 * the destroy code is JVM specific so quite hard to test.
	 * 
	 * @throws Exception
	 */
	public void testClassLoadDestroyForCurrentJVM() throws Exception {
		ClassLoader classLoader = getClass().getClassLoader();
		ClassLoaderUtil.destroy(classLoader);
	}

	@SuppressWarnings("unchecked")
	public void testReleaseCommonsLogging() throws Exception {
		ClassLoader classLoader = mock(ClassLoader.class);
		when(classLoader.loadClass("org.apache.commons.logging.LogFactory")).thenReturn((Class) LogFactory.class);
		ClassLoaderUtil.releaseCommonsLoggingCache(classLoader);
		LogFactory.assertReleased();
	}

	private static class SampleSoftCache {
		private static Map<Object, Object> cache = new HashMap<Object, Object>();

		public static Map<Object, Object> getCache() {
			return cache;
		}
	}

	public static class LogFactory {
		private static boolean released = false;;

		public static void release(ClassLoader classLoader) {
			released = true;
		}

		public static void assertReleased() {
			assertTrue(released);
		}
	}
}
