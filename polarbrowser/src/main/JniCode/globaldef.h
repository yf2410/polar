#if !defined(SX_GLOBALDEF_H)
#define SX_GLOBALDEF_H

#include <string.h>
#include <wchar.h>
#include <ctype.h>
#include <stdlib.h>
#include <locale.h>
#include <string>
#include <vector>
#include <map>
#include <algorithm>
#include <regex.h>
#include "exwchar.h"

using namespace std;

#define _T(x) L ## x

#ifndef BOOL
typedef signed char BOOL;
#endif
typedef wchar_t TCHAR;
typedef char CHAR;
typedef unsigned int UINT;
typedef unsigned long DWORD;
typedef DWORD *LPDWORD;
typedef long LONG;
typedef short WORD;

typedef const TCHAR *LPCTSTR;
typedef TCHAR *LPTSTR;
typedef const CHAR *LPCSTR;
typedef TCHAR *LPWSTR;


#define _tcsicmp wcscasecmp
#define _tcslen wcslen
#define _tcsnicmp wcsncasecmp
#define _tcscpy wcscpy
#define _tcschr wcschr
#define _tcsstr wcsstr
#define _tcsrchr wcsrchr
#define _tcsncpy wcsncpy
#define StrCmp wcscmp
#define StrCmpN wcsncmp
#define StrCmpNI wcsncasecmp
#define StrStrI wcscasestr
#define StrChrI wcsrcasechr


#define TRUE 1
#define FALSE 0

#define MAX_URL_LEN 2048

#ifndef SIZEOF
#define SIZEOF(A) (sizeof(A)/sizeof((A)[0]))
#endif

#ifndef MAKELONG
#define MAKELONG(a, b) ((LONG)(((WORD)(a)) | ((DWORD)((WORD)(b))) << 16))
#endif

#ifndef INTERNET_MAX_URL_LENGTH
#define INTERNET_MAX_HOST_NAME_LENGTH   256
#define INTERNET_MAX_USER_NAME_LENGTH   128
#define INTERNET_MAX_PASSWORD_LENGTH    128
#define INTERNET_MAX_PORT_NUMBER_LENGTH 5           // INTERNET_PORT is unsigned short
#define INTERNET_MAX_PORT_NUMBER_VALUE  65535       // maximum unsigned short value
#define INTERNET_MAX_PATH_LENGTH        2048
#define INTERNET_MAX_SCHEME_LENGTH      32          // longest protocol name length
#define INTERNET_MAX_URL_LENGTH         (INTERNET_MAX_SCHEME_LENGTH \
	+ sizeof("://") \
	+ INTERNET_MAX_PATH_LENGTH)
#endif

#endif
