package org.apache.xbean.classloader;

import java.io.IOException;
import java.net.URL;
import java.util.jar.Manifest;

import junit.framework.TestCase;

/**
 * Test the {@link AbstractUrlResourceLocation}.
 * 
 * @author Phillip Webb
 */
public class AstractUrlResourceLocationTest extends TestCase {

	/**
	 * Given a {@link AbstractUrlResourceLocation} when the constructor is called with a null codeSource then an
	 * {@link IllegalArgumentException} is thrown.
	 * 
	 * @throws Exception
	 */
	public void testConstructorWithNullThrowsIllegalArgumentException() throws Exception {
		try {
			new MockUrlResourceLocation(null);
			fail("Did not throw");
		} catch (IllegalArgumentException e) {
			assertEquals("Illegal null codeSource specified for AbstractUrlResourceLocation", e.getMessage());
		}
	}

	/**
	 * Given a {@link AbstractUrlResourceLocation} when getCodeSource() is called then the source passed to the
	 * constructor is returned.
	 * 
	 * @throws Exception
	 */
	public void testGetCodeSource() throws Exception {
		URL codeSource = new URL("http://");
		AbstractUrlResourceLocation location = new MockUrlResourceLocation(codeSource);
		assertSame(codeSource, location.getCodeSource());
	}

	/**
	 * Given a {@link AbstractUrlResourceLocation} when equals is called with null then the result is false
	 * 
	 * @throws Exception
	 */
	public void testEqualsNull() throws Exception {
		AbstractUrlResourceLocation location = new MockUrlResourceLocation(new URL("http://"));
		assertFalse(location.equals(null));
	}

	/**
	 * Given a {@link AbstractUrlResourceLocation} when equals is called with the same object then the result is true
	 * 
	 * @throws Exception
	 */
	public void testEqualsSame() throws Exception {
		AbstractUrlResourceLocation location = new MockUrlResourceLocation(new URL("http://"));
		assertTrue(location.equals(location));
	}

	/**
	 * Given a {@link AbstractUrlResourceLocation} when equals is called with a object of a different class then the
	 * result is false
	 * 
	 * @throws Exception
	 */
	public void testEqualsWithDifferentClass() throws Exception {
		URL codeSource = new URL("http://");
		AbstractUrlResourceLocation location = new MockUrlResourceLocation(codeSource);
		AbstractUrlResourceLocation otherLocation = new OtherMockUrlResourceLocation(codeSource);
		assertFalse(location.equals(otherLocation));
		assertFalse(otherLocation.equals(location));
	}

	/**
	 * Given a {@link AbstractUrlResourceLocation} when equals is called with another object of the same class then the
	 * result is taken from the url
	 * 
	 * @throws Exception
	 */
	public void testEqualsDelegatesToCodeSource() throws Exception {
		URL codeSource = new URL("http://");
		AbstractUrlResourceLocation location = new MockUrlResourceLocation(codeSource);
		AbstractUrlResourceLocation equalsLocation = new MockUrlResourceLocation(codeSource);
		AbstractUrlResourceLocation differentLocation = new MockUrlResourceLocation(new URL("https://"));
		assertTrue(location.equals(equalsLocation));
		assertTrue(equalsLocation.equals(location));
		assertFalse(location.equals(differentLocation));
		assertFalse(differentLocation.equals(location));
	}

	/**
	 * Given a {@link AbstractUrlResourceLocation} when hashCode is called then the result is identical to the
	 * codeSource hashCode.
	 * 
	 * @throws Exception
	 */
	public void testHashCodeDelegateToCodeSource() throws Exception {
		URL codeSource = new URL("http://");
		assertEquals(codeSource.hashCode(), new MockUrlResourceLocation(codeSource).hashCode());
	}

	/**
	 * Given a {@link AbstractUrlResourceLocation} then toString() is called then the result is generated from the
	 * codeSource.
	 * 
	 * @throws Exception
	 */
	public void testToString() throws Exception {
		assertEquals("[org.apache.xbean.classloader.AstractUrlResourceLocationTest$MockUrlResourceLocation: http:]",
				new MockUrlResourceLocation(new URL("http://")).toString());
	}

	/**
	 * Mock implementation of {@link AbstractUrlResourceLocation} used for testing.
	 */
	private static class MockUrlResourceLocation extends AbstractUrlResourceLocation {
		public MockUrlResourceLocation(URL codeSource) {
			super(codeSource);
		}

		public Manifest getManifest() throws IOException {
			throw new UnsupportedOperationException();
		}

		public ResourceHandle getResourceHandle(String resourceName) {
			throw new UnsupportedOperationException();
		}
	}

	/**
	 * Mock implementation of {@link AbstractUrlResourceLocation} used for testing.
	 */
	private static class OtherMockUrlResourceLocation extends MockUrlResourceLocation {
		public OtherMockUrlResourceLocation(URL codeSource) {
			super(codeSource);
		}
	}
}
