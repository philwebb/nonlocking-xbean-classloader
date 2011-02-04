package org.apache.xbean.classloader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.net.URL;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import junit.framework.TestCase;

/**
 * Test the {@link JarResourceHandle}
 * 
 * @author Phillip Webb
 */
public class JarResourceHandleTest extends TestCase {

	private JarResourceHandle resourceHandle;
	private JarFile jarFile;
	private JarEntry jarEntry;
	private URL codeSourceUrl;

	protected void setUp() throws Exception {
		this.jarFile = mock(JarFile.class);
		this.jarEntry = mock(JarEntry.class);
		this.codeSourceUrl = new URL("file:///");
		when(jarEntry.getName()).thenReturn("entryName");
		this.resourceHandle = new JarResourceHandle(jarFile, jarEntry, codeSourceUrl);
		reset(jarEntry);
	}

	/**
	 * Given a {@link JarResourceHandle} when getName() is called then a suitable delegate method is called.
	 * @throws Exception
	 */
	public void testGetName() throws Exception {
		resourceHandle.getName();
		verify(jarEntry).getName();
	}

	/**
	 * Given a {@link JarResourceHandle} when getUrl() is called then a suitable delegate method is called.
	 * @throws Exception
	 */
	public void testGetUrl() throws Exception {
		URL url = resourceHandle.getUrl();
		assertEquals("jar:file:/!/entryName", url.toString());
	}

	/**
	 * Given a {@link JarResourceHandle} when getCodeSourceUrl() is called then a suitable delegate method is called.
	 * @throws Exception
	 */
	public void testGetCodeSourceUrl() throws Exception {
		assertSame(codeSourceUrl, resourceHandle.getCodeSourceUrl());
	}

	/**
	 * Given a {@link JarResourceHandle} when isDirectory() is called then a suitable delegate method is called.
	 * @throws Exception
	 */
	public void testIsDirectory() throws Exception {
		resourceHandle.isDirectory();
		verify(jarEntry).isDirectory();
	}

	/**
	 * Given a {@link JarResourceHandle} when getInputStream() is called then a suitable delegate method is called.
	 * @throws Exception
	 */
	public void testGetInputStream() throws Exception {
		resourceHandle.getInputStream();
		verify(jarFile).getInputStream(jarEntry);
	}

	/**
	 * Given a {@link JarResourceHandle} when getContentLength() is called then a suitable delegate method is called.
	 * @throws Exception
	 */
	public void testGetContentLength() throws Exception {
		resourceHandle.getContentLength();
		verify(jarEntry).getSize();
	}

	/**
	 * Given a {@link JarResourceHandle} when getManifest() is called then a suitable delegate method is called.
	 * @throws Exception
	 */
	public void testGetManifest() throws Exception {
		resourceHandle.getManifest();
		verify(jarFile).getManifest();
	}

	/**
	 * Given a {@link JarResourceHandle} when getAttributes() is called then a suitable delegate method is called.
	 * @throws Exception
	 */
	public void testGetAttributes() throws Exception {
		resourceHandle.getAttributes();
		verify(jarEntry).getAttributes();
	}

	/**
	 * Given a {@link JarResourceHandle} when getCertificates() is called then a suitable delegate method is called.
	 * @throws Exception
	 */
	public void testGetCertificates() throws Exception {
		resourceHandle.getCertificates();
		verify(jarEntry).getCertificates();
	}
}
