#include "NavigateQuery.h"
#include "json/json.h"
#include "StringConvertor.h"
//
//#include <android/log.h>
//
//#define JNI_DEBUG
//
//#ifdef JNI_DEBUG
//
//#ifndef LOG_TAG
//#define LOG_TAG "JNI_DEBUG"
//#endif
//
//#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, LOG_TAG ,__VA_ARGS__) // 定义LOGD类型
//#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG ,__VA_ARGS__) // 定义LOGI类型
//#define LOGW(...) __android_log_print(ANDROID_LOG_WARN, LOG_TAG ,__VA_ARGS__) // 定义LOGW类型
//#define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG ,__VA_ARGS__) // 定义LOGE类型
//#define LOGF(...) __android_log_print(ANDROID_LOG_FATAL, LOG_TAG ,__VA_ARGS__) // 定义LOGF类型
//
//#endif

#define MAX_HISTORY_COUNT	1000
#define MAX_MOSTVISITED_COUNT	30

int PRESETWEIGHT = 1800;
int BOOKMARKWEIGHT = 2000;
int ONLYDOMAINWEIGHT = 600;
int ACCTIME24WEIGHT = 1000;
int ACCTIME24_72WEIGHT = 600;
int ACCTIME72_188WEIGHT = 200;
int ACCTIME188WEIGHT = 100;
int FROMBOOKMARKWEIGHT = 300;
int FROMADDRBOXWEIGHT = 1000;
int FROMSEARCHWEIGHT = 100;
int FROMEXTERNALWEIGHT = 100;
int FROMOTHERSRCWEIGHT = 100;
int INVALIDURLWEIGHT = 0;
int MATCHTITLE = 200;
int URLMACTHW = 1500;
int LEFTMATCH = 2000;
int CANACWEIGHT = 10000;

#ifndef MIN
#define MIN(a,b)            (((a) < (b)) ? (a) : (b))
#endif

enum HISTORY_SRC
{
	History_Src_Normal = 0,
	History_Src_Addr,
	History_Src_Bookmark,
	History_Src_History,
	History_Src_Other,
	History_Src_Search,
};

DWORD GetWeightByKeyMatch(LPCTSTR lpszKey, LPCTSTR lpszTitle, LPCTSTR lpszUrl, LPCTSTR lpszUrlOffset, int nSrcType, BOOL& bCanAutoComplete)
{
	DWORD dwRet = 0;
	bCanAutoComplete = FALSE;

	if(lpszTitle && 0 == wcscmp(lpszTitle, lpszKey))
		dwRet += MATCHTITLE;

	if(lpszUrlOffset)
	{
		BOOL bLeftMatch = FALSE;
		if(lpszUrlOffset == lpszUrl)
			bLeftMatch = TRUE;
		else if(lpszUrlOffset > lpszUrl)
		{
			if(lpszUrlOffset - lpszUrl > 3)
			{
				if(0 == StrCmpN(lpszUrlOffset - 3, _T("://"), 3))
					bLeftMatch = TRUE;
				else if((lpszUrlOffset - lpszUrl > 7 && 0 == StrCmpNI(lpszUrlOffset - 7, _T("://www."), 7))
					|| (lpszUrl + 4 == lpszUrlOffset && StrCmpNI(lpszUrl, _T("www."), 4) == 0))
					bLeftMatch = TRUE;
			}
		}

		if(bLeftMatch)
		{
			if(History_Src_Addr == nSrcType)
				bCanAutoComplete = TRUE;
			
			dwRet += LEFTMATCH;
		}
	}

	if((_T('w') == lpszKey[0] || _T('W') == lpszKey[0]) && 0 == lpszKey[1])
	{
		LPTSTR lpszMatch = StrStrI(lpszUrl, _T("://www."));
		if(lpszMatch)
		{
			lpszMatch += 7;
			if(StrChrI(lpszMatch, _T('w')))
				dwRet += URLMACTHW;
		}
	}

	return dwRet;
}

//////////////////////////////////////////////////////////////////////////

CNavigateQuery::CNavigateQuery(void)
{
	m_nMaxRetCount = 10;
	m_bInit = FALSE;
	memset(m_bLoaded, 0, sizeof(m_bLoaded));
	memset(m_szLastKey, 0, sizeof(m_szLastKey));
}

CNavigateQuery::~CNavigateQuery(void)
{

}

CNavigateQuery& CNavigateQuery::GetInstance()
{
	static CNavigateQuery _this;
	return _this;
}

void CNavigateQuery::SetMaxRetCount(UINT nCount)
{
	m_nMaxRetCount = nCount;
}

