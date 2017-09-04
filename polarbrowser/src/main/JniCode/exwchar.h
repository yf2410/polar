#if !defined(SX_EXWCHAR_H)
#define SX_EXWCHAR_H
#include <stddef.h>
extern "C"{
int
        wcsncasecmp(const wchar_t *s1, const wchar_t *s2, size_t n);

int
        wcscasecmp(const wchar_t *s1, const wchar_t *s2);

wchar_t *
        wcscasestr(const wchar_t * __restrict s, const wchar_t * __restrict find);

wchar_t *
        wcsrcasechr(const wchar_t *s, wchar_t c);
}


#endif
