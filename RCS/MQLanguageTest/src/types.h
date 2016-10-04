#pragma once

#include <string>

typedef std::string String;
typedef unsigned int UINT;
typedef void* LPVOID;
typedef const char* LPCSTR;
typedef int BOOL;
typedef unsigned long long ULONG64;

#ifndef WIN32
typedef unsigned int DWORD;
typedef unsigned char BYTE;
#endif

#define TRUE 1
#define FALSE 0