void CNavigateQuery::Load(UINT nType, void* lpszData)
{
	if(nType < NavQuery_All)
	{
		m_vNavItems[nType].clear();
		if(NavQuery_History == nType)
			m_mpHistoryIndex.clear();
		m_bLoaded[nType] = FALSE;
	}
	else if(NavQuery_All == nType)
	{
		m_mpHistoryIndex.clear();
		m_vNavItems[NavQuery_History].clear();
		m_bLoaded[NavQuery_History] = FALSE;

		m_vNavItems[NavQuery_Bookmark].clear();
		m_bLoaded[NavQuery_Bookmark] = FALSE;
	}
	m_vRetItems.clear();

	if(NavQuery_All != nType && m_bLoaded[nType])
		return ;

	memset(m_szLastKey, 0, sizeof(m_szLastKey));

	switch (nType)
	{
	case NavQuery_Preset: {
//		LOGD("loadPreset");
		TCHAR* lpszUnicodeData = UTF8Char2UnicodeChar((char*)lpszData);

		LoadPreset(lpszUnicodeData);
		break;
	}
	case NavQuery_History:
		LoadHistory((LPCSTR)lpszData);
		break;

	case NavQuery_Bookmark:
		LoadBookmark((LPCSTR)lpszData);
		break;

	case NavQuery_All:
		for (int i = 0; i < SIZEOF(m_bLoaded); ++ i)
		{
			if(!m_bLoaded[i])
				Load(i, FALSE);
		}
		break;
	}
}

DWORD GetProtoclLen(LPCTSTR lpszUrl)
{
	DWORD dwRet = 0;
	if(lpszUrl)
	{
		LPTSTR lpszProt = wcsstr(lpszUrl, _T("://"));
		if(lpszProt)
			dwRet = lpszProt + 3 -lpszUrl;
	}

	return dwRet;
}

DWORD GetWeightBySrc(int nSrcType)
{
	DWORD dwRet = 0;

	switch (nSrcType)
	{
	case History_Src_Bookmark:
		dwRet = FROMBOOKMARKWEIGHT;
		break;

	case History_Src_Addr:
		dwRet = FROMADDRBOXWEIGHT;
		break;

	case History_Src_Search:
		dwRet = FROMSEARCHWEIGHT;
		break;

	case History_Src_Normal:
		dwRet = FROMEXTERNALWEIGHT;
		break;

	case History_Src_Other:
		dwRet = FROMOTHERSRCWEIGHT;
		break;
	}

	return dwRet;
}

DWORD GetWeightByTime(time_t tAccTime)
{
	DWORD dwRet = 0;
	time_t tCur = time(NULL);
	time_t tInterval = tCur - tAccTime;

	if(tInterval <= 24 * 60 * 60)
		dwRet = ACCTIME24WEIGHT;
	else if(tInterval <= 72 * 60 * 60)
		dwRet = ACCTIME24_72WEIGHT;
	else if(tInterval <= 188 * 60 * 60)
		dwRet = ACCTIME72_188WEIGHT;
	else 
		dwRet = ACCTIME188WEIGHT;

	return dwRet;
}

DWORD GetWeightByUrl(LPCTSTR lpszUrl, DWORD dwProtLen)
{
	DWORD dwRet = 0;

	BOOL bOnlyDomain = FALSE;
	if(lpszUrl)
	{
		LPCTSTR lpszDomain = lpszUrl + dwProtLen;
		LPTSTR lpszEnd = wcschr(lpszDomain, _T('/'));
		if(lpszEnd)
			bOnlyDomain = (0 == *(lpszEnd + 1));
		else
			bOnlyDomain = TRUE;
	}

	if(bOnlyDomain)
		dwRet = ONLYDOMAINWEIGHT;

	return dwRet;
}

void GetStrLine(LPWSTR pBuf, const int nSize, int& nIdx, LPWSTR& lpszLine)
{
	if( !pBuf || nIdx >= nSize )
		return;

	int nStart = nIdx;
	int nEnd = nIdx;
	while( nIdx < nSize )
	{
		TCHAR ch = pBuf[nIdx++];
		if( _T('\r')==ch || _T('\n')==ch )
			break;
		nEnd = nIdx;
	}

	if( nStart!=nEnd && pBuf[nStart] )
	{
		lpszLine = pBuf+nStart;
		*(pBuf+nEnd) = _T('\0');
	}

	if( nIdx < nSize )
	{
		if( _T('\n')==pBuf[nIdx] )
		{
			pBuf[nIdx] = _T('\0');
			nIdx++;
		}
	}
}

