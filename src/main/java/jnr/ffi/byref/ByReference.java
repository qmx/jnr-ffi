/*
 * Copyright (C) 2008-2010 Wayne Meissner
 *
 * This file is part of the JNR project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package jnr.ffi.byref;

import jnr.ffi.Pointer;
import jnr.ffi.Runtime;

/**
 * A ByReference subclass is used when a primitive parameter must be passed
 * by-reference.
 *
 * <p>For example, the following C code,
 * <p><blockquote><pre>
 * {@code
 * extern void get_a(int * ap);
 *
 * int foo(void) {
 *     int a;
 *     // pass a reference to 'a' so get_a() can fill it out
 *     get_a(&a);
 *
 *     return a;
 * }
 * }
 * </pre></blockquote>
 * <p>Would be declared in java as
 * <p><blockquote><pre>
 * {@code
 * interface Lib {
 *     void get_a(@Out IntByReference ap);
 * }
 * }
 * </pre></blockquote>
 * <p>and used like this
 * <p><blockquote><pre>
 * IntByReference ap = new IntByReference();
 * lib.get_a(ap);
 * System.out.printf("a from lib=%d\n", a.getValue());
 * </pre></blockquote>
 * @param <T>
 */
public interface ByReference<T> {
    /**
     * Gets the size of the native buffer required to store the value
     * 
     * @return the size in bytes of the native type
     */
    int nativeSize(Runtime runtime);

    /**
     * Copies the java value to native memory
     * 
     * @param memory the native memory buffer.
     */
    void marshal(Pointer memory, long offset);
    
    /**
     * Copies the java value from native memory
     * 
     * @param memory the native memory buffer.
     */
    void unmarshal(Pointer memory, long offset);
    
    T getValue();
}
