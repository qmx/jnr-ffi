/*
 * Copyright (c) 2007 Wayne Meissner. All rights reserved.
 * 
 * This file is part of jffi.
 *
 * This code is free software: you can redistribute it and/or modify it under 
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT 
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or 
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License 
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

#include <sys/types.h>
#include <sys/param.h>
#ifndef __mips__
# include <stdint.h>
#endif
#include <stdio.h>
#include <stdlib.h>
#include <string.h>
typedef void* ptr;
typedef void* pointer;
#ifdef _WIN32
typedef char* caddr_t;
#endif
#ifndef _STDINT_H
typedef signed char int8_t;
typedef signed short int16_t;
typedef signed int int32_t;
typedef signed long long int64_t;
#endif
#define RET(T) T ptr_ret_##T(void* arg1, int offset) { \
    T tmp; memcpy(&tmp, (caddr_t) arg1 + offset, sizeof(tmp)); return tmp; \
}
#define SET(T) void ptr_set_##T(void* arg1, int offset, T value) { \
    memcpy((caddr_t) arg1 + offset, &value, sizeof(value)); \
}
#define TEST(T) SET(T) RET(T)

TEST(int8_t);
TEST(int16_t);
TEST(int32_t);
TEST(int64_t);
TEST(float);
TEST(double);
TEST(pointer);

void*
ptr_return_array_element(void **ptrArray, int arrayIndex) 
{
    return ptrArray[arrayIndex];
}

void
ptr_set_array_element(void **ptrArray, int arrayIndex, void *value)
{    
    ptrArray[arrayIndex] = value;
}

void*
ptr_from_buffer(void* ptr)
{
    return ptr;
}
void*
ptr_malloc(int size) 
{
    void* ptr = malloc(size);
    memset(ptr, 0, size);
    return ptr;
}
void
ptr_free(void* ptr)
{
    free(ptr);
}