void CNavigateQuery::LoadPreset(LPCTSTR lpszData)
{
	if(lpszData && 0 != lpszData[0])
	{
		m_vNavItems[NavQuery_Preset].clear();
		int nSize = _tcslen(lpszData);
		LPTSTR pOutBuffer = new TCHAR[nSize + 1];
		if( !pOutBuffer )
			return;
		_tcscpy( pOutBuffer, lpszData );

		wchar_t* lpszTok = (wchar_t*)lpszData;
		int nIdx = 0;

		while (nIdx < nSize) {
			LPWSTR lpszLine = NULL;
			GetStrLine(pOutBuffer, nSize, nIdx, lpszLine);

			NAVITEM ni;
			ni.strTitle = lpszLine;

			GetStrLine(pOutBuffer, nSize, nIdx, lpszLine);
			ni.strUrl = lpszLine;

			ni.dwWeight = PRESETWEIGHT;
			ni.tAccTime = 0;
			ni.dwProtLen = GetProtoclLen(ni.strUrl.c_str());
			m_vNavItems[NavQuery_Preset].push_back(ni);
		}
	}
//		const wchar_t* sep = _T("\r\n");
//		do
//		{
//			wchar_t* last = NULL;
//			lpszTok = wcstok(lpszTok, sep, &last);
//			if(!lpszTok)
//				break;
////			LOGD("lpszTok[0]=%c",lpszTok[0]);
//			strItem = lpszTok;
//
//			NAVITEM ni;
//			ni.strTitle = strItem;
//
//			lpszTok = wcstok(lpszTok, sep, &last);
//			if(!lpszTok)
//				break;
//
//			strItem = lpszTok;
//			ni.strUrl = strItem;
//
//			lpszTok = wcstok(lpszTok, sep, &last);
//			if(!lpszTok)
//				break;
//			strItem = lpszTok;
//
//			ni.dwWeight = PRESETWEIGHT;
//
//			ni.tAccTime = 0;
//			ni.dwProtLen = GetProtoclLen(ni.strUrl.c_str());
//
//			m_vNavItems[NavQuery_Preset].push_back(ni);
//			LOGD("NavQuery_Preset push_back");
//		} while (1);

//	LOGD("NavQuery_Preset.size=%d", m_vNavItems[NavQuery_Preset].size());

//	LOGD("NavQuery_Preset success");
	m_bLoaded[NavQuery_Preset] = TRUE;
}

wstring utf8strTowstr(std::string utf8str)
{
	int bufLen = sizeof(wchar_t) * utf8str.size();
	wchar_t *pUniBuf = new wchar_t[bufLen];
	memset(pUniBuf, 0, bufLen * sizeof(wchar_t));

	int unicodeSize = UTF82Unicode(utf8str.c_str(), pUniBuf, utf8str.length());
	wstring wstr = pUniBuf;
	delete(pUniBuf);

	return wstr;
}

string unicodeToUtf8(std::wstring unicodeStr) {
    // unicode编码，对应的utf8最大字符数为6个
	int bufLen = (unicodeStr.length()+2) * 6;
	char * pUtf8Buf = new char[bufLen];
	memset(pUtf8Buf, 0, bufLen * sizeof(char));

	Unicode2UTF8(unicodeStr.c_str(), pUtf8Buf, unicodeStr.length());
	string utf8str = pUtf8Buf;
	delete(pUtf8Buf);

	return utf8str;
}

void CNavigateQuery::LoadHistory(LPCSTR lpszData)
{
	//加载历史记录json
	if(lpszData && lpszData[0])
	{
		/*
		[
			   {
				  "id": 4,
				  "title": "短短 - 百度",
				  "src": 0,
				  "ts": "2014-10-27 18:40:50",
				  "url": "http%3A%2F%2Fwww.baidu.com%2Fs%3Fwd%3D%25E7%259F%25AD%25E7%259F%25AD"
			   },
			   {
				  "id": 3,
				  "title": "短短 - 散文网",
				  "src": 0,
				  "ts": "2014-10-27 18:40:46",
				  "url": "http%3A%2F%2Fm.sanwen.net%2Fsubject%2F87914%2F"
			   },
			   {
				  "id": 1,
				  "title": "百度贴吧",
				  "src": 0,
				  "ts": "2014-10-27 18:39:20",
				  "url": "http%3A%2F%2Ftieba.baidu.com%2F"
			   }
		]
		 */

		Json::Reader reader;
		Json::Value root;
		string strDoc = lpszData;
		reader.parse(strDoc, root);
//		LOGD("parse history json success!");

//		LOGD("root.size(): %d", root.size());
		for (int i = 0; i < root.size(); ++ i )
		{
			string utf8url = root[i].get(HISTORY_JSON_NODE_url, "").asString();
//			LOGD("utf8url: %s", utf8url.c_str());

			wstring url = utf8strTowstr(utf8url);

			if(StrCmpNI(url.c_str(), _T("res://"), 6) != 0 && StrCmpNI(url.c_str(), _T("file://"), 7) != 0)
			{
				NAVITEM ni;
				string utf8title = root[i].get(HISTORY_JSON_NODE_title, "").asString();
				ni.strTitle = utf8strTowstr(utf8title);
				ni.strUrl = url;
				ni.dwProtLen = GetProtoclLen(ni.strUrl.c_str());
				ni.tAccTime = root[i].get(HISTORY_JSON_NODE_ts, 0).asInt64();
				ni.nSrcType = root[i].get(HISTORY_JSON_NODE_src, 0).asInt();
				ni.dwWeight = GetWeightBySrc(ni.nSrcType) + GetWeightByUrl(ni.strUrl.c_str(), ni.dwProtLen);

				tagNavUrlIndexMap::iterator iterNavUrlIndex = m_mpHistoryIndex.find(ni.strUrl);
				if(m_mpHistoryIndex.end() == iterNavUrlIndex)
				{
					m_vNavItems[NavQuery_History].push_back(ni);
					m_mpHistoryIndex[ni.strUrl] = m_vNavItems[NavQuery_History].size() - 1;
				}
				else
				{
					NAVITEM& exItem = m_vNavItems[NavQuery_History].at(iterNavUrlIndex->second);
					exItem.dwWeight += ni.dwWeight;
					if(exItem.tAccTime < ni.tAccTime)
					{
						exItem.tAccTime = ni.tAccTime;
						exItem.strTitle = ni.strTitle;
					}
					if(GetWeightBySrc(ni.nSrcType) == FROMADDRBOXWEIGHT)
						exItem.nSrcType = ni.nSrcType;
				}
			}
		}

	}

	m_bLoaded[NavQuery_History] = TRUE;
//	LOGD("init history success!");
}

