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

import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * @param <T>
 * 
 * @author Dain Sundstrom
 */
public final class UnionEnumeration<T> implements Enumeration<T> {

	private final LinkedList<Enumeration<T>> enumerations = new LinkedList<Enumeration<T>>();

	public UnionEnumeration(List<Enumeration<T>> enumerations) {
		if (enumerations == null) {
			throw new IllegalArgumentException("Illegal null enumerations specified for UnionEnumeration");
		}
		this.enumerations.addAll(enumerations);
	}

	public UnionEnumeration(Enumeration<T> first, Enumeration<T> second) {
		if (first == null) {
			throw new IllegalArgumentException("Illegal null first specified for UnionEnumeration");
		}
		if (second == null) {
			throw new IllegalArgumentException("Illegal null second specified for UnionEnumeration");
		}
		enumerations.add(first);
		enumerations.add(second);
	}

	public boolean hasMoreElements() {
		while (!enumerations.isEmpty()) {
			Enumeration<T> enumeration = enumerations.getFirst();
			if (enumeration.hasMoreElements()) {
				return true;
			}
			enumerations.removeFirst();
		}
		return false;
	}

	public T nextElement() {
		while (!enumerations.isEmpty()) {
			Enumeration<T> enumeration = enumerations.getFirst();
			if (enumeration.hasMoreElements()) {
				return enumeration.nextElement();
			}
			enumerations.removeFirst();
		}
		throw new NoSuchElementException();
	}
}
