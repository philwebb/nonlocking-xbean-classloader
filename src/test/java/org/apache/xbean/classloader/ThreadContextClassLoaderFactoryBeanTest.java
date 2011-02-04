package org.apache.xbean.classloader;

import junit.framework.TestCase;

/**
 * Test the {@link ThreadContextClassLoaderFactoryBean}.
 * 
 * @author Phillip Webb
 */
public class ThreadContextClassLoaderFactoryBeanTest extends TestCase {

	/**
	 * Given a {@link ThreadContextClassLoaderFactoryBean} when getObject() is called then the object is the context
	 * classloader for the current thread.
	 * @throws Exception
	 */
	public void testGetObject() throws Exception {
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		assertSame(loader, new ThreadContextClassLoaderFactoryBean().getObject());
	}

	/**
	 * Given a {@link ThreadContextClassLoaderFactoryBean} when getObjectType() is called then ClassLoader is returned.
	 * @throws Exception
	 */
	public void testGetObjectType() throws Exception {
		assertEquals(ClassLoader.class, new ThreadContextClassLoaderFactoryBean().getObjectType());
	}

	/**
	 * Given a {@link ThreadContextClassLoaderFactoryBean} when isSingleton() is called then the result is true.
	 * @throws Exception
	 */
	public void testIsSingleton() throws Exception {
		assertTrue(new ThreadContextClassLoaderFactoryBean().isSingleton());
	}
}
