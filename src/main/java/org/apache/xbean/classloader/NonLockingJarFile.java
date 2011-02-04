/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.xbean.classloader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.net.URLClassLoader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A {@link JarFile} implementation that does not lock the underlying JAR file as entries are accessed. This
 * implementation can be used to overcome the file locking issues that occur when using {@link URLClassLoader}s on
 * Microsoft Windows.
 * 
 * @author Phillip Webb
 */
public class NonLockingJarFile extends JarFile {

	private static final JarEntry NO_MANIFEST = new JarEntry(NonLockingJarFile.class.getName() + ".NO_MANIFEST");

	private File file;

	private Long previousLastModified;

	private SoftReference<Map<String, CachedJarEntry>> jarEntryCache;

	private JarEntry manifestJarEntry;

	private SoftReference<Manifest> manifestRef;

	/**
	 * Creates a new <code>NonLockingJarFile</code> to read from the specified file <code>name</code>. The
	 * <code>NonLockingJarFile</code> will be verified if it is signed.
	 * @param name the name of the jar file to be opened for reading
	 * @throws IOException if an I/O error has occurred
	 * @throws SecurityException if access to the file is denied by the SecurityManager
	 */
	public NonLockingJarFile(String name) throws IOException {
		this(new File(name), true, ZipFile.OPEN_READ);
	}

	/**
	 * Creates a new <code>NonLockingJarFile</code> to read from the specified file <code>name</code>.
	 * @param name the name of the jar file to be opened for reading
	 * @param verify whether or not to verify the jar file if it is signed.
	 * @throws IOException if an I/O error has occurred
	 * @throws SecurityException if access to the file is denied by the SecurityManager
	 */
	public NonLockingJarFile(String name, boolean verify) throws IOException {
		this(new File(name), verify, ZipFile.OPEN_READ);
	}

	/**
	 * Creates a new <code>NonLockingJarFile</code> to read from the specified <code>File</code> object. The
	 * <code>NonLockingJarFile</code> will be verified if it is signed.
	 * @param file the jar file to be opened for reading
	 * @throws IOException if an I/O error has occurred
	 * @throws SecurityException if access to the file is denied by the SecurityManager
	 */
	public NonLockingJarFile(File file) throws IOException {
		this(file, true, ZipFile.OPEN_READ);
	}

	/**
	 * Creates a new <code>NonLockingJarFile</code> to read from the specified <code>File</code> object.
	 * @param file the jar file to be opened for reading
	 * @param verify whether or not to verify the jar file if it is signed.
	 * @throws IOException if an I/O error has occurred
	 * @throws SecurityException if access to the file is denied by the SecurityManager.
	 */
	public NonLockingJarFile(File file, boolean verify) throws IOException {
		this(file, verify, ZipFile.OPEN_READ);
	}

	/**
	 * Creates a new <code>NonLockingJarFile</code> to read from the specified <code>File</code> object in the specified
	 * mode. The mode argument must be either <tt>OPEN_READ</tt> or <tt>OPEN_READ | OPEN_DELETE</tt>.
	 * @param file the jar file to be opened for reading
	 * @param verify whether or not to verify the jar file if it is signed.
	 * @param mode the mode in which the file is to be opened
	 * @throws IOException if an I/O error has occurred
	 * @throws IllegalArgumentException if the <tt>mode</tt> argument is invalid
	 * @throws SecurityException if access to the file is denied by the SecurityManager
	 */
	public NonLockingJarFile(File file, boolean verify, int mode) throws IOException {
		super(file, verify, mode);
		initialize(file, verify, mode);
	}

	/**
	 * Initialize method called from all constructors to initialize the class.
	 * @param file the jar file to be opened for reading
	 * @param verify whether or not to verify the jar file if it is signed.
	 * @param mode the mode in which the file is to be opened
	 * @throws IOException if an I/O error has occurred
	 */
	protected void initialize(File file, boolean verify, int mode) throws IOException {
		this.file = file;
		close();
	}

	/**
	 * Returns the underling file that this JAR file is manipulating.
	 * @return The underlying file
	 */
	protected final File getFile() {
		return file;
	}

	/**
	 * Internal method that is used to reopen the underlying {@link JarFile}.
	 * @return A newly opened jar file
	 * @throws IOException
	 */
	protected JarFile reopenJarFile() throws IOException {
		return new JarFile(file, false, ZipFile.OPEN_READ);
	}

