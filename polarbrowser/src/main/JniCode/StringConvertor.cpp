/*
 * StringConvertor.cpp
 *
 *  Created on: 2014-10-29
 *      Author: dpk
 */
//参数1是UTF8字符串当前位置指针，这里必须要是指针，因为必须要通过第1个字符进行判断才知道一个完整的字符的编码要向后取多少个字符
//参数2是返回的UCS-2编码的Unicode字符
#include "StringConvertor.h"

inline int UTF82UnicodeOne(const char* utf8, wchar_t& wch)
{
	//首字符的Ascii码大于0xC0才需要向后判断，否则，就肯定是单个ANSI字符了
	unsigned char firstCh = utf8[0];
	if (firstCh >= 0xC0)
	{
		//根据首字符的高位判断这是几个字母的UTF8编码
		int afters, code;
		if ((firstCh & 0xE0) == 0xC0)
		{
			afters = 2;
			code = firstCh & 0x1F;
		}
		else if ((firstCh & 0xF0) == 0xE0)
		{
			afters = 3;
			code = firstCh & 0xF;
		}
		else if ((firstCh & 0xF8) == 0xF0)
		{
			afters = 4;
			code = firstCh & 0x7;
		}
		else if ((firstCh & 0xFC) == 0xF8)
		{
			afters = 5;
			code = firstCh & 0x3;
		}
		else if ((firstCh & 0xFE) == 0xFC)
		{
			afters = 6;
			code = firstCh & 0x1;
		}
		else
		{
			wch = firstCh;
			return 1;
		}

		//知道了字节数量之后，还需要向后检查一下，如果检查失败，就简单的认为此UTF8编码有问题，或者不是UTF8编码，于是当成一个ANSI来返回处理
		for(int k = 1; k < afters; ++ k)
		{
			if ((utf8[k] & 0xC0) != 0x80)
			{
				//判断失败，不符合UTF8编码的规则，直接当成一个ANSI字符返回
				wch = firstCh;
				return 1;
			}

			code <<= 6;
			code |= (unsigned char)utf8[k] & 0x3F;
		}

		wch = code;
		return afters;
	}
	else
	{
		wch = firstCh;
	}

	return 1;
}

//参数1是UTF8编码的字符串
//参数2是输出的UCS-2的Unicode字符串
//参数3是参数1字符串的长度
//使用的时候需要注意参数2所指向的内存块足够用。其实安全的办法是判断一下pUniBuf是否为NULL，如果为NULL则只统计输出长度不写pUniBuf，这样
//通过两次函数调用就可以计算出实际所需要的Unicode缓存输出长度。当然，更简单的思路是：无论如何转换，UTF8的字符数量不可能比Unicode少，所
//以可以简单的按照sizeof(wchar_t) * utf8Leng来分配pUniBuf的内存……
int UTF82Unicode(const char* utf8Buf, wchar_t *pUniBuf, int utf8Leng)
{
	int i = 0, count = 0;
	while(i < utf8Leng)
	{
		i += UTF82UnicodeOne(utf8Buf + i, pUniBuf[count]);
		count ++;
	}

	return count;
}

// utf8->unicode char*->tchar*
TCHAR* UTF8Char2UnicodeChar(char* pszData) {
	// unicode字符串结尾必须保证有2个0才可以，否则会认为字符串没结束
	int unicodeLength = strlen(pszData) + 2;
	TCHAR* lpszData = new TCHAR[unicodeLength];
	memset(lpszData, 0, (unsigned int) (unicodeLength) * sizeof(TCHAR));
	UTF82Unicode(pszData, lpszData, strlen(pszData));

	return lpszData;
}

inline int Unicode2UTF8One(unsigned int wchar, char *utf8)
{
	int len = 0;
	if (wchar < 0xC0)
	{
		utf8[len ++] = (char)wchar;
	}
	else if (wchar < 0x800)
	{
		utf8[len ++] = 0xc0 | (wchar >> 6);
		utf8[len ++] = 0x80 | (wchar & 0x3f);
	}
	else if (wchar < 0x10000)
	{
		utf8[len ++] = 0xe0 | (wchar >> 12);
		utf8[len ++] = 0x80 | ((wchar >> 6) & 0x3f);
		utf8[len ++] = 0x80 | (wchar & 0x3f);
	}
	else if (wchar < 0x200000)
	{
		utf8[len ++] = 0xf0 | ((int)wchar >> 18);
		utf8[len ++] = 0x80 | ((wchar >> 12) & 0x3f);
		utf8[len ++] = 0x80 | ((wchar >> 6) & 0x3f);
		utf8[len ++] = 0x80 | (wchar & 0x3f);
	}
	else if (wchar < 0x4000000)
	{
		utf8[len ++] = 0xf8 | ((int)wchar >> 24);
		utf8[len ++] = 0x80 | ((wchar >> 18) & 0x3f);
		utf8[len ++] = 0x80 | ((wchar >> 12) & 0x3f);
		utf8[len ++] = 0x80 | ((wchar >> 6) & 0x3f);
		utf8[len ++] = 0x80 | (wchar & 0x3f);
	}
	else if (wchar < 0x80000000)
	{
		utf8[len ++] = 0xfc | ((int)wchar >> 30);
		utf8[len ++] = 0x80 | ((wchar >> 24) & 0x3f);
		utf8[len ++] = 0x80 | ((wchar >> 18) & 0x3f);
		utf8[len ++] = 0x80 | ((wchar >> 12) & 0x3f);
		utf8[len ++] = 0x80 | ((wchar >> 6) & 0x3f);
		utf8[len ++] = 0x80 | (wchar & 0x3f);
	}

	return len;
}

int Unicode2UTF8(const wchar_t* unicodeBuf, char *pUtf8Buf, int unicodeSize)
{
	int i = 0, count = 0;

	while (i < unicodeSize) {
		count += Unicode2UTF8One((unsigned int)unicodeBuf[i], pUtf8Buf + count);
		++i;
	}
	return count;
}



