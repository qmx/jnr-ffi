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

import com.kenai.jffi.Platform;
import jnr.ffi.NativeLong;
import jnr.ffi.annotations.LongLong;

import java.lang.annotation.Annotation;

public final class NumberUtil {
    private NumberUtil() {}
    
    static final Class getBoxedClass(Class c) {
        if (!c.isPrimitive()) {
            return c;
        }

        if (void.class == c) {
            return Void.class;

        } else if (byte.class == c) {
            return Byte.class;
        
        } else if (char.class == c) {
            return Character.class;

        } else if (short.class == c) {
            return Short.class;

        } else if (int.class == c) {
            return Integer.class;

        } else if (long.class == c) {
            return Long.class;

        } else if (float.class == c) {
            return Float.class;

        } else if (double.class == c) {
            return Double.class;

        } else if (boolean.class == c) {
            return Boolean.class;

        } else {
            throw new IllegalArgumentException("unknown primitive class");
        }
    }

    static final Class getPrimitiveClass(Class c) {
        if (Void.class == c) {
            return void.class;

        } else if (Boolean.class == c) {
            return boolean.class;

        } else if (Byte.class == c) {
            return byte.class;

        } else if (Character.class == c) {
            return char.class;

        } else if (Short.class == c) {
            return short.class;

        } else if (Integer.class == c) {
            return int.class;

        } else if (Long.class == c) {
            return long.class;

        } else if (Float.class == c) {
            return float.class;

        } else if (Double.class == c) {
            return double.class;
        
        } else if (NativeLong.class == c) {
            return long.class;

        } else if (c.isPrimitive()) {
            return c;
        } else {
            throw new IllegalArgumentException("unsupported number class");
        }
    }

    public static boolean isPrimitiveInt(Class c) {
        return byte.class == c || char.class == c || short.class == c || int.class == c || boolean.class == c;
    }


    public static final void widen(SkinnyMethodAdapter mv, Class from, Class to) {
        if (long.class == to && long.class != from && isPrimitiveInt(from)) {
            mv.i2l();
        }
    }

    public static final void narrow(SkinnyMethodAdapter mv, Class from, Class to) {
        if (!from.equals(to)) {
            if (byte.class == to || short.class == to || char.class == to || int.class == to || boolean.class == to) {
                if (long.class == from) {
                    mv.l2i();
                }

                if (byte.class == to) {
                    mv.i2b();

                } else if (short.class == to) {
                    mv.i2s();

                } else if (char.class == to) {
                    mv.i2c();

                } else if (boolean.class == to) {
                    // Ensure only 0x0 and 0x1 values are used for boolean
                    mv.iconst_1();
                    mv.iand();
                }
            }
        }
    }

    static boolean isLong32(Class type, Annotation[] annotations) {
        return isLong32(Platform.getPlatform(), type, annotations);
    }

    static boolean isLong32(Platform platform, Class type, Annotation[] annotations) {
        return platform.longSize() == 32
            && (((long.class == type || Long.class.isAssignableFrom(type))
                 && !InvokerUtil.hasAnnotation(annotations, LongLong.class))
               || NativeLong.class.isAssignableFrom(type));
    }

    static boolean isLong64(Class type, Annotation[] annotations) {
        final int longSize = Platform.getPlatform().longSize();
        return ((long.class == type || Long.class.isAssignableFrom(type))
                && (longSize == 64 || InvokerUtil.hasAnnotation(annotations, LongLong.class)))
            || (NativeLong.class.isAssignableFrom(type) && longSize == 64);
    }

    static boolean isInt32(Class type, Annotation[] annotations) {
        return Boolean.class.isAssignableFrom(type) || boolean.class == type
                || Byte.class.isAssignableFrom(type) || byte.class == type
                || Short.class.isAssignableFrom(type) || short.class == type
                || Integer.class.isAssignableFrom(type) || int.class == type
                || isLong32(type, annotations)
                || Enum.class.isAssignableFrom(type)
                ;
    }
}
