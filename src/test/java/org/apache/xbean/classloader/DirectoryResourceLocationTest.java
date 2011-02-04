package org.apache.xbean.classloader;

import java.io.File;
import java.io.FileOutputStream;
import java.util.jar.Manifest;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.xbean.classloader.TstUtils.TempFileCallback;

/**
 * Test the {@link DirectoryResourceLocation}.
 * 
 * @author Phillip Webb
 */
public class DirectoryResourceLocationTest extends TestCase {

	/**
	 * Given a {@link DirectoryResourceLocation} when constructed with a null argument then an
	 * {@link IllegalArgumentException} is thrown.
	 * 
	 * @throws Exception
	 */
	public void testConstructorWithNullArgumentThrowsIllegalArgumentException() throws Exception {
		try {
			new DirectoryResourceLocation(null);
			fail("Did not throw");
		} catch (IllegalArgumentException e) {
		}
	}

	/**
	 * Given a {@link DirectoryResourceLocation} when getResourceHandle() is called for a non existing resource then
	 * null is returned.
	 * 
	 * @throws Exception
	 */
	public void testGetResourceHandleForMissingResource() throws Exception {
		File tempFile = File.createTempFile("tmp", "tmp");
		TstUtils.deleteTempFile(tempFile);
		assertFalse(tempFile.exists());
		DirectoryResourceLocation location = new DirectoryResourceLocation(tempFile.getParentFile());
		assertNull(location.getResourceHandle(tempFile.getName()));
	}

	/**
	 * Given a {@link DirectoryResourceLocation} when getResourceHandle() is called for an existing resource then a
	 * {@link DirectoryResourceHandle} is returned.
	 * 
	 * @throws Exception
	 */
	public void testGetResourceHandleForValidResource() throws Exception {
		TstUtils.doWithTempFile(new byte[] { 0, 1, 2 }, new TempFileCallback() {
			public void doWithFile(File file) throws Exception {
				DirectoryResourceLocation location = new DirectoryResourceLocation(file.getParentFile());
				ResourceHandle handle = location.getResourceHandle(file.getName());
				assertNotNull(handle);
				assertTrue(handle instanceof DirectoryResourceHandle);
				assertEquals(3, handle.getBytes().length);
			}
		});
	}

	/**
	 * Given a {@link DirectoryResourceLocation} without a manifest when getManifest() is called then null is returned.
	 * 
	 * @throws Exception
	 */
	public void testGetManifestWhenMissing() throws Exception {
		File baseDir = TstUtils.createTempDir();
		try {
			DirectoryResourceLocation location = new DirectoryResourceLocation(baseDir);
			assertNull(location.getManifest());
		} finally {
			FileUtils.deleteDirectory(baseDir);
		}
	}

	/**
	 * Given a {@link DirectoryResourceLocation} with a manifest file when getManifest() is called then the manifest is
	 * returned.
	 * 
	 * @throws Exception
	 */
	public void testGetManifestWhenValid() throws Exception {
		File baseDir = TstUtils.createTempDir();
		try {
			File metaInfDir = new File(baseDir, "META-INF");
			File manifestFile = new File(metaInfDir, "MANIFEST.MF");
			metaInfDir.mkdir();
			FileOutputStream fos = new FileOutputStream(manifestFile);
			try {
				fos.write("Manifest-Version: 1.0\n".getBytes());
				fos.write("Class-Path: test.jar\n".getBytes());

			} finally {
				fos.close();
			}
			DirectoryResourceLocation location = new DirectoryResourceLocation(baseDir);
			Manifest manifest = location.getManifest();
			assertNotNull(manifest);
			System.out.println(manifest.getEntries().keySet());
			assertEquals("test.jar", manifest.getMainAttributes().getValue("Class-Path"));
		} finally {
			FileUtils.deleteDirectory(baseDir);
		}
	}
}
