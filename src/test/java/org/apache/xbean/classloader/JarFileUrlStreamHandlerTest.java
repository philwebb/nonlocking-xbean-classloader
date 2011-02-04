package org.apache.xbean.classloader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.net.URLConnection;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import junit.framework.TestCase;

/**
 * Test for {@link JarFileUrlStreamHandler}.
 * 
 * @author Phillip Webb
 */
public class JarFileUrlStreamHandlerTest extends TestCase {

	private JarFile jarFile;
	private JarEntry jarEntry;
	private JarFileUrlStreamHandler handler;

	protected void setUp() throws Exception {
		super.setUp();
		this.jarFile = mock(JarFile.class);
		this.jarEntry = mock(JarEntry.class);
		when(jarFile.getName()).thenReturn("sample.jar");
		this.handler = new JarFileUrlStreamHandler(jarFile, jarEntry);
	}

	/**
	 * Given a {@link JarFileUrlStreamHandler} when constructed with null arguments an {@link IllegalArgumentException}
	 * is thown.
	 * 
	 * @throws Exception
	 */
	public void testNullConstructorArgumentsThrowsIllegalArgumentException() throws Exception {
		try {
			new JarFileUrlStreamHandler(null, mock(JarEntry.class));
			fail("Did not throw");
		} catch (IllegalArgumentException e) {
			assertEquals("Illegal null jarFile specified for JarFileUrlStreamHandler", e.getMessage());
		}
		try {
			new JarFileUrlStreamHandler(mock(JarFile.class), null);
			fail("Did not throw");
		} catch (IllegalArgumentException e) {
			assertEquals("Illegal null jarEntry specified for JarFileUrlStreamHandler", e.getMessage());
		}
	}

	/**
	 * Given a {@link JarFileUrlStreamHandler} when setExpectedUrl() is called with a null argument then an
	 * {@link IllegalArgumentException} is thrown.
	 * @throws Exception
	 */
	public void testSetNullExpectedUrlThrowsIllegalArgumentException() throws Exception {
		try {
			new JarFileUrlStreamHandler(mock(JarFile.class), mock(JarEntry.class)).setExpectedUrl(null);
			fail("Did not throw");
		} catch (IllegalArgumentException e) {
			assertEquals("Illegal null expectedUrl specified for setExpectedUrl", e.getMessage());
		}
	}

	/**
	 * Given a {@link JarFileUrlStreamHandler} when openConnection() is called without setting the expected URL then an
	 * exception is thrown.
	 * 
	 * @throws Exception
	 */
	public void testOpenConnectionWithoutSettingExpectedUrl() throws Exception {
		try {
			handler.openConnection(new URL("http://"));
			fail("Did not throw");
		} catch (IllegalStateException e) {
			assertEquals("expectedUrl was not set", e.getMessage());
		}
	}

	/**
	 * Given a {@link JarFileUrlConnection} when openConnection() is called against the expected URL then a correctly
	 * configured {@link JarFileUrlConnection} is returned.
	 * @throws Exception
	 */
	public void testOpenConnectionWithUrlAsExpected() throws Exception {
		URL url = new URL("file:///sample.jar!/test.class");
		handler.setExpectedUrl(url);
		URLConnection connection = handler.openConnection(url);
		assertTrue(connection instanceof JarFileUrlConnection);
		assertSame(jarFile, ((JarFileUrlConnection) connection).getJarFile());
		assertSame(jarEntry, ((JarFileUrlConnection) connection).getJarEntry());
	}

	/**
	 * Given a {@link JarFileUrlConnection} that is being used because an existing handler is reused (in other words,
	 * then the expected URL does not match the actual URL) when openConnection() is called then a correctly configured
	 * {@link JarFileUrlConnection} is returned.
	 * @throws Exception
	 */
	public void testOpenConnectionWithWhenHandlerIsReused() throws Exception {
		URL url = new URL("file:///sample2.jar!/test.class");
		handler.setExpectedUrl(url);
		JarEntry newJarEntry = mock(JarEntry.class);
		when(jarFile.getJarEntry("test/test.class")).thenReturn(newJarEntry);
		URLConnection connection = new URL("jar", null, 0, "file:sample.jar!/test/test.class", handler)
				.openConnection();
		assertSame(jarFile, ((JarFileUrlConnection) connection).getJarFile());
		assertSame(newJarEntry, ((JarFileUrlConnection) connection).getJarEntry());
	}
}
