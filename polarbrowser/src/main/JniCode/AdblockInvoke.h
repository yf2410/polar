//Interface For IOS, Note All Param Is utf8
#include "globaldef.h"

#ifdef __cplusplus
extern "C" {
#endif
void AdblockInitialize(LPCTSTR lpszRaw, LPCTSTR lpszWin, LPCTSTR lpszEpt);
bool AdblockMatchUrlByRaw(LPCTSTR lpszDocUrl, LPCTSTR lpszDocHost, LPCTSTR lpszUrl, LPCTSTR lpszUrlHost);
std::string AdblockMatchCssByAll(LPCTSTR lpszDocUrl, LPCTSTR lpszDocHost);
void AdblockMatchCssFree(LPTSTR lpszCss);
#ifdef __cplusplus
}
#endif