void CNavigateQuery::LoadBookmark(LPCSTR lpszData)
{
	//加载收藏数据json
	if(lpszData && lpszData[0])
	{
		 /*
		 [
			{
			   "type": "url",
			   "id": 2,
			   "url": "http:\/\/m.sohu.com\/",
			   "name": "搜狐网"
			},
			{
			   "type": "url",
			   "id": 3,
			   "url": "http:\/\/sina.cn\/",
			   "name": "手机新浪网"
			},
			{
			   "type": "url",
			   "id": 4,
			   "url": "http:\/\/novel.baidu.com\/",
			   "name": "百度文学-百读不厌 百度旗下数字阅读平台"
			}
		 ],
		 */
		Json::Reader reader;
		Json::Value root;
		string strDoc = lpszData;
		reader.parse(strDoc, root);

//		LOGD("parse bookmark json success!");
//
//		LOGD("root.size(): %d", root.size());
		for (int i = 0; i < root.size(); ++ i )
		{
			string name = root[i].get(BOOKMARK_JSON_NODE_name, "").asString();
			string url = root[i].get(BOOKMARK_JSON_NODE_url, "").asString();

//			LOGD("name: %s", name.c_str());
//			LOGD("url: %s", url.c_str());

			NAVITEM ni;
			ni.strTitle = utf8strTowstr(name);
			ni.strUrl = utf8strTowstr(url);
			ni.dwProtLen = GetProtoclLen(ni.strUrl.c_str());
			ni.dwWeight = BOOKMARKWEIGHT + GetWeightByUrl(ni.strUrl.c_str(), ni.dwProtLen);

			m_vNavItems[NavQuery_Bookmark].push_back(ni);
		}
	}
//	LOGD("init bookmark success!");

	m_bLoaded[NavQuery_Bookmark] = TRUE;
}

CNavigateQuery::AUTOCOMPLETEITEM _acItem;

bool CNavigateQuery::NavRetVecGreater(RETITEM item1, RETITEM item2)
{
	if(_acItem.bValid && item1.nType == _acItem.nType && item1.lpszUrl == _acItem.lpszUrl)
		return true;

	if(_acItem.bValid && item2.nType == _acItem.nType && item2.lpszUrl == _acItem.lpszUrl)
		return false;

	return (item1.dwWeight + item1.dwDynWeight) > (item2.dwWeight + item2.dwDynWeight);
}

BOOL MatchNavKeys(LPCTSTR lpszTitle, LPCTSTR lpszUrl, std::vector<wstring>& vKeys, LPTSTR& lpszUrlOffset)
{
//	LOGD("MatchNavKeys() called");

	BOOL bRet = TRUE;

	int nCount = vKeys.size();
	for (int i = 0; i < nCount; ++ i)
	{
		if(1 == nCount) {
			bRet = ((lpszUrlOffset = StrStrI(lpszUrl, vKeys[i].c_str())) || StrStrI(lpszTitle, vKeys[i].c_str()));
		}
		else
			bRet = bRet && (StrStrI(lpszUrl, vKeys[i].c_str()) || StrStrI(lpszTitle, vKeys[i].c_str()));
	}

//	if (bRet)
//		LOGD("Match!");
//	else
//		LOGD("not match!");

	return bRet;
}

