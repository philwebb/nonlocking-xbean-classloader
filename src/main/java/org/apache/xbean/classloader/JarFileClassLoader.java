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

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.CodeSource;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.cert.Certificate;
import java.util.Collection;
import java.util.Enumeration;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

/**
 * The JarFileClassLoader that loads classes and resources from a list of JarFiles. This method is similar to
 * URLClassLoader except it properly closes JarFiles when the classloader is destroyed so that the file read lock will
 * be released, and the jar file can be modified and deleted.
 * 
 * @author Dain Sundstrom
 * 
 * @see NonLockingJarFile
 */
public class JarFileClassLoader extends MultiParentClassLoader {

	private static final URL[] EMPTY_URLS = new URL[0];

	private UrlResourceFinder resourceFinder;
	private AccessControlContext accessControlContext;

	/**
	 * Creates a JarFileClassLoader that is a child of the system class loader.
	 * @param name the name of this class loader
	 * @param urls a list of URLs from which classes and resources should be loaded
	 */
	public JarFileClassLoader(String name, URL[] urls) {
		super(name, EMPTY_URLS);
		initialize(urls);
	}

	/**
	 * Creates a JarFileClassLoader that is a child of the specified class loader.
	 * @param name the name of this class loader
	 * @param urls a list of URLs from which classes and resources should be loaded
	 * @param parent the parent of this class loader
	 */
	public JarFileClassLoader(String name, URL[] urls, ClassLoader parent) {
		super(name, EMPTY_URLS, parent);
		initialize(urls);
	}

	/**
	 * Creates a JarFileClassLoader that is a child of the specified class loader.
	 * @param name
	 * @param urls
	 * @param parent
	 * @param inverseClassLoading
	 * @param hiddenClasses
	 * @param nonOverridableClasses
	 */
	public JarFileClassLoader(String name, URL[] urls, ClassLoader parent, boolean inverseClassLoading,
			String[] hiddenClasses, String[] nonOverridableClasses) {
		super(name, EMPTY_URLS, parent, inverseClassLoading, hiddenClasses, nonOverridableClasses);
		initialize(urls);
	}

	/**
	 * Creates a named class loader as a child of the specified parents.
	 * @param name the name of this class loader
	 * @param urls the urls from which this class loader will classes and resources
	 * @param parents the parents of this class loader
	 */
	public JarFileClassLoader(String name, URL[] urls, ClassLoader[] parents) {
		super(name, EMPTY_URLS, parents);
		initialize(urls);
	}

	/**
	 * Creates a JarFileClassLoader that is a child of the specified class loaders.
	 * @param name
	 * @param urls
	 * @param parents
	 * @param inverseClassLoading
	 * @param hiddenClasses
	 * @param nonOverridableClasses
	 */
	public JarFileClassLoader(String name, URL[] urls, ClassLoader[] parents, boolean inverseClassLoading,
			Collection<String> hiddenClasses, Collection<String> nonOverridableClasses) {
		super(name, EMPTY_URLS, parents, inverseClassLoading, hiddenClasses, nonOverridableClasses);
		initialize(urls);
	}

	/**
	 * Creates a JarFileClassLoader that is a child of the specified class loaders.
	 * @param name
	 * @param urls
	 * @param parents
	 * @param inverseClassLoading
	 * @param hiddenClasses
	 * @param nonOverridableClasses
	 */
	public JarFileClassLoader(String name, URL[] urls, ClassLoader[] parents, boolean inverseClassLoading,
			String[] hiddenClasses, String[] nonOverridableClasses) {
		super(name, EMPTY_URLS, parents, inverseClassLoading, hiddenClasses, nonOverridableClasses);
		initialize(urls);
	}

	/**
	 * Determine if URLs should be added during {@link #initialize(URL[])}. The default implementation returns
	 * <tt>true</tt>, subclasses can override if they need to perform additional setups before {@link #addURLs(URL[])}
	 * is called.
	 * 
	 * @return true if URLs should be added on {@link #initialize(URL[])}
	 */
	protected boolean addUrlsOnInitialize() {
		return true;
	}

