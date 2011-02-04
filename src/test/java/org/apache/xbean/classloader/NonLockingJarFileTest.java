package org.apache.xbean.classloader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import junit.framework.TestCase;

import org.apache.commons.io.IOUtils;

/**
 * Test the {@link NonLockingJarFile}.
 * 
 * @author Phillip Webb
 */
public class NonLockingJarFileTest extends TestCase {

	public static final String SAMPLE_JAR_CLASS = TstUtils.SAMPLE_JAR_CLASS;

	private File file;

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#setUp()
	 */
	protected void setUp() throws Exception {
		super.setUp();
		file = TstUtils.createTempJarFile();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see junit.framework.TestCase#tearDown()
	 */
	protected void tearDown() throws Exception {
		TstUtils.deleteTempFile(file);
		super.tearDown();
	}

	/**
	 * Sanity check that <tt>NonLockingJarFileSpy</tt> tracks the open and close of underlying files.
	 * 
	 * @throws Exception
	 */
	public void testSanityCheck() throws Exception {
		NonLockingJarFileSpy jarFile = new NonLockingJarFileSpy(file);
		JarFile underling = jarFile.reopenJarFile();
		assertFalse(jarFile.isClosed());
		underling.close();
		assertTrue(jarFile.isClosed());
	}

	/**
	 * Given a {@link NonLockingJarFile} when constructed then the underlying file is closed.
	 * @throws Exception
	 */
	public void testConstructorClosesUnderlyingStream() throws Exception {
		NonLockingJarFileSpy jarFile = new NonLockingJarFileSpy(file, false, ZipFile.OPEN_READ);
		assertTrue("Newly created non locking jar file did not close underlying file", jarFile.isClosed());
	}

	/**
	 * Given a {@link NonLockingJarFile} is being constructed when the various constructors are called sensible defaults
	 * are applied.
	 * @throws Exception
	 */
	public void testConstructorDefaults() throws Exception {
		NonLockingJarFileSpy jarFile;

		jarFile = new NonLockingJarFileSpy(file.getPath());
		assertEquals(file.getPath(), jarFile.getName());
		assertEquals(true, jarFile.isVerify());
		assertEquals(ZipFile.OPEN_READ, jarFile.getMode());

		jarFile = new NonLockingJarFileSpy(file.getPath(), false);
		assertEquals(file.getPath(), jarFile.getName());
		assertEquals(false, jarFile.isVerify());
		assertEquals(ZipFile.OPEN_READ, jarFile.getMode());

		jarFile = new NonLockingJarFileSpy(file);
		assertEquals(file.getPath(), jarFile.getName());
		assertEquals(true, jarFile.isVerify());
		assertEquals(ZipFile.OPEN_READ, jarFile.getMode());

		jarFile = new NonLockingJarFileSpy(file, false);
		assertEquals(file.getPath(), jarFile.getName());
		assertEquals(false, jarFile.isVerify());
		assertEquals(ZipFile.OPEN_READ, jarFile.getMode());

		jarFile = new NonLockingJarFileSpy(file, false, ZipFile.OPEN_READ | ZipFile.OPEN_DELETE);
		assertEquals(file.getPath(), jarFile.getName());
		assertEquals(false, jarFile.isVerify());
		assertEquals(ZipFile.OPEN_READ | ZipFile.OPEN_DELETE, jarFile.getMode());
	}

	/**
	 * Given a {@link NonLockingJarFile} when getEntry() is called then the entry is returned and the underlying jar
	 * file is closed.
	 * 
	 * @throws Exception
	 */
	public void testGetEntryDoesNotLockFile() throws Exception {
		NonLockingJarFileSpy jarFile = new NonLockingJarFileSpy(file);
		ZipEntry entry = jarFile.getEntry(SAMPLE_JAR_CLASS);
		assertEquals(SAMPLE_JAR_CLASS, entry.getName());
		assertTrue(jarFile.isClosed());
	}

	/**
	 * Given a {@link NonLockingJarFile} when getEntry() is called the entry returned should be a {@link JarEntry}
	 * 
	 * @throws Exception
	 */
	public void testGetEntryUsesGetJarEntry() throws Exception {
		NonLockingJarFileSpy jarFile = new NonLockingJarFileSpy(file);
		ZipEntry entry = jarFile.getEntry(SAMPLE_JAR_CLASS);
		assertTrue("Entry should be a Jar Entry", entry instanceof JarEntry);
	}

	/**
	 * Given a {@link NonLockingJarFile} when getEntry() is called then the entry is returned and the underlying jar
	 * file is closed.
	 * 
	 * @throws Exception
	 */
	public void testGetJarEntryDoesNotLockFile() throws Exception {
		NonLockingJarFileSpy jarFile = new NonLockingJarFileSpy(file);
		JarEntry entry = jarFile.getJarEntry(SAMPLE_JAR_CLASS);
		assertEquals(SAMPLE_JAR_CLASS, entry.getName());
		assertTrue(jarFile.isClosed());
	}

	/**
	 * Given a {@link NonLockingJarFile} when entries() is called the then the enumeration is returned and the
	 * underlying jar file is closed.
	 * 
	 * @throws Exception
	 */
	public void testEntriesDoesNotLockFile() throws Exception {
		NonLockingJarFileSpy jarFile = new NonLockingJarFileSpy(file);
		Enumeration<JarEntry> entries = jarFile.entries();
		assertTrue(jarFile.isClosed());
		assertTrue(entries.hasMoreElements());
	}

	private List<String> getEntryNames(Enumeration<JarEntry> entries) {
		List<String> jarEntryNames = new ArrayList<String>();
		while (entries.hasMoreElements()) {
			jarEntryNames.add(entries.nextElement().getName());
		}
		return jarEntryNames;
	}

	/**
	 * Given a {@link NonLockingJarFile} when entries() is called then the enumeration should contained the same values
	 * as an equivalent {@link JarFile}.
	 * 
	 * @throws Exception
	 */
	public void testEntriesReturnsValidEnumeration() throws Exception {
		// Get entry names for the standard implementation
		List<String> defaultJarEntryNames;
		JarFile defaultJarFile = new JarFile(file);
		try {
			defaultJarEntryNames = getEntryNames(defaultJarFile.entries());
		} finally {
			defaultJarFile.close();
		}

		// Do the same for the non-locking version
		NonLockingJarFileSpy jarFile = new NonLockingJarFileSpy(file);
		List<String> nonLockingJarEntryNames = getEntryNames(jarFile.entries());

		assertEquals("Expected same names for nonlocking/default jar files", defaultJarEntryNames,
				nonLockingJarEntryNames);
	}

	/**
	 * Given a {@link NonLockingJarFile} when getInputStream is called with a null entry then an
	 * {@link IllegalArgumentException} is thrown.
	 * 
	 * @throws Exception
	 */
	public void testGetInputStreamThrowsIllegalArgumentExceptionOnNullEntry() throws Exception {
		NonLockingJarFile jarFile = new NonLockingJarFile(file);
		try {
			jarFile.getInputStream(null);
			fail("Did not throw");
		} catch (IllegalArgumentException e) {
			assertEquals("The zip entry is required", e.getMessage());
		}
	}

	/**
	 * Given a {@link NonLockingJarFile} when getInputStream is called with a entry that references an item not in the
	 * zip file then an {@link IOException} is thrown.
	 * 
	 * @throws Exception
	 */
	public void testGetInputStreamFromMadeUpEntryThrowsIOException() throws Exception {
		NonLockingJarFile jarFile = new NonLockingJarFile(file);
		try {
			jarFile.getInputStream(new ZipEntry("madeup"));
			fail("Did not throw");
		} catch (IOException e) {
			assertEquals("Unable to locate JAR entry with name madeup", e.getMessage());
		}
	}

	/**
	 * Given a {@link NonLockingJarFile} when getInputStream() is called and the resulting stream is not closed then the
	 * jar file is closed.
	 * 
	 * @throws Exception
	 */
	public void testGetInputStreamClosesJarFileEventIfStreamIsNotClosed() throws Exception {
		NonLockingJarFileSpy jarFile = new NonLockingJarFileSpy(file);
		InputStream inputStream = jarFile.getInputStream(jarFile.getEntry(SAMPLE_JAR_CLASS));
		assertTrue(jarFile.isClosed());
		inputStream.close();
	}

	/**
	 * Given a {@link NonLockingJarFile} when getInputStream() is called then the resulting data is identical to the
	 * equivalent {@link JarFile} call.
	 * 
	 * @throws Exception
	 */
	public void testGetInputStreamReturnsValidData() throws Exception {
		byte[] defaultContents;
		JarFile defaultJarFile = new JarFile(file);
		try {
			InputStream inputStream = defaultJarFile.getInputStream(defaultJarFile.getEntry(SAMPLE_JAR_CLASS));
			try {
				defaultContents = IOUtils.toByteArray(inputStream);
			} finally {
				inputStream.close();
			}
		} finally {
			defaultJarFile.close();
		}

		NonLockingJarFileSpy jarFile = new NonLockingJarFileSpy(file);
		byte[] nonLockedContents = IOUtils.toByteArray(jarFile.getInputStream(jarFile.getEntry(SAMPLE_JAR_CLASS)));

		assertTrue("Expected same byte contents for input stream", Arrays.equals(defaultContents, nonLockedContents));
	}

	/**
	 * Given a {@link NonLockingJarFile} when getManifest() is called then the jar file is closed.
	 * 
	 * @throws Exception
	 */
	public void testGetManifestDoesNotLockFile() throws Exception {
		NonLockingJarFileSpy jarFile = new NonLockingJarFileSpy(file);
		Manifest manifest = jarFile.getManifest();
		assertTrue(jarFile.isClosed());
		assertNotNull(manifest);
	}

	/**
	 * Given a {@link NonLockingJarFile} when getManifest() is called the manifest is identical to the equivalent call
	 * on {@link JarFile}.
	 * 
	 * @throws Exception
	 */
	public void testGetManifestReturnsValidData() throws Exception {
		NonLockingJarFileSpy jarFile = new NonLockingJarFileSpy(file);
		Manifest manifest = jarFile.getManifest();
		JarFile defaultJarFile = new JarFile(file);
		try {
			Manifest defaultManifest = defaultJarFile.getManifest();
			assertEquals(defaultManifest, manifest);
		} finally {
			defaultJarFile.close();
		}
	}

	/**
	 * Version of {@link NonLockingJarFileSpy} that spys on certain operations so that we can assert state in various
	 * tests.
	 * 
	 */
	private static class NonLockingJarFileSpy extends NonLockingJarFile {

		// Note: do not use primitive here as we need to detect if close is called during super constructor, if
		// primitive is used the value is reset to false after the super constructor is called.
		private Boolean closed;

		private boolean verify;
		private int mode;

		public NonLockingJarFileSpy(String name) throws IOException {
			super(name);
		}

		public NonLockingJarFileSpy(String name, boolean verify) throws IOException {
			super(name, verify);
		}

		public NonLockingJarFileSpy(File file) throws IOException {
			super(file);
		}

		public NonLockingJarFileSpy(File file, boolean verify) throws IOException {
			super(file, verify);
		}

		public NonLockingJarFileSpy(File file, boolean verify, int mode) throws IOException {
			super(file, verify, mode);
		}

		protected void initialize(File file, boolean verify, int mode) throws IOException {
			this.verify = verify;
			this.mode = mode;
			super.initialize(file, verify, mode);
		}

		protected JarFile reopenJarFile() throws IOException {
			this.closed = null;
			return new ReopenedJarFile(getFile(), false, ZipFile.OPEN_READ);
		}

		public void close() throws IOException {
			this.closed = new Boolean(true);
			super.close();
		}

		public boolean isClosed() {
			return (closed == null ? false : closed.booleanValue());
		}

		public boolean isVerify() {
			return verify;
		}

		public int getMode() {
			return mode;
		}

		private class ReopenedJarFile extends JarFile {
			public ReopenedJarFile(File file, boolean verify, int mode) throws IOException {
				super(file, verify, mode);
			}

			public void close() throws IOException {
				super.close();
				NonLockingJarFileSpy.this.closed = new Boolean(true);
			}
		}
	}
}