//void SetHilightKeys(const std::vector<wstring>& vKeys, LPCTSTR lpszTitle, LPCTSTR lpszUrl,
//					LPDWORD lpNavTitleHilightPos, DWORD dwTitleHilightSize,
//					LPDWORD lpNavUrlHilightPos, DWORD dwUrlHilightSize)
//{
//	int nCount = vKeys.size();
//	LPTSTR lpszTitleOffset = NULL;
//	LPTSTR lpszUrlOffset = NULL;
//	int nTitleCount = 0;
//	int nUrlCount = 0;
//
//	for (int i = 0; i < nCount; ++ i)
//	{
//		DWORD dwKeyLen = vKeys[i].length();
//		lpszTitleOffset = (LPTSTR)lpszTitle;
//		while(nTitleCount < dwTitleHilightSize && (lpszTitleOffset = StrStrI(lpszTitleOffset, vKeys[i].c_str())))
//		{
//			lpNavTitleHilightPos[nTitleCount] = MAKELONG(dwKeyLen, lpszTitleOffset - lpszTitle);
//			++ nTitleCount;
//			lpszTitleOffset += dwKeyLen;
//
//		}
//
//		lpszUrlOffset = (LPTSTR)lpszUrl;
//		while(nUrlCount < dwUrlHilightSize && (lpszUrlOffset = StrStrI(lpszUrlOffset, vKeys[i].c_str())))
//		{
//			lpNavUrlHilightPos[nUrlCount] = MAKELONG(dwKeyLen, lpszUrlOffset - lpszUrl);
//			++ nUrlCount;
//			lpszUrlOffset += dwKeyLen;
//		}
//	}
//}



