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

package jnr.ffi.provider.jffi;

import com.kenai.jffi.CallingConvention;
import com.kenai.jffi.Function;
import com.kenai.jffi.Internals;
import static jnr.x86asm.Asm.*;
import jnr.x86asm.Assembler;
import static jnr.ffi.provider.jffi.CodegenUtils.*;

/**
 * Compilers method trampoline stubs for x86_64 
 */
final class X86_64StubCompiler extends AbstractX86StubCompiler {
    

    @Override
    final boolean canCompile(Class returnType, Class[] parameterTypes, CallingConvention convention) {
        
        if (returnType != byte.class && returnType != short.class && returnType != int.class
                && returnType != long.class && returnType != float.class && returnType != double.class
                && returnType != void.class) {
            return false;
        }

        // There is only one calling convention; SYSV, so abort if someone tries to use stdcall
        if (convention != CallingConvention.DEFAULT) {
            return false;
        }

        int fCount = 0;
        int iCount = 0;

        for (Class t : parameterTypes) {
            if (t == byte.class || t == short.class || t == int.class || t == long.class) {
                ++iCount;
            } else if (t == float.class || t == double.class) {
                ++fCount;
            } else {
                // Fail on anything else
                return false;
            }
        }

        // We can only safely compile methods with up to 6 integer and 8 floating point parameters
        if (iCount > 6 || fCount > 8) {
            return false;
        }
        
        return true;
    }

    @Override
    final void compile(Function function, String name, Class returnType, Class[] parameterTypes,
            CallingConvention convention, boolean saveErrno) {
        
        int fCount = 0;
        int iCount = 0;

        for (Class t : parameterTypes) {
            if (t == byte.class || t == short.class || t == int.class || t == long.class) {
                ++iCount;
            } else if (t == float.class || t == double.class) {
                ++fCount;
            } else {
                throw new IllegalArgumentException("invalid parameter type " + t);
            }
        }
        
        Assembler a = new Assembler(X86_64);
        
        //
        // JNI functions all look like:
        // foo(JNIEnv* env, jobject self, arg...)
        // on AMD64, those sit in %rdi, %rsi, %rdx, %rcx, %r8 and %r9
        // So we need to shuffle all the integer args up to over-write the
        // env and self arguments
        //
        if (iCount > 0) {
            a.mov(rdi, rdx);
        }
        if (iCount > 1) {
            a.mov(rsi, rcx);
        }
        if (iCount > 2) {
            a.mov(rdx, r8);
        }
        if (iCount > 3) {
            a.mov(rcx, r9);
        }

        // For args 5 & 6 of the function, they would have been pushed on the stack
        if (iCount > 4) {
            a.mov(r8, qword_ptr(rsp, 8));
        }

        if (iCount > 5) {
            a.mov(r9, qword_ptr(rsp, 16));
        }
        if (iCount > 6) {
            throw new IllegalArgumentException("integer argument count > 6");
        }

        // All the integer registers are loaded; there nothing to do for the floating
        // registers, as the first 8 args are already in xmm0..xmm7, so just sanity check
        if (fCount > 8) {
            throw new IllegalArgumentException("float argument count > 8");
        }

        // Need to align the stack to 16 bytes for function call.
        // It already has 8 bytes pushed (the return address), so making space
        // to save the return value from the function neatly aligns it to 16 bytes
        int space = returnType == float.class || returnType == double.class
                    ? 24 : 8;
        a.sub(rsp, imm(space));

        // Call to the actual native function
        a.mov(rax, imm(function.getFunctionAddress()));
        a.call(rax);

        if (saveErrno) {
            // Save the return on the stack
            if (returnType == void.class) {
                // No need to save/reload return value registers

            } else if (returnType == float.class) {
                a.movss(dword_ptr(rsp, 0), xmm0);

            } else if (returnType == double.class) {
                a.movsd(qword_ptr(rsp, 0), xmm0);

            } else {
                a.mov(qword_ptr(rsp, 0), rax);
            }

            // Save the errno in a thread-local variable
            a.mov(rax, imm(errnoFunctionAddress));
            a.call(rax);

            // Retrieve return value and put it back in the appropriate return register
            if (returnType == void.class) {
                // No need to save/reload return value registers

            } else if (returnType == float.class) {
                a.movss(xmm0, dword_ptr(rsp, 0));

            } else if (returnType == double.class) {
                a.movsd(xmm0, qword_ptr(rsp, 0));

            } else {
                a.mov(rax, dword_ptr(rsp, 0));
            }
        }

        // Restore rsp to original position
        a.add(rsp, imm(space));
        a.ret();

        stubs.add(new Stub(name, sig(returnType, parameterTypes), a));
    }
}
