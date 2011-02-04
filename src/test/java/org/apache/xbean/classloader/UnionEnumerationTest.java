package org.apache.xbean.classloader;

import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.NoSuchElementException;

import junit.framework.TestCase;

/**
 * Test the {@link UnionEnumeration}.
 * 
 * @author Phillip Webb
 */
public class UnionEnumerationTest extends TestCase {

	/**
	 * @return A mock enum implementation
	 */
	@SuppressWarnings("unchecked")
	private Enumeration<String> mockEnum() {
		return mock(Enumeration.class);
	}

	/**
	 * Given a {@link UnionEnumeration} when constructed with any null arguments then a suitable
	 * {@link IllegalArgumentException} is thrown.
	 * @throws Exception
	 */
	public void testNullConstructorsArguments() throws Exception {
		try {
			new UnionEnumeration<String>(null);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("Illegal null enumerations specified for UnionEnumeration", e.getMessage());
		}

		try {
			new UnionEnumeration<String>(null, mockEnum());
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("Illegal null first specified for UnionEnumeration", e.getMessage());
		}

		try {
			new UnionEnumeration<String>(mockEnum(), null);
			fail();
		} catch (IllegalArgumentException e) {
			assertEquals("Illegal null second specified for UnionEnumeration", e.getMessage());
		}
	}

	/**
	 * Given a {@link UnionEnumeration} backed by two enumerations then suitable results are enumerated.
	 * @throws Exception
	 */
	public void testUnionOfTwoItems() throws Exception {
		Enumeration<String> e1 = Collections.enumeration(Arrays.asList("a", "b"));
		Enumeration<String> e2 = Collections.enumeration(Arrays.asList("c", "d"));
		Enumeration<String> enumeration = new UnionEnumeration<String>(e1, e2);

		assertTrue(enumeration.hasMoreElements());
		assertEquals("a", enumeration.nextElement());
		assertTrue(enumeration.hasMoreElements());
		assertEquals("b", enumeration.nextElement());
		assertTrue(enumeration.hasMoreElements());
		assertEquals("c", enumeration.nextElement());
		assertTrue(enumeration.hasMoreElements());
		assertEquals("d", enumeration.nextElement());
		assertFalse(enumeration.hasMoreElements());
		try {
			enumeration.nextElement();
			fail();
		} catch (NoSuchElementException e) {
		}
	}

	/**
	 * Given a {@link UnionEnumeration} backed by a list of enumerations then suitable results are enumerated.
	 * @throws Exception
	 */
	public void testUnionOfList() throws Exception {
		List<Enumeration<String>> list = new ArrayList<Enumeration<String>>();
		list.add(Collections.enumeration(Arrays.asList("a")));
		list.add(Collections.enumeration(Arrays.asList("b", "c")));
		list.add(Collections.enumeration(Arrays.asList(new String[] {})));
		list.add(Collections.enumeration(Arrays.asList("d")));
		Enumeration<String> enumeration = new UnionEnumeration<String>(list);

		assertTrue(enumeration.hasMoreElements());
		assertEquals("a", enumeration.nextElement());
		assertTrue(enumeration.hasMoreElements());
		assertEquals("b", enumeration.nextElement());
		assertTrue(enumeration.hasMoreElements());
		assertEquals("c", enumeration.nextElement());
		assertTrue(enumeration.hasMoreElements());
		assertEquals("d", enumeration.nextElement());
		assertFalse(enumeration.hasMoreElements());
		try {
			enumeration.nextElement();
			fail();
		} catch (NoSuchElementException e) {
		}
	}
}
