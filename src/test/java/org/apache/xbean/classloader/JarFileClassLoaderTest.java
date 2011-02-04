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

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.File;
import java.net.URL;
import java.security.Permission;
import java.util.jar.Manifest;

/**
 * Test the {@link JarFileClassLoader}.
 * 
 * @author Dain Sundstrom
 * @author Phillip Webb
 */
public class JarFileClassLoaderTest extends MultiParentClassLoaderTest {

	protected MultiParentClassLoader createClassLoader(String name, URL[] urls, ClassLoader[] parents) {
		return new JarFileClassLoader(name, urls, parents);
	}

	/**
	 * Given a {@link JarFileClassLoader} when getURLs() is called then the method should delegate to the finder.
	 * @throws Exception
	 */
	public void testGetUrlsDelegatesToResourceFinder() throws Exception {
		UrlResourceFinder finder = mock(UrlResourceFinder.class);
		JarFileClassLoader classLoader = new MockJarFileClassLoader("test", TstUtils.EMPTY_URLS, finder);
		classLoader.getURLs();
		verify(finder).getUrls();
	}

	/**
	 * Given a {@link JarFileClassLoader} when addURL() is called then the method should delegate to the finder.
	 * @throws Exception
	 */
	public void testAddUrlDelegatesToResourceFinder() throws Exception {
		URL url = new URL("http:///test");
		UrlResourceFinder finder = mock(UrlResourceFinder.class);
		JarFileClassLoader classLoader = new MockJarFileClassLoader("test", TstUtils.EMPTY_URLS, finder);
		classLoader.addURL(url);
		verify(finder).addUrl(url);
	}

	/**
	 * Given a {@link JarFileClassLoader} when destroy() is called then the method should also call the finder.
	 * @throws Exception
	 */
	public void testDestroyCallsResourceFinder() throws Exception {
		UrlResourceFinder finder = mock(UrlResourceFinder.class);
		JarFileClassLoader classLoader = new MockJarFileClassLoader("test", TstUtils.EMPTY_URLS, finder);
		classLoader.destroy();
		verify(finder).destroy();
	}

	/**
	 * Given a {@link JarFileClassLoader} when findLibrary is called with a string that ends with "/" then an exception
	 * is thrown.
	 * @throws Exception
	 */
	public void testFindLibraryForDirectoryThrows() throws Exception {
		JarFileClassLoader classLoader = new JarFileClassLoader("test", TstUtils.EMPTY_URLS);
		try {
			classLoader.findLibrary("sample/ends/with/slash/");
			fail("Did not throw");
		} catch (IllegalArgumentException e) {
			assertEquals("libraryName ends with a '/' character: sample/ends/with/slash/", e.getMessage());
		}
	}

	/**
	 * Given a {@link JarFileClassLoader} when findLibrary is called with a library in a path then a suitable result is
	 * returned.
	 * 
	 * @throws Exception
	 */
	public void testFindLibraryWithPath() throws Exception {
		URL url = new URL("file:///path/sample-lib.so");
		UrlResourceFinder finder = mock(UrlResourceFinder.class);
		JarFileClassLoader classLoader = new MockJarFileClassLoader("test", TstUtils.EMPTY_URLS, finder);
		ResourceHandle resourceHandle = mock(ResourceHandle.class);
		when(finder.getResource("/path/" + System.mapLibraryName("sample-lib"))).thenReturn(resourceHandle);
		when(resourceHandle.getUrl()).thenReturn(url);
		String library = classLoader.findLibrary("/path/sample-lib");
		assertEquals(File.separator + "path" + File.separator + "sample-lib.so", library);
	}

	/**
	 * Given a {@link JarFileClassLoader} when findLibrary is called with a library not in a path then a suitable result
	 * is returned.
	 * 
	 * @throws Exception
	 */
	public void testFindLibraryWithoutPath() throws Exception {
		URL url = new URL("file:///sample-lib.so");
		UrlResourceFinder finder = mock(UrlResourceFinder.class);
		JarFileClassLoader classLoader = new MockJarFileClassLoader("test", TstUtils.EMPTY_URLS, finder);
		ResourceHandle resourceHandle = mock(ResourceHandle.class);
		when(finder.getResource(System.mapLibraryName("sample-lib"))).thenReturn(resourceHandle);
		when(resourceHandle.getUrl()).thenReturn(url);
		String library = classLoader.findLibrary("sample-lib");
		assertEquals(File.separator + "sample-lib.so", library);
	}