std::string CNavigateQuery::Input(LPCTSTR lpszKey)
{
	std::string result;

	BOOL bKeepSearch = FALSE;
	if(lpszKey && lpszKey[0])
	{
		wstring strKey = lpszKey;

		// TODO： 讲所有句号替换为
//		strKey.replace(_T('。'), _T('.'));
		int nProtFind = strKey.find(_T("://"));
		if(-1 != nProtFind)
			strKey.erase(0, nProtFind + 3);

		memset(&_acItem, 0, sizeof(_acItem));
		_acItem.nType = NavQuery_All;
		_acItem.bValid = FALSE;

		BOOL bCanAutoComplete = FALSE;

		if(m_szLastKey[0] && StrCmpN(strKey.c_str(), m_szLastKey, wcslen(m_szLastKey)) == 0)
			bKeepSearch = TRUE;

		wcsncpy(m_szLastKey, strKey.c_str(), SIZEOF(m_szLastKey) - 1);

		//////////////////////////////////////////////////////////////////////////
		// get multi keywords
		std::vector<wstring> vKeys;
		LPCTSTR lpszTempKW = strKey.c_str();
		LPTSTR lpszSep = NULL;
		TCHAR szKey[MAX_URL_LEN] = {0};
		while((lpszSep = StrChrI(lpszTempKW, _T(' '))))
		{
			if(lpszSep > lpszTempKW)
			{
				wcsncpy(szKey, lpszTempKW, lpszSep - lpszTempKW);
				vKeys.push_back(szKey);
			}
			lpszTempKW = lpszSep + 1;
		}
		if(vKeys.size() == 0) {
//			LOGD("vKeys.size() == 0");
			vKeys.push_back(strKey);
		}
		else if(lpszTempKW[0]) {
//			LOGD("vKeys.size() != 0");
			vKeys.push_back(lpszTempKW);
		}

		//////////////////////////////////////////////////////////////////////////
		if(!bKeepSearch)
		{
//			LOGD("!bKeepSearch");
			m_vRetItems.clear();

			tagNavItemVec::iterator iter;
			int nIndex = 0;

			//////////////////////////////////////////////////////////////////////////
			// History
			if(m_vNavItems[NavQuery_History].size() > 0)
			{
//				LOGD("History size > 0");
				nIndex = m_vNavItems[NavQuery_History].size() - 1;
				for (; nIndex >= 0; -- nIndex)
				{
					iter = m_vNavItems[NavQuery_History].begin() + nIndex;

					LPTSTR lpszUrlOffset = NULL;
					if(MatchNavKeys(iter->strTitle.c_str(), iter->strUrl.c_str() + iter->dwProtLen, vKeys, lpszUrlOffset))
					{
						RETITEM rItem;
						rItem.nType = NavQuery_History;
						rItem.lpszTitle = iter->strTitle.c_str();
						rItem.lpszUrl = iter->strUrl.c_str();
						rItem.dwProtLen = iter->dwProtLen;
						rItem.tAccTime = iter->tAccTime;
						rItem.nSrcType = iter->nSrcType;
						rItem.dwWeight = iter->dwWeight + GetWeightByTime(iter->tAccTime);
						rItem.dwDynWeight = GetWeightByKeyMatch(lpszKey, iter->strTitle.c_str(), iter->strUrl.c_str() + iter->dwProtLen, lpszUrlOffset, iter->nSrcType, rItem.bCanAC);
						if(rItem.bCanAC && rItem.tAccTime > _acItem.tAccTime)
						{
							_acItem.nType = rItem.nType;
							_acItem.lpszTitle = (LPTSTR)rItem.lpszTitle;
							_acItem.lpszUrl = (LPTSTR)rItem.lpszUrl;
							_acItem.dwProtLen = rItem.dwProtLen;
							_acItem.tAccTime = iter->tAccTime;
							_acItem.bValid = TRUE;
						}

						/*
						tagRetItemVec::iterator iterTmp;
						BOOL bFind = FALSE;
						for (iterTmp = m_vRetItems.begin(); iterTmp != m_vRetItems.end(); ++ iterTmp)
						{
							if(NavQuery_History == iterTmp->nType && StrCmpI(iterTmp->lpszUrl, iter->strUrl) == 0)
							{
								bFind = TRUE;
								iterTmp->dwWeight += rItem.dwWeight;
								break;
							}
						}

						if(!bFind) */
						m_vRetItems.push_back(rItem);
					}
				}
			}
			//////////////////////////////////////////////////////////////////////////
			// Preset
//			LOGD("Preset begin");
			nIndex = 0;
			for (iter = m_vNavItems[NavQuery_Preset].begin(); iter != m_vNavItems[NavQuery_Preset].end(); ++ iter)
			{
				LPTSTR lpszUrlOffset = NULL;
				if(MatchNavKeys(iter->strTitle.c_str(), iter->strUrl.c_str() + iter->dwProtLen, vKeys, lpszUrlOffset))
				{
					RETITEM rItem;
					rItem.nType = NavQuery_Preset;
					rItem.lpszTitle = iter->strTitle.c_str();
					rItem.lpszUrl = iter->strUrl.c_str();
					rItem.dwProtLen = iter->dwProtLen;
					rItem.tAccTime = iter->tAccTime;
					rItem.nSrcType = iter->nSrcType;
					rItem.dwWeight = iter->dwWeight;
					rItem.dwDynWeight = GetWeightByKeyMatch(lpszKey, iter->strTitle.c_str(), iter->strUrl.c_str() + iter->dwProtLen, lpszUrlOffset, iter->nSrcType, bCanAutoComplete);
					m_vRetItems.push_back(rItem);
				}
				++ nIndex;
			}
//			LOGD("Preset end");

			//////////////////////////////////////////////////////////////////////////
			// Bookmark
//			LOGD("bookmark begin");
			nIndex = 0;
			for (iter = m_vNavItems[NavQuery_Bookmark].begin(); iter != m_vNavItems[NavQuery_Bookmark].end(); ++ iter)
			{
				LPTSTR lpszUrlOffset = NULL;
				if(MatchNavKeys(iter->strTitle.c_str(), iter->strUrl.c_str() + iter->dwProtLen, vKeys, lpszUrlOffset))
				{
					RETITEM rItem;
					rItem.nType = NavQuery_Bookmark;
					rItem.lpszTitle = iter->strTitle.c_str();
					rItem.lpszUrl = iter->strUrl.c_str();
					rItem.dwProtLen = iter->dwProtLen;
					rItem.tAccTime = iter->tAccTime;
					rItem.nSrcType = iter->nSrcType;
					rItem.dwWeight = iter->dwWeight;
					rItem.dwDynWeight = GetWeightByKeyMatch(lpszKey, iter->strTitle.c_str(), iter->strUrl.c_str() + iter->dwProtLen, lpszUrlOffset, iter->nSrcType, bCanAutoComplete);
					m_vRetItems.push_back(rItem);
				}
				++ nIndex;
			}
//			LOGD("bookmark end");
		}
		else
		{
//			LOGD("bKeepSearch");
			tagRetItemVec vTempRetItems;
			vTempRetItems.assign(m_vRetItems.begin(), m_vRetItems.end());
			m_vRetItems.clear();

			tagRetItemVec::iterator iter;
			for (iter = vTempRetItems.begin(); iter != vTempRetItems.end(); ++ iter)
			{
				LPTSTR lpszUrlOffset = NULL;
				if(MatchNavKeys(iter->lpszTitle, iter->lpszUrl + iter->dwProtLen, vKeys, lpszUrlOffset))
				{
					iter->dwDynWeight = GetWeightByKeyMatch(lpszKey, iter->lpszTitle, iter->lpszUrl + iter->dwProtLen, lpszUrlOffset, iter->nSrcType, iter->bCanAC);
					if(iter->bCanAC && iter->tAccTime > _acItem.tAccTime)
					{
						_acItem.nType = iter->nType;
						_acItem.lpszTitle = (LPTSTR)iter->lpszTitle;
						_acItem.lpszUrl = (LPTSTR)iter->lpszUrl;
						_acItem.dwProtLen = iter->dwProtLen;
						_acItem.tAccTime = iter->tAccTime;
						_acItem.bValid = TRUE;
					}
					m_vRetItems.push_back((*iter));
				}
			}
		}
//		LOGD("sort begin");
		sort(m_vRetItems.begin(), m_vRetItems.end(), CNavigateQuery::NavRetVecGreater);
//		LOGD("sort end");

		int nCount = m_vRetItems.size();
//		LOGD("return count = %d", nCount);
		if(nCount > 0)
		{
			if(m_vRetItems[0].bCanAC && m_vRetItems[0].dwWeight + m_vRetItems[0].dwDynWeight < CANACWEIGHT && GetWeightByUrl(m_vRetItems[0].lpszUrl, m_vRetItems[0].dwProtLen) == 0)
			{
				m_vRetItems[0].bCanAC = FALSE;
				memset(&_acItem, 0, sizeof(_acItem));

				tagRetItemVec::iterator iter;
				int n = 0;
				for (int i = 0; i < nCount; ++ i)
				{
					iter = m_vRetItems.begin() + i;
					if(iter->bCanAC && iter->tAccTime > _acItem.tAccTime && (
						iter->dwWeight + iter->dwDynWeight >= CANACWEIGHT || GetWeightByUrl(iter->lpszUrl, iter->dwProtLen) > 0))
					{
						_acItem.nType = iter->nType;
						_acItem.lpszTitle = (LPTSTR)iter->lpszTitle;
						_acItem.lpszUrl = (LPTSTR)iter->lpszUrl;
						_acItem.dwProtLen = iter->dwProtLen;
						_acItem.tAccTime = iter->tAccTime;
						_acItem.bValid = TRUE;
						n = i;
					}
					if(i == 100)
						break;
				}

				if(_acItem.bValid)
				{
					RETITEM rItem;
					rItem.nType = m_vRetItems[n].nType;
					rItem.lpszTitle = m_vRetItems[n].lpszTitle;
					rItem.lpszUrl = m_vRetItems[n].lpszUrl;
					rItem.dwProtLen = m_vRetItems[n].dwProtLen;
					rItem.tAccTime = m_vRetItems[n].tAccTime;
					rItem.nSrcType = m_vRetItems[n].nSrcType;
					rItem.dwWeight = m_vRetItems[n].dwWeight;
					rItem.dwDynWeight = m_vRetItems[n].dwDynWeight;
					rItem.bCanAC = m_vRetItems[n].bCanAC;
					m_vRetItems.erase(m_vRetItems.begin() + n);
					m_vRetItems.insert(m_vRetItems.begin(), rItem);
					
				}
			}
		}

		//////////////////////////////////////////////////////////////////////////
		// generate results
//		LOGD("generate result begin");
		LPNAVINPUTITEM pRetNavInput = new NAVINPUTITEM[m_nMaxRetCount];
		int nRetCount = 0;
		pRetNavInput[0].strKey = lpszKey;
		Json::FastWriter writer;
		Json::Value root;

		for (int i = 0; i < nCount; ++ i)
		{
			BOOL bFind = FALSE;
			for (int j = 0; j < nRetCount; ++ j)
			{
				int nLen = MIN(wcslen(m_vRetItems[i].lpszUrl) - m_vRetItems[i].dwProtLen, pRetNavInput[j].strUrl.length() - pRetNavInput[j].dwProtLen);
				if(StrCmpNI((m_vRetItems[i].lpszUrl + m_vRetItems[i].dwProtLen), pRetNavInput[j].strUrl.c_str() + pRetNavInput[j].dwProtLen, nLen) == 0)
				{
					wstring a = m_vRetItems[i].lpszUrl + m_vRetItems[i].dwProtLen;
					string utf8a = unicodeToUtf8(a);
//					LOGD("arg1=%s", utf8a.c_str());

					wstring b = pRetNavInput[j].strUrl.c_str() + pRetNavInput[j].dwProtLen;
					string utf8b = unicodeToUtf8(b);
//					LOGD("arg2=%s", utf8b.c_str());
//
//					LOGD("arg3=%d", nLen);
//
//					LOGD("m_vRetItems[i].dwProtLen=%d", m_vRetItems[i].dwProtLen);


					LPCTSTR lpszEnd1 = m_vRetItems[i].lpszUrl + m_vRetItems[i].dwProtLen + nLen;
					LPCTSTR lpszEnd2 = pRetNavInput[j].strUrl.c_str() + pRetNavInput[j].dwProtLen + nLen;
					
					if((0 == lpszEnd1[0] || StrCmp(lpszEnd1, _T("/")) == 0) && (0 == lpszEnd2[0] || StrCmp(lpszEnd2, _T("/")) == 0))
					{
						pRetNavInput[j].dwWeight += m_vRetItems[i].dwWeight;
						bFind = TRUE;
						break;
					}
				}
			}

//			LOGD("m_vRetItems[%d], url=%s", i, unicodeToUtf8(m_vRetItems[i].lpszUrl).c_str());

			if(bFind) {
//				LOGD("m_vRetItems[%d], url=%s", i, unicodeToUtf8(m_vRetItems[i].lpszUrl).c_str());
//				LOGD("pRetNavInput[%d], url=%s", i, unicodeToUtf8(pRetNavInput[j].strUrl).c_str());
				continue;
			}

			pRetNavInput[nRetCount].nType = m_vRetItems[i].nType;
			pRetNavInput[nRetCount].strTitle = m_vRetItems[i].lpszTitle;
			pRetNavInput[nRetCount].strUrl = m_vRetItems[i].lpszUrl;
			pRetNavInput[nRetCount].dwProtLen = m_vRetItems[i].dwProtLen;
			pRetNavInput[nRetCount].dwWeight = m_vRetItems[i].dwWeight + m_vRetItems[i].dwDynWeight;
			pRetNavInput[nRetCount].bCanAC = m_vRetItems[i].bCanAC;

			Json::Value retItem;
			retItem["title"] = unicodeToUtf8(m_vRetItems[i].lpszTitle);
			retItem["url"] = unicodeToUtf8(m_vRetItems[i].lpszUrl);
			root.append(retItem);

//			SetHilightKeys(vKeys, pRetNavInput[nRetCount].strTitle.c_str(), pRetNavInput[nRetCount].strUrl.c_str(),
//				pRetNavInput[nRetCount].arrTitleHilight, SIZEOF(pRetNavInput[nRetCount].arrTitleHilight),
//				pRetNavInput[nRetCount].arrUrlHilight, SIZEOF(pRetNavInput[nRetCount].arrUrlHilight));
			++ nRetCount;

			if(nRetCount == m_nMaxRetCount)
				break;
		}
//		LOGD("generate result end");

		// 生成结果json并返回。
		if (root.size() > 0) {
			result = writer.write(root);
//			LOGD("result=%s", result.c_str());
		}
	}
	return result;
}