	private void clearJarEntryCacheIfFileHasChanged() {
		long lastModified = file.lastModified();
		if (previousLastModified == null || previousLastModified.longValue() != lastModified) {
			previousLastModified = new Long(lastModified);
			jarEntryCache = null;
			manifestJarEntry = null;
			manifestRef = null;
		}
	}

	private Map<String, CachedJarEntry> getJarEntryCache() {
		clearJarEntryCacheIfFileHasChanged();
		Map<String, CachedJarEntry> rtn = (jarEntryCache == null ? null : jarEntryCache.get());
		if (rtn == null) {
			try {
				JarFile jarFile = reopenJarFile();
				try {
					rtn = new LinkedHashMap<String, CachedJarEntry>();
					Enumeration<JarEntry> entries = jarFile.entries();
					while (entries.hasMoreElements()) {
						JarEntry entry = entries.nextElement();
						rtn.put(entry.getName(), new CachedJarEntry(entry));
					}
					jarEntryCache = new SoftReference<Map<String, CachedJarEntry>>(rtn);
				} finally {
					jarFile.close();
				}
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}
		return rtn;
	}

	private byte[] getZipEntryBytes(ZipEntry ze) throws IOException {
		JarFile jarFile = reopenJarFile();
		try {
			return getBytes(jarFile.getInputStream(ze));
		} finally {
			jarFile.close();
		}
	}

	private static byte[] getBytes(InputStream input) throws IOException {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024 * 4];
			int n = 0;
			while (-1 != (n = input.read(buffer))) {
				output.write(buffer, 0, n);
			}
			return output.toByteArray();
		} finally {
			try {
				input.close();
			} catch (IOException e) {
			}
		}
	}

	@Override
	public ZipEntry getEntry(String name) {
		return getJarEntry(name);
	}

	@Override
	public JarEntry getJarEntry(String name) {
		CachedJarEntry cachedJarEntry = getJarEntryCache().get(name);
		return cachedJarEntry == null ? null : cachedJarEntry.getJarEntry();
	}

	@Override
	public Enumeration<JarEntry> entries() {

		final Enumeration<CachedJarEntry> cacheEnumeration = Collections.enumeration(getJarEntryCache().values());

		return new Enumeration<JarEntry>() {

			public boolean hasMoreElements() {
				return cacheEnumeration.hasMoreElements();
			}

			public JarEntry nextElement() {
				return cacheEnumeration.nextElement().getJarEntry();
			}
		};
	}

	@Override
	public synchronized InputStream getInputStream(ZipEntry ze) throws IOException {
		if (ze == null) {
			throw new IllegalArgumentException("The zip entry is required");
		}
		CachedJarEntry cacheEntry = getJarEntryCache().get(ze.getName());
		if (cacheEntry == null) {
			throw new IOException("Unable to locate JAR entry with name " + ze.getName());
		}
		return cacheEntry.getInputStream(this);
	}

	private JarEntry getManifestEntry() {
		if (manifestJarEntry == null) {
			manifestJarEntry = getJarEntry(MANIFEST_NAME);
			if (manifestJarEntry == null) {
				for (CachedJarEntry cachedJarEntry : getJarEntryCache().values()) {
					if (MANIFEST_NAME.equals(cachedJarEntry.getJarEntry().getName().toUpperCase(Locale.ENGLISH))) {
						manifestJarEntry = cachedJarEntry.getJarEntry();
						break;
					}
				}
			}
			if (manifestJarEntry == null) {
				manifestJarEntry = NO_MANIFEST;
			}
		}
		return (manifestJarEntry == NO_MANIFEST ? null : manifestJarEntry);
	}

	public Manifest getManifest() throws IOException {
		clearJarEntryCacheIfFileHasChanged();
		Manifest manifest = (manifestRef != null ? manifestRef.get() : null);
		if (manifest == null) {
			JarEntry manEntry = getManifestEntry();
			if (manEntry != null) {
				manifest = new Manifest(getInputStream(manEntry));
			}
			manifestRef = new SoftReference<Manifest>(manifest);
		}
		return manifest;
	}

	private static class CachedJarEntry {
		private JarEntry jarEntry;

		private byte[] inputStreamData;

		public CachedJarEntry(JarEntry jarEntry) {
			this.jarEntry = jarEntry;
		}

		public JarEntry getJarEntry() {
			return jarEntry;
		}

		public InputStream getInputStream(NonLockingJarFile file) throws IOException {
			if (inputStreamData == null) {
				inputStreamData = file.getZipEntryBytes(jarEntry);
			}
			return new ByteArrayInputStream(inputStreamData);
		}
	}
}
