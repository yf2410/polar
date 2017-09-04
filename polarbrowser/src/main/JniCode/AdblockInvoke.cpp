#include "AdblockPlus.h"
#include "StringConvertor.h"
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

extern "C" {
	void AdblockInitialize(LPCTSTR lpszRaw, LPCTSTR lpszWin, LPCTSTR lpszEpt);
	bool AdblockMatchUrlByRaw(LPCTSTR lpszDocUrl, LPCTSTR lpszDocHost, LPCTSTR lpszUrl, LPCTSTR lpszUrlHost);
	std::string AdblockMatchCssByAll(LPCTSTR lpszDocUrl, LPCTSTR lpszDocHost);
	void AdblockMatchCssFree(char* lpszCss);
}

//wstring Utf8ToWide(const char* lpszInput)
//{
//	wstring strWide = L"";
//	if( !lpszInput || !lpszInput[0] )
//		return strWide;
//
//	setlocale( LC_CTYPE, "UTF-8" );
//
//	size_t chLength = mbstowcs( NULL, lpszInput, 0 );
//	if( chLength && chLength!=-1 )
//	{
//		int nBuffer = (chLength + 1)*sizeof(wchar_t);
//		wchar_t* szBuffer = (wchar_t*)malloc( nBuffer );
//		if( szBuffer )
//		{
//			memset( szBuffer, 0, nBuffer );
//			mbstowcs( szBuffer, lpszInput, chLength );
//			szBuffer[chLength] = L'\0';
//			strWide = szBuffer;
//			free( szBuffer );
//		}
//	}
//
//	return strWide;
//}

void AdblockInitialize(LPCTSTR lpszRaw, LPCTSTR lpszWin, LPCTSTR lpszEpt)
{
//	LOGD("AdblockInitialize");
	CAdblockPlus* adblockPlush = CAdblockPlus::GetInstance();
	if( adblockPlush )
	{
//		wstring strRaw( Utf8ToWide(lpszRaw) );
//		wstring strWin( Utf8ToWide(lpszWin) );
//		wstring strEpt( Utf8ToWide(lpszEpt) );
		adblockPlush->Initialize(lpszRaw, lpszWin, 0);
	}
}

UINT AdblockMatchUrl(UINT uType, LPCTSTR lpszDocUrl, LPCTSTR lpszDocHost, LPCTSTR lpszUrl, LPCTSTR lpszUrlHost)
{
	UINT uRet = ADBLOCK_TYPE_NONE;
	if( !CAdblockPlus::HasInited() || !lpszDocUrl || !lpszDocUrl[0] || !lpszDocHost || !lpszDocHost[0] || !lpszUrl || !lpszUrl[0] || !lpszUrlHost || !lpszUrlHost[0] )
		return uRet;

	CAdblockPlus* adblockPlush = CAdblockPlus::GetInstance();
	if( adblockPlush )
	{
		UrlParam urlParam;
		urlParam.strUrl = lpszUrl;
		urlParam.strUrlHost = lpszUrlHost;
		urlParam.strDocUrl = lpszDocUrl;
		urlParam.strDocHost = lpszDocHost;
		urlParam.urlType = adblockPlush->GetUrlTypeByUrl( urlParam.strUrl.c_str() );

		uRet = adblockPlush->MatchUrl( urlParam, uType );
	}

//	LOGD("AdblockMatchUrl: uRet=%d", uRet);

	return uRet;
}

bool AdblockMatchUrlByRaw(LPCTSTR lpszDocUrl, LPCTSTR lpszDocHost, LPCTSTR lpszUrl, LPCTSTR lpszUrlHost)
{
	UINT uRet = AdblockMatchUrl( ADBLOCK_TYPE_RAW, lpszDocUrl, lpszDocHost, lpszUrl, lpszUrlHost );

	return uRet!=ADBLOCK_TYPE_NONE;
}

string wstringToUtf8(std::wstring unicodeStr) {
    // unicode编码，对应的utf8最大字符数为6个
	int bufLen = (unicodeStr.length()+2) * 6;
	char * pUtf8Buf = new char[bufLen];
	memset(pUtf8Buf, 0, bufLen * sizeof(char));

	Unicode2UTF8(unicodeStr.c_str(), pUtf8Buf, unicodeStr.length());
	string utf8str = pUtf8Buf;
	delete(pUtf8Buf);

	return utf8str;
}

std::string AdblockMatchCssByAll(LPCTSTR lpszDocUrl, LPCTSTR lpszDocHost)
{

//	LOGD("AdblockMatchCssByAll");
	std::string strCss;

	if( !CAdblockPlus::HasInited() || !lpszDocUrl || !lpszDocUrl[0] || !lpszDocHost || !lpszDocHost[0] ) {
		return strCss;
	}

	CAdblockPlus* adblockPlush = CAdblockPlus::GetInstance();
	if( adblockPlush )
	{
		VString vecCss;
//		LOGD("MatchCss");
		if( adblockPlush->MatchCss(lpszDocUrl, lpszDocHost, vecCss, TRUE) )
		{
//			LOGD("MatchCss Success");
			strCss = wstringToUtf8( vecCss[0].c_str() );
//			LOGD("MatchCss:%s", strCss.c_str());
//			szCss = (char*)malloc( strCss.size() + 1 );
//			if( szCss )
//			{
//				memset( szCss, 0, strCss.size() + 1 );
//				memcpy( szCss, strCss.c_str(), strCss.size() );
//			}
		}
	}

	return strCss;
}
