package org.apache.xbean.classloader;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

/**
 * Test the {@link ResourceEnumeration}.
 * 
 * @author Phillip Webb
 */
public class ResourceEnumerationTest extends TestCase {

	/**
	 * Given a {@link ResourceEnumeration} backed by an empty collection when enumerating items then no values are
	 * returned.
	 * @throws Exception
	 */
	public void testEmptyCollection() throws Exception {
		Collection<ResourceLocation> collection = Collections.emptySet();
		ResourceEnumeration enumeration = new ResourceEnumeration(collection, "test");
		assertFalse(enumeration.hasMoreElements());
		try {
			enumeration.nextElement();
			fail();
		} catch (NoSuchElementException e) {
		}
	}

	/**
	 * Given a {@link ResourceEnumeration} backed by a complete collection when enumerating items then correct values
	 * are returned.
	 * @throws Exception
	 */
	public void testAllValid() throws Exception {
		String resourceName = "test";
		List<ResourceLocation> collection = new ArrayList<ResourceLocation>();

		MockItem item1 = new MockItem(resourceName, "1");
		collection.add(item1.getLocation());
		MockItem item2 = new MockItem(resourceName, "2");
		collection.add(item2.getLocation());

		ResourceEnumeration enumeration = new ResourceEnumeration(collection, resourceName);

		assertTrue(enumeration.hasMoreElements());
		assertSame(item1.getUrl(), enumeration.nextElement());
		assertTrue(enumeration.hasMoreElements());
		assertSame(item2.getUrl(), enumeration.nextElement());
		assertFalse(enumeration.hasMoreElements());
		try {
			enumeration.nextElement();
			fail();
		} catch (NoSuchElementException e) {
		}
	}

	/**
	 * Given a {@link ResourceEnumeration} backed by a collection where one of the items is for a different resource
	 * when enumerating items then correct values are returned and the item for the different resource is skipped.
	 * @throws Exception
	 */

	public void testMissingResource() throws Exception {
		String resourceName = "test";
		List<ResourceLocation> collection = new ArrayList<ResourceLocation>();

		MockItem item1 = new MockItem(resourceName, "1");
		collection.add(item1.getLocation());
		MockItem item2 = new MockItem("missing", "2");
		collection.add(item2.getLocation());
		MockItem item3 = new MockItem(resourceName, "3");
		collection.add(item3.getLocation());

		ResourceEnumeration enumeration = new ResourceEnumeration(collection, resourceName);

		assertTrue(enumeration.hasMoreElements());
		assertSame(item1.getUrl(), enumeration.nextElement());
		assertTrue(enumeration.hasMoreElements());
		assertSame(item3.getUrl(), enumeration.nextElement());
		assertFalse(enumeration.hasMoreElements());
		try {
			enumeration.nextElement();
			fail();
		} catch (NoSuchElementException e) {
		}
	}

	/**
	 * Given a {@link ResourceEnumeration} backed by a collection that throws an illegal state exception then items are
	 * returned up to the point of the exception, the exception is thrown then the enumeration is cleared.
	 * @throws Exception
	 */
	public void testItemThowingIllegalStateException() throws Exception {
		String resourceName = "test";
		List<ResourceLocation> collection = new ArrayList<ResourceLocation>();

		MockItem item1 = new MockItem(resourceName, "1");
		collection.add(item1.getLocation());
		MockItem item2 = new MockItem(resourceName, "2", true);
		collection.add(item2.getLocation());

		ResourceEnumeration enumeration = new ResourceEnumeration(collection, resourceName);

		assertTrue(enumeration.hasMoreElements());
		assertSame(item1.getUrl(), enumeration.nextElement());

		try {
			enumeration.hasMoreElements();
			fail();
		} catch (IllegalStateException e) {
		}
		assertFalse(enumeration.hasMoreElements());
	}

	/**
	 * Item that mocks a {@link ResourceLocation}, {@link ResourceHandle} and {@link URL}.
	 */
	private static class MockItem {
		private ResourceLocation location;
		private ResourceHandle handle;
		private URL url;

		public MockItem(String resourceName, String id) throws MalformedURLException {
			this(resourceName, id, false);
		}

		public MockItem(String resourceName, String id, boolean throwsException) throws MalformedURLException {
			this.location = mock(ResourceLocation.class);
			this.handle = mock(ResourceHandle.class);
			this.url = new URL("file:///" + id);
			if (throwsException) {
				when(location.getResourceHandle(resourceName)).thenThrow(new IllegalStateException());
			} else {
				when(location.getResourceHandle(resourceName)).thenReturn(handle);
			}
			when(handle.getUrl()).thenReturn(url);
		}

		public ResourceLocation getLocation() {
			return location;
		}

		public URL getUrl() {
			return url;
		}
	}
}
