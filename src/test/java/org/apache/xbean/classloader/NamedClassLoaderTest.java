package org.apache.xbean.classloader;

import junit.framework.TestCase;

/**
 * Test the {@link NamedClassLoader}.
 * 
 * @author Phillip Webb
 */
public class NamedClassLoaderTest extends TestCase {

	/**
	 * Given a new {@link NamedClassLoader} when isDestroyed() is called then the result is false.
	 * @throws Exception
	 */
	public void testIsDestroyedDefaultsToFalse() throws Exception {
		assertFalse(new NamedClassLoader("test", TstUtils.EMPTY_URLS).isDestroyed());
	}

	/**
	 * Given a {@link NamedClassLoader} when destroy() is called then the result of isDestroyed() changes to true.
	 * @throws Exception
	 */
	public void testDestroyChangesIsDestroyed() throws Exception {
		NamedClassLoader loader = new NamedClassLoader("test", TstUtils.EMPTY_URLS);
		loader.destroy();
		assertTrue(loader.isDestroyed());
	}

	/**
	 * Given a {@link NamedClassLoader} when getName() is called then the result the value passed on construction.
	 * @throws Exception
	 */
	public void testGetName() throws Exception {
		assertEquals("test", new NamedClassLoader("test", TstUtils.EMPTY_URLS).getName());

	}

	/**
	 * Given a {@link NamedClassLoader} when toString() is called then the result is sensible.
	 * @throws Exception
	 */
	public void testToString() throws Exception {
		assertEquals("[org.apache.xbean.classloader.NamedClassLoader: name=test urls=[]]", new NamedClassLoader("test",
				TstUtils.EMPTY_URLS).toString());
	}

}
