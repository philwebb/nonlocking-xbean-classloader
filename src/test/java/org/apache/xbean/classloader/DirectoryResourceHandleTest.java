package org.apache.xbean.classloader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.io.File;
import java.util.Arrays;
import java.util.jar.Manifest;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;
import org.apache.xbean.classloader.TstUtils.TempFileCallback;

/**
 * Test the {@link DirectoryResourceHandle}.
 * 
 * @author Phillip Webb
 */
public class DirectoryResourceHandleTest extends TestCase {

	/**
	 * Given a {@link DirectoryResourceHandle} with valid constructor arguments when getters are called then suitable
	 * data is returned.
	 * 
	 * @throws Exception
	 */
	public void testConstructorWithValidArguments() throws Exception {
		File file = new File("test");
		File codeSource = new File("codeSource");
		Manifest manifest = new Manifest();
		DirectoryResourceHandle handle = new DirectoryResourceHandle("name", file, codeSource, manifest);
		assertEquals(file.toURI().toURL(), handle.getUrl());
		assertEquals(codeSource.toURI().toURL(), handle.getCodeSourceUrl());
		assertSame(manifest, handle.getManifest());
	}

	/**
	 * Given a {@link DirectoryResourceHandle} when constructed with certain null arguments then an
	 * {@link IllegalArgumentException} is thrown.
	 * 
	 * @throws Exception
	 */
	public void testConstructorWithIllegalArguments() throws Exception {

		try {
			new DirectoryResourceHandle("test", null, new File("test"), null);
			fail("Did not throw");
		} catch (IllegalArgumentException e) {
			assertEquals("Illegal null file specified for DirectoryResourceHandle", e.getMessage());
		}

		try {
			new DirectoryResourceHandle("test", new File("test"), null, null);
			fail("Did not throw");
		} catch (IllegalArgumentException e) {
			assertEquals("Illegal null codeSource specified for DirectoryResourceHandle", e.getMessage());
		}
	}

	/**
	 * Given a {@link DirectoryResourceHandle} backed by a file when isDirectory() is called then the result is false.
	 * 
	 * @throws Exception
	 */
	public void testIsDirectoryBackedByFile() throws Exception {
		File file = File.createTempFile("tmp", "tmp");
		try {
			DirectoryResourceHandle handle = new DirectoryResourceHandle("test", file, new File("test"), null);
			assertFalse(handle.isDirectory());
		} finally {
			TstUtils.deleteTempFile(file);
		}
	}

	/**
	 * Given a {@link DirectoryResourceHandle} backed by a directory when isDirectory() is called then the result is
	 * true.
	 * 
	 * @throws Exception
	 */
	public void testIsDirectoryBackedByDirectory() throws Exception {
		File file = TstUtils.getTempFolder();
		DirectoryResourceHandle handle = new DirectoryResourceHandle("test", file, new File("test"), null);
		assertTrue(handle.isDirectory());
	}

	/**
	 * Given a {@link DirectoryResourceHandle} backed by a file when getInputStream() is called then the data is read
	 * from the file.
	 * 
	 * @throws Exception
	 */
	public void testGetInputStreamForFile() throws Exception {
		final byte[] data = new byte[] { 0, 1, 2, 3, 4 };
		TstUtils.doWithTempFile(data, new TempFileCallback() {
			public void doWithFile(File file) throws Exception {
				DirectoryResourceHandle handle = new DirectoryResourceHandle("test", file, new File("test"), null);
				byte[] read = IOUtils.toByteArray(handle.getInputStream());
				assertTrue("Data does not match", Arrays.equals(data, read));
			}
		});
	}

	/**
	 * Given a {@link DirectoryResourceHandle} backed by a directory when getInputStream() is called then an empty
	 * stream is returned.
	 * 
	 * @throws Exception
	 */
	public void testGetInputStreamForDirectory() throws Exception {
		File folder = TstUtils.getTempFolder();
		DirectoryResourceHandle handle = new DirectoryResourceHandle("test", folder, new File("test"), null);
		byte[] read = IOUtils.toByteArray(handle.getInputStream());
		assertTrue("Expected empty data set", read.length == 0);
	}

	/**
	 * Given a {@link DirectoryResourceHandle} backed by a file when getLength() is called then the correct size is
	 * returned.
	 * 
	 * @throws Exception
	 */
	public void testGetLengthForFile() throws Exception {
		final byte[] data = new byte[100];
		TstUtils.doWithTempFile(data, new TempFileCallback() {
			public void doWithFile(File file) throws Exception {
				DirectoryResourceHandle handle = new DirectoryResourceHandle("test", file, new File("test"), null);
				assertEquals("Data length does not match", 100, handle.getContentLength());
			}
		});
	}

	/**
	 * Given a {@link DirectoryResourceHandle} backed by a directory when getLength() is called then -1 is returned.
	 * 
	 * @throws Exception
	 */
	public void testGetLengthForDirectory() throws Exception {
		File folder = TstUtils.getTempFolder();
		DirectoryResourceHandle handle = new DirectoryResourceHandle("test", folder, new File("test"), null);
		assertEquals(-1, handle.getContentLength());
	}

	/**
	 * Given a {@link DirectoryResourceHandle} with a manifest when getAttributes is called then the value is obtained
	 * from the manifest.
	 * 
	 * @throws Exception
	 */
	public void testGetAttributesDelegatesToManifest() throws Exception {
		Manifest manifest = mock(Manifest.class);
		DirectoryResourceHandle handle = new DirectoryResourceHandle("name", new File("test"), new File("test"),
				manifest);
		handle.getAttributes();
		verify(manifest).getAttributes("name");
	}

	/**
	 * Given a {@link DirectoryResourceHandle} with a null manifest when getAttributes is called then the returned value
	 * is null.
	 * 
	 * @throws Exception
	 */
	public void testGetAttributesWithNullManifest() throws Exception {
		DirectoryResourceHandle handle = new DirectoryResourceHandle("name", new File("test"), new File("test"), null);
		assertNull(handle.getAttributes());
	}

	/**
	 * Given a {@link DirectoryResourceHandle} when getCertificates() is called then the returned value is null.
	 * 
	 * @throws Exception
	 */
	public void testGetCertificatesIsNull() throws Exception {
		DirectoryResourceHandle handle = new DirectoryResourceHandle("name", new File("test"), new File("test"), null);
		assertNull(handle.getCertificates());
	}
}
