/*
 * StringConvertor.h
 *
 *  Created on: 2014-10-29
 *      Author: dpk
 */
#include "globaldef.h"

#ifndef STRINGCONVERTOR_H_
#define STRINGCONVERTOR_H_

inline int UTF82UnicodeOne(const char* utf8, wchar_t& wch);
inline int Unicode2UTF8One(unsigned wchar, char *utf8);

int UTF82Unicode(const char* utf8Buf, wchar_t *pUniBuf, int utf8Leng);
int Unicode2UTF8(const wchar_t* unicodeBuf, char *pUtf8Buf, int unicodeLen);

TCHAR* UTF8Char2UnicodeChar(char* pszData);

#endif /* STRINGCONVERTOR_H_ */
