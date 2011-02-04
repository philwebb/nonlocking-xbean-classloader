package org.apache.xbean.classloader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.jar.Manifest;

import junit.framework.TestCase;

/**
 * Test the {@link AbstractResourceHandle}.
 * 
 * @author Phillip Webb
 */
public class AstractResourceHandleTest extends TestCase {

	/**
	 * Given a resource handle with a valid input stream when getBytes() is called then the data is read from the stream
	 * and the stream is closed.
	 * 
	 * @throws Exception
	 */
	public void testGetBytesFromInputStream() throws Exception {
		AbstractResourceHandle resourceHandle = mock(AbstractResourceHandle.class);
		byte[] bytes = "test".getBytes();
		InputStream inputStream = spy(new ByteArrayInputStream(bytes));
		when(resourceHandle.getInputStream()).thenReturn(inputStream);
		when(resourceHandle.getBytes()).thenCallRealMethod();
		assertTrue("bytes do not match", Arrays.equals(bytes, resourceHandle.getBytes()));
		verify(resourceHandle).getBytes();
		verify(inputStream).close();
	}

	/**
	 * Give a resource handle with a input stream that throws an exception when getBytes() is called then the input
	 * stream is closed.
	 * 
	 * @throws Exception
	 */
	public void testGetBytesClosesInputStreamWhenStreamThrows() throws Exception {
		AbstractResourceHandle resourceHandle = mock(AbstractResourceHandle.class);
		InputStream inputStream = spy(new ThowingInputStream());
		when(resourceHandle.getInputStream()).thenReturn(inputStream);
		when(resourceHandle.getBytes()).thenCallRealMethod();
		try {
			resourceHandle.getBytes();
			fail("Did not throw IOException");
		} catch (IOException e) {
		}
		verify(resourceHandle).getBytes();
		verify(inputStream).close();
	}

	/**
	 * Given a resource handle that has not overridden any methods when getManifest() is called then null is returned.
	 * 
	 * @throws Exception
	 */
	public void testGetManifestDefaultsToNull() throws Exception {
		AbstractResourceHandle resourceHandle = mock(AbstractResourceHandle.class);
		when(resourceHandle.getManifest()).thenCallRealMethod();
		assertNull(resourceHandle.getManifest());
		verify(resourceHandle).getManifest();
	}

	/**
	 * Given a resource handle that has not overridden any methods when getCertificates() is called then null is
	 * returned.
	 * 
	 * @throws Exception
	 */
	public void testgetCertificatesDefaultsToNull() throws Exception {
		AbstractResourceHandle resourceHandle = mock(AbstractResourceHandle.class);
		when(resourceHandle.getCertificates()).thenCallRealMethod();
		assertNull(resourceHandle.getCertificates());
		verify(resourceHandle).getCertificates();
	}

	/**
	 * Given a resource handle that returns a null manifest when getAttributes() is called then null is returned.
	 * 
	 * @throws Exception
	 */
	public void testGetAttributesWithNullManifestIsNull() throws Exception {
		AbstractResourceHandle resourceHandle = mock(AbstractResourceHandle.class);
		when(resourceHandle.getManifest()).thenReturn(null);
		when(resourceHandle.getAttributes()).thenCallRealMethod();
		assertNull("A null manifest should return null attributes", resourceHandle.getAttributes());
		verify(resourceHandle).getManifest();
		verify(resourceHandle).getAttributes();
	}

	/**
	 * Given a resource handle that returns a manifest when getAttributes is called then the result is taken from the
	 * manifest.
	 * 
	 * @throws Exception
	 */
	public void testGetAttributesDelegateToManifest() throws Exception {
		final String file = "/filename";
		AbstractResourceHandle resourceHandle = mock(AbstractResourceHandle.class);
		Manifest manifest = mock(Manifest.class);
		URL url = new URL("file://" + file);
		when(resourceHandle.getManifest()).thenReturn(manifest);
		when(resourceHandle.getUrl()).thenReturn(url);
		when(resourceHandle.getAttributes()).thenCallRealMethod();
		resourceHandle.getAttributes();
		verify(manifest).getAttributes(file);
	}

	/**
	 * Given a resource handle when toString is called then the result is readable.
	 * 
	 * @throws Exception
	 */
	public void testToString() throws Exception {
		AbstractResourceHandle resourceHandle = mock(AbstractResourceHandle.class);
		when(resourceHandle.getName()).thenReturn("name");
		when(resourceHandle.getUrl()).thenReturn(new URL("file:///url"));
		when(resourceHandle.getCodeSourceUrl()).thenReturn(new URL("file:///codesourceurl"));
		when(resourceHandle.toString()).thenCallRealMethod();
		assertEquals("[name: file:/url; code source: file:/codesourceurl]", resourceHandle.toString());
	}

	/**
	 * Simple {@link InputStream} implementation that will always throw {@link IOException}s on read.
	 */
	private static class ThowingInputStream extends InputStream {
		public int read() throws IOException {
			throw new IOException();
		}
	}
}