void CNavigateQuery::AddItem(UINT nType, LPCTSTR lpszTitle, LPCTSTR lpszUrl, UINT nNavType, time_t tAccTime)
{
	if(nType >= NavQuery_All || !lpszUrl || !lpszUrl[0])
		return ;
	if(!lpszTitle)
		lpszTitle = _T("");
	
	NAVITEM ni;
	ni.strTitle = lpszTitle;
	ni.strUrl = lpszUrl;
	ni.dwProtLen = GetProtoclLen(ni.strUrl.c_str());
	ni.nSrcType = nNavType;
	ni.tAccTime = tAccTime;

	switch (nType)
	{
	case NavQuery_Preset:
		ni.dwWeight = PRESETWEIGHT;
		break;

	case NavQuery_History:
		ni.dwWeight = GetWeightBySrc(ni.nSrcType) + GetWeightByUrl(ni.strUrl.c_str(), ni.dwProtLen);
		break;

	case NavQuery_Bookmark:
		ni.dwWeight = BOOKMARKWEIGHT;
		break;
	}

	if(NavQuery_History == nType)
	{
		tagNavUrlIndexMap::iterator iterNavUrlIndex = m_mpHistoryIndex.find(ni.strUrl);
		if(m_mpHistoryIndex.end() != iterNavUrlIndex)
		{
			NAVITEM& exItem = m_vNavItems[nType].at(iterNavUrlIndex->second);
			exItem.dwWeight += ni.dwWeight;
			exItem.strTitle = ni.strTitle;
			exItem.tAccTime = ni.tAccTime;
			if(GetWeightBySrc(ni.nSrcType) == FROMADDRBOXWEIGHT)
				exItem.nSrcType = ni.nSrcType;
		}
		else
		{
			m_vNavItems[nType].push_back(ni);
			m_mpHistoryIndex[ni.strUrl] = m_vNavItems[NavQuery_History].size() - 1;
		}
	}
	else
		m_vNavItems[nType].push_back(ni);
	memset(m_szLastKey, 0, sizeof(m_szLastKey));
}


