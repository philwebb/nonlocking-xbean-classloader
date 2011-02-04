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

import java.io.IOException;
import java.net.URL;
import java.util.jar.Manifest;

/**
 * A resource location that acts as a container for named resources. Resource data can be accessed via a
 * {@link ResourceHandle} obtained using the {@link #getResourceHandle(String)} method. Locations can include manifests
 * and code source metadata. etc.
 * <p/>
 * As soon as the location is no longer in use, it should be explicitly {@link #close}d, similarly to I/O streams.
 * 
 * @see JarResourceLocation
 * @see DirectoryResourceLocation
 * 
 * @author Dain Sundstrom
 */
public interface ResourceLocation {

	/**
	 * @return the CodeSource URL for the class or resource.
	 */
	URL getCodeSource();

	/**
	 * Obtain a resource handle for the specified resource name or <tt>null</tt> if the name is not contained in the
	 * location.
	 * @param resourceName The name of the resource to load
	 * @return A {@link ResourceHandle} that can be used to access resource data or <tt>null</tt> if the resource cannot
	 * be located.
	 */
	ResourceHandle getResourceHandle(String resourceName);

	/**
	 * @return the Manifest of the location or <tt>null</tt> if no manifest is availble for this location.
	 * @throws IOException
	 */
	Manifest getManifest() throws IOException;

	/**
	 * Closes a connection to the resource identified by this location. Releases any I/O objects associated with the
	 * location.
	 */
	void close();
}
