package org.apache.xbean.classloader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.security.Permission;
import java.security.cert.Certificate;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import junit.framework.TestCase;

/**
 * Test the {@link JarFileUrlConnection}.
 * 
 * @author Phillip Webb
 */
public class JarFileUrlConnectionTest extends TestCase {

	private URL url;
	private JarFile jarFile;
	private JarEntry jarEntry;
	private JarFileUrlConnection connection;

	protected void setUp() throws Exception {
		super.setUp();
		this.url = new URL("file:///");
		this.jarFile = mock(JarFile.class);
		this.jarEntry = mock(JarEntry.class);
		when(jarFile.getName()).thenReturn("sample.jar");
		this.connection = new JarFileUrlConnection(url, jarFile, jarEntry);
	}

	/**
	 * Given a {@link JarFileUrlConnection} when constructed with null arguments then {@link IllegalArgumentException}s
	 * are thrown.
	 * @throws Exception
	 */
	public void testConstructorIllegalArguments() throws Exception {
		try {
			new JarFileUrlConnection(null, mock(JarFile.class), mock(JarEntry.class));
			fail("Did not throw");
		} catch (IllegalArgumentException e) {
			assertEquals("Illegal null url specified for JarFileUrlConnection", e.getMessage());
		}

		try {
			new JarFileUrlConnection(new URL("file:///"), null, mock(JarEntry.class));
			fail("Did not throw");
		} catch (IllegalArgumentException e) {
			assertEquals("Illegal null jarFile specified for JarFileUrlConnection", e.getMessage());
		}

		try {
			new JarFileUrlConnection(new URL("file:///"), mock(JarFile.class), null);
			fail("Did not throw");
		} catch (IllegalArgumentException e) {
			assertEquals("Illegal null jarEntry specified for JarFileUrlConnection", e.getMessage());
		}
	}

	/**
	 * Given a {@link JarFileUrlConnection} when getJarFile is called then the implementation will delegate.
	 * @throws Exception
	 */
	public void testGetJarFile() throws Exception {
		assertSame(jarFile, connection.getJarFile());
	}

	/**
	 * Given a {@link JarFileUrlConnection} when getJarFileURL is called then the implementation will delegate.
	 * @throws Exception
	 */
	public void testGetJarFileURL() throws Exception {
		assertEquals(new File("sample.jar").toURI().toURL(), connection.getJarFileURL());
	}

	/**
	 * Given a {@link JarFileUrlConnection} when getEntryName is called then the implementation will delegate.
	 * @throws Exception
	 */
	public void testgetEntryName() throws Exception {
		when(jarEntry.getName()).thenReturn("entryname");
		assertEquals("entryname", connection.getEntryName());
	}

	/**
	 * Given a {@link JarFileUrlConnection} when getManifest is called then the implementation will delegate.
	 * @throws Exception
	 */
	public void testGetManifest() throws Exception {
		Manifest manifest = new Manifest();
		when(jarFile.getManifest()).thenReturn(manifest);
		assertSame(manifest, connection.getManifest());

	}

	/**
	 * Given a {@link JarFileUrlConnection} when getJarEntry is called then the implementation will delegate.
	 * @throws Exception
	 */
	public void testGetJarEntry() throws Exception {
		assertSame(jarEntry, connection.getJarEntry());

	}

	/**
	 * Given a {@link JarFileUrlConnection} when getAttributes is called then the implementation will delegate.
	 * @throws Exception
	 */
	public void testGetAttributes() throws Exception {
		Attributes attributes = new Attributes();
		when(jarEntry.getAttributes()).thenReturn(attributes);
		assertSame(attributes, connection.getAttributes());

	}

	/**
	 * Given a {@link JarFileUrlConnection} when getMainAttributes is called then the implementation will delegate.
	 * @throws Exception
	 */
	public void testGetMainAttributes() throws Exception {
		Manifest manifest = new Manifest();
		when(jarFile.getManifest()).thenReturn(manifest);
		assertSame(manifest.getMainAttributes(), connection.getMainAttributes());
	}

	/**
	 * Given a {@link JarFileUrlConnection} when getCertificates is called then the implementation will delegate.
	 * @throws Exception
	 */
	public void testGetCertificates() throws Exception {
		Certificate[] certificates = new Certificate[] {};
		when(jarEntry.getCertificates()).thenReturn(certificates);
		assertSame(certificates, connection.getCertificates());
	}

	/**
	 * Given a {@link JarFileUrlConnection} when getURL is called then the implementation will delegate.
	 * @throws Exception
	 */
	public void testGetURL() throws Exception {
		assertSame(url, connection.getURL());
	}

	/**
	 * Given a {@link JarFileUrlConnection} when getLastModified is called then the implementation will delegate.
	 * @throws Exception
	 */
	public void testGetLastModified() throws Exception {
		long time = System.currentTimeMillis();
		when(jarEntry.getTime()).thenReturn(time);
		assertEquals(time, connection.getLastModified());

	}

	/**
	 * Given a {@link JarFileUrlConnection} when getContentLength is called then the implementation will delegate.
	 * @throws Exception
	 */
	public void testGetContentLength() throws Exception {
		when(jarEntry.getSize()).thenReturn(123L);
		assertEquals(123, connection.getContentLength());
	}

	/**
	 * Given a {@link JarFileUrlConnection} backed by a file larger than the maximum int size when getContentLength is
	 * called then -1 is returned.
	 * @throws Exception
	 */
	public void testGetContentLengtWhenTooBig() throws Exception {
		when(jarEntry.getSize()).thenReturn(Integer.MAX_VALUE + 1L);
		assertEquals(-1, connection.getContentLength());
	}

	/**
	 * Given a {@link JarFileUrlConnection} when getInputStream is called then the implementation will delegate.
	 * @throws Exception
	 */
	public void testGetInputStream() throws Exception {
		InputStream inputStream = mock(InputStream.class);
		when(jarFile.getInputStream(jarEntry)).thenReturn(inputStream);
		assertSame(inputStream, connection.getInputStream());
	}

	/**
	 * Given a {@link JarFileUrlConnection} when getPermission is called then the implementation will delegate.
	 * @throws Exception
	 */
	public void testGetPermission() throws Exception {
		Permission permission = new File("sample.jar").toURI().toURL().openConnection().getPermission();
		assertEquals(permission, connection.getPermission());
	}

	/**
	 * Given a {@link JarFileUrlConnection} when toString is called then the result is readable.
	 * @throws Exception
	 */

	public void testToString() throws Exception {
		assertEquals("org.apache.xbean.classloader.JarFileUrlConnection:file:/", connection.toString());
	}
}