	/**
	 * Initialize method called from all constructors.
	 * 
	 * @param urls
	 */
	protected void initialize(URL[] urls) {
		this.accessControlContext = AccessController.getContext();
		setResourceFinder(newResourceFinder());
		if (addUrlsOnInitialize()) {
			addURLs(urls);
		}
	}

	/**
	 * Factory method used to create the resource finder used with this class loader. The default implementation returns
	 * a {@link UrlResourceFinder}.
	 * 
	 * @return A {@link UrlResourceFinder}.
	 * @see #setResourceFinder(UrlResourceFinder)
	 */
	protected UrlResourceFinder newResourceFinder() {
		return new UrlResourceFinder(null);
	}

	/**
	 * Set the resource finder that will be used to perform all resource operations. Generally this method will not be
	 * called directly, instead the {@link #newResourceFinder()} factory method should be overridden. This method is
	 * primarily intended for test cases to use.
	 * 
	 * @param resourceFinder
	 */
	protected final void setResourceFinder(UrlResourceFinder resourceFinder) {
		this.resourceFinder = resourceFinder;
	}

	/**
	 * {@inheritDoc}
	 */
	public URL[] getURLs() {
		return resourceFinder.getUrls();
	}

	/**
	 * {@inheritDoc}
	 */
	public void addURL(final URL url) {
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run() {
				resourceFinder.addUrl(url);
				return null;
			}
		}, accessControlContext);
	}

	/**
	 * Adds an array of urls to the end of this class loader.
	 * @param urls the URLs to add
	 */
	protected void addURLs(final URL[] urls) {
		AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run() {
				resourceFinder.addUrls(urls);
				return null;
			}
		}, accessControlContext);
	}

	/**
	 * {@inheritDoc}
	 */
	public void destroy() {
		resourceFinder.destroy();
		super.destroy();
	}

	/**
	 * {@inheritDoc}
	 */
	public URL findResource(final String resourceName) {
		return (URL) AccessController.doPrivileged(new PrivilegedAction<Object>() {
			public Object run() {
				return resourceFinder.findResource(resourceName);
			}
		}, accessControlContext);
	}

	/**
	 * {@inheritDoc}
	 */
	public Enumeration<URL> findResources(final String resourceName) throws IOException {
		// first get the resources from the parent classloaders
		Enumeration<URL> parentResources = super.findResources(resourceName);

		// get the classes from my urls
		Enumeration<URL> myResources = AccessController.doPrivileged(new PrivilegedAction<Enumeration<URL>>() {
			public Enumeration<URL> run() {
				return resourceFinder.findResources(resourceName);
			}
		}, accessControlContext);

		// join the two together
		Enumeration<URL> resources = new UnionEnumeration<URL>(parentResources, myResources);
		return resources;
	}

	/**
	 * {@inheritDoc}
	 */
	protected String findLibrary(String libraryName) {
		// if the libraryName is actually a directory it is invalid
		int pathEnd = libraryName.lastIndexOf('/');
		if (pathEnd == libraryName.length() - 1) {
			throw new IllegalArgumentException("libraryName ends with a '/' character: " + libraryName);
		}

		// get the name if the library file
		final String resourceName;
		if (pathEnd < 0) {
			resourceName = System.mapLibraryName(libraryName);
		} else {
			String path = libraryName.substring(0, pathEnd + 1);
			String file = libraryName.substring(pathEnd + 1);
			resourceName = path + System.mapLibraryName(file);
		}

		// get a resource handle to the library
		ResourceHandle resourceHandle = AccessController.doPrivileged(new PrivilegedAction<ResourceHandle>() {
			public ResourceHandle run() {
				return resourceFinder.getResource(resourceName);
			}
		}, accessControlContext);

		if (resourceHandle == null) {
			return null;
		}

		// the library must be accessible on the file system
		URL url = resourceHandle.getUrl();
		if (!"file".equals(url.getProtocol())) {
			return null;
		}

		String path = new File(URI.create(url.toString())).getPath();
		return path;
	}

	/**
	 * {@inheritDoc}
	 */
	protected Class<?> findClass(final String className) throws ClassNotFoundException {
		try {
			return AccessController.doPrivileged(new PrivilegedExceptionAction<Class<?>>() {
				public Class<?> run() throws ClassNotFoundException {
					// first think check if we are allowed to define the package
					SecurityManager securityManager = System.getSecurityManager();
					if (securityManager != null) {
						String packageName;
						int packageEnd = className.lastIndexOf('.');
						if (packageEnd >= 0) {
							packageName = className.substring(0, packageEnd);
							securityManager.checkPackageDefinition(packageName);
						}
					}

					// convert the class name to a file name
					String resourceName = className.replace('.', '/') + ".class";

					// find the class file resource
					ResourceHandle resourceHandle = resourceFinder.getResource(resourceName);
					if (resourceHandle == null) {
						throw new ClassNotFoundException(className);
					}

					byte[] bytes;
					Manifest manifest;
					try {
						// get the bytes from the class file
						bytes = resourceHandle.getBytes();

						// get the manifest for defining the packages
						manifest = resourceHandle.getManifest();
					} catch (IOException e) {
						throw new ClassNotFoundException(className, e);
					}

					// get the certificates for the code source
					Certificate[] certificates = resourceHandle.getCertificates();

					// the code source url is used to define the package and as the security context for the class
					URL codeSourceUrl = resourceHandle.getCodeSourceUrl();

					// define the package (required for security)
					definePackage(className, codeSourceUrl, manifest);

					// this is the security context of the class
					CodeSource codeSource = new CodeSource(codeSourceUrl, certificates);

					// load the class into the vm
					Class<?> clazz = defineClass(className, bytes, 0, bytes.length, codeSource);
					return clazz;
				}
			}, accessControlContext);
		} catch (PrivilegedActionException e) {
			throw (ClassNotFoundException) e.getException();
		}
	}

	private void definePackage(String className, URL jarUrl, Manifest manifest) {
		int packageEnd = className.lastIndexOf('.');
		if (packageEnd < 0) {
			return;
		}

		String packageName = className.substring(0, packageEnd);
		String packagePath = packageName.replace('.', '/') + "/";

		Attributes packageAttributes = null;
		Attributes mainAttributes = null;
		if (manifest != null) {
			packageAttributes = manifest.getAttributes(packagePath);
			mainAttributes = manifest.getMainAttributes();
		}
		Package pkg = getPackage(packageName);
		if (pkg != null) {
			if (pkg.isSealed()) {
				if (!pkg.isSealed(jarUrl)) {
					throw new SecurityException("Package was already sealed with another URL: package=" + packageName
							+ ", url=" + jarUrl);
				}
			} else {
				if (isSealed(packageAttributes, mainAttributes)) {
					throw new SecurityException("Package was already been loaded and not sealed: package="
							+ packageName + ", url=" + jarUrl);
				}
			}
		} else {
			String specTitle = getAttribute(Attributes.Name.SPECIFICATION_TITLE, packageAttributes, mainAttributes);
			String specVendor = getAttribute(Attributes.Name.SPECIFICATION_VENDOR, packageAttributes, mainAttributes);
			String specVersion = getAttribute(Attributes.Name.SPECIFICATION_VERSION, packageAttributes, mainAttributes);
			String implTitle = getAttribute(Attributes.Name.IMPLEMENTATION_TITLE, packageAttributes, mainAttributes);
			String implVendor = getAttribute(Attributes.Name.IMPLEMENTATION_VENDOR, packageAttributes, mainAttributes);
			String implVersion = getAttribute(Attributes.Name.IMPLEMENTATION_VERSION, packageAttributes, mainAttributes);

			URL sealBase = null;
			if (isSealed(packageAttributes, mainAttributes)) {
				sealBase = jarUrl;
			}

			definePackage(packageName, specTitle, specVersion, specVendor, implTitle, implVersion, implVendor, sealBase);
		}
	}

	private String getAttribute(Attributes.Name name, Attributes packageAttributes, Attributes mainAttributes) {
		if (packageAttributes != null) {
			String value = packageAttributes.getValue(name);
			if (value != null) {
				return value;
			}
		}
		if (mainAttributes != null) {
			return mainAttributes.getValue(name);
		}
		return null;
	}

	private boolean isSealed(Attributes packageAttributes, Attributes mainAttributes) {
		String sealed = getAttribute(Attributes.Name.SEALED, packageAttributes, mainAttributes);
		if (sealed == null) {
			return false;
		}
		return "true".equalsIgnoreCase(sealed);
	}
}
