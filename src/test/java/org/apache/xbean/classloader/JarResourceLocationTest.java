package org.apache.xbean.classloader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import junit.framework.TestCase;

/**
 * Test the {@link JarResourceLocation}
 * 
 * @author Phillip Webb
 */
public class JarResourceLocationTest extends TestCase {

	private URL codeSource;
	private JarFile jarFile;
	private JarResourceLocation jarResourceLocation;

	protected void setUp() throws Exception {
		super.setUp();
		this.codeSource = new URL("file://");
		this.jarFile = mock(JarFile.class);
		this.jarResourceLocation = new JarResourceLocation(codeSource, jarFile);
	}

	/**
	 * Given a {@link JarResourceLocation} when constructed with a null jarFile then an {@link IllegalArgumentException}
	 * is thrown.
	 * @throws Exception
	 */
	public void testConstructorWithNullJarFileThrowsIllegalArgumentException() throws Exception {
		try {
			new JarResourceLocation(codeSource, null);
			fail("Did not throw");
		} catch (IllegalArgumentException e) {
			assertEquals("Illegal null jarFile specified for JarResourceLocation", e.getMessage());
		}
	}

	/**
	 * Given a {@link JarResourceLocation} when getResourceHandle() is called and the resourceName is not a jar file
	 * entry then null is returned.
	 * @throws Exception
	 */
	public void testGetResourceHandleForMissingEntry() throws Exception {
		assertNull(jarResourceLocation.getResourceHandle("test"));
	}

	/**
	 * Given a {@link JarResourceLocation} when getResourceHandle() is called and the resourceName is a jar file entry
	 * then a valid {@link JarResourceHandle} is returned.
	 * @throws Exception
	 */
	public void testGetResourceHandleForValidEntry() throws Exception {
		String resourceName = "test";
		JarEntry entry = mock(JarEntry.class);
		when(jarFile.getJarEntry(resourceName)).thenReturn(entry);
		ResourceHandle handle = jarResourceLocation.getResourceHandle(resourceName);
		assertNotNull(handle);
		assertTrue(handle instanceof JarResourceHandle);
		assertSame(codeSource, ((JarResourceHandle) handle).getCodeSourceUrl());
	}

	/**
	 * Given a {@link JarResourceLocation} when getManifest() is called then the method delegates to the jar file.
	 * @throws Exception
	 */
	public void testGetManifest() throws Exception {
		jarResourceLocation.getManifest();
		verify(jarFile).getManifest();
	}

	/**
	 * Given a {@link JarResourceLocation} when close() is called then the jar file is closed.
	 * @throws Exception
	 */
	public void testClose() throws Exception {
		jarResourceLocation.close();
		verify(jarFile).close();
	}
}