	/**
	 * Given a {@link JarFileClassLoader} when findLibrary is called with a library that does not exist then null is
	 * returned.
	 * 
	 * @throws Exception
	 */
	public void testFindLibraryNotFound() throws Exception {
		UrlResourceFinder finder = mock(UrlResourceFinder.class);
		JarFileClassLoader classLoader = new MockJarFileClassLoader("test", TstUtils.EMPTY_URLS, finder);
		String library = classLoader.findLibrary("sample-lib");
		verify(finder).getResource(System.mapLibraryName("sample-lib"));
		assertNull(library);
	}

	/**
	 * Given a {@link JarFileClassLoader} when findLibrary is called with a library that does not map to a local file
	 * then null is returned.
	 * 
	 * @throws Exception
	 */
	public void testFindLibraryNotFile() throws Exception {
		URL url = new URL("http:///sample-lib.so");
		UrlResourceFinder finder = mock(UrlResourceFinder.class);
		JarFileClassLoader classLoader = new MockJarFileClassLoader("test", TstUtils.EMPTY_URLS, finder);
		ResourceHandle resourceHandle = mock(ResourceHandle.class);
		when(finder.getResource(System.mapLibraryName("sample-lib"))).thenReturn(resourceHandle);
		when(resourceHandle.getUrl()).thenReturn(url);
		String library = classLoader.findLibrary("sample-lib");
		assertNull(library);
	}

	/**
	 * Given a {@link JarFileClassLoader} when findClass is called on a class in a package and a {@link SecurityManager}
	 * is installed then the manager should check the package definition.
	 * 
	 * @throws Exception
	 */
	public void testFindClassCallsSecurityManager() throws Exception {
		MockSecurityManager securityManager = new MockSecurityManager();
		System.setSecurityManager(securityManager);
		try {
			UrlResourceFinder finder = mock(UrlResourceFinder.class);
			JarFileClassLoader classLoader = new MockJarFileClassLoader("test", TstUtils.EMPTY_URLS, finder);
			try {
				classLoader.findClass("javax.sample.SomeClass");
			} catch (ClassNotFoundException e) {
				// Expected
			}
			assertEquals("javax.sample", securityManager.getCheckedPackageDefinition());
		} finally {
			System.setSecurityManager(null);
		}
	}

	/**
	 * Given a {@link JarFileClassLoader} when findClass is called on a class in a package then the package is defined
	 * with attributes loaded from the manifest.
	 * 
	 * @throws Exception
	 */
	public void testDefinePackagePicksUpManifest() throws Exception {
		UrlResourceFinder finder = mock(UrlResourceFinder.class);
		MockJarFileClassLoader classLoader = new MockJarFileClassLoader("test", TstUtils.EMPTY_URLS, finder);
		assertNull(classLoader.getPackage("javax.sample"));
		ResourceHandle resourceHandle = mock(ResourceHandle.class);
		when(finder.getResource("javax/sample/SomeClass.class")).thenReturn(resourceHandle);
		when(resourceHandle.getBytes()).thenReturn(new byte[] {});
		when(resourceHandle.getCodeSourceUrl()).thenReturn(new URL("file:///"));
		Manifest manifest = new Manifest(getClass().getResourceAsStream("JarFileClassLoaderTest_package.mf"));
		when(resourceHandle.getManifest()).thenReturn(manifest);
		try {
			classLoader.findClass("javax.sample.SomeClass");
		} catch (ClassFormatError e) {
			// Expected
		}
		Package samplePackage = classLoader.getPackage("javax.sample");
		assertNotNull(samplePackage);
		assertEquals("Sample Specification Title", samplePackage.getSpecificationTitle());
		assertEquals("Sample Specification Vendor", samplePackage.getSpecificationVendor());
		assertEquals("1.0", samplePackage.getSpecificationVersion());
		assertEquals("javax.sample", samplePackage.getImplementationTitle());
		assertEquals("Sample Implementation Vendor", samplePackage.getImplementationVendor());
		assertEquals("build10", samplePackage.getImplementationVersion());
	}

	private static class MockJarFileClassLoader extends JarFileClassLoader {

		public MockJarFileClassLoader(String name, URL[] urls, UrlResourceFinder resourceFinder) {
			super(name, urls);
			setResourceFinder(resourceFinder);
			addURLs(urls);
		}

		protected boolean addUrlsOnInitialize() {
			return false;
		}

		public Package getPackage(String name) {
			return super.getPackage(name);
		}
	}

	private static class MockSecurityManager extends SecurityManager {
		private String checkedPackageDefinition;

		public void checkPermission(Permission perm) {
		}

		public void checkPackageDefinition(String pkg) {
			this.checkedPackageDefinition = pkg;
		}

		public String getCheckedPackageDefinition() {
			return checkedPackageDefinition;
		}
	}

}
