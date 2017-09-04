#include "AdblockPlus.h"

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

typedef struct tagADPRuleFile{
	LPCTSTR szBuffer;
	UINT uType;
}ADPRuleFile;

static ADPRuleFile _ADPRuleFile[] = {
	NULL,			ADBLOCK_TYPE_RAW,		//adbraw.dat
	NULL,			ADBLOCK_TYPE_WIN,		//adbwin.dat
	NULL,			ADBLOCK_TYPE_FULL		//adbept.dat
};

#define ADB_STYLE_CSS	_T("{display:none!important;}\t")
#define ADB_STYLE_SEPCH	_T('\t')
#define ADB_STYLE_SEPST	_T("\t")
#define MAX_STYLE_RULE	4095*100	//dummy
#define MIN_URLKEYLEN	4
#define MAX_DOC_URL		200

#ifndef SIZEOF
#define SIZEOF(A) (sizeof(A)/sizeof((A)[0]))
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


BOOL CAdblockPlus::m_bAdblockInited = FALSE;

CAdblockPlus::CAdblockPlus()
{
	m_bInit = FALSE;
	m_bInitAdb = FALSE;
}

CAdblockPlus::~CAdblockPlus()
{
}

CAdblockPlus* CAdblockPlus::GetInstance()
{
	static CAdblockPlus* p_This = NULL;
	if( !p_This )
		p_This = new CAdblockPlus();
	return p_This;
}

void CAdblockPlus::Initialize(LPCTSTR lpszRaw, LPCTSTR lpszWin, LPCTSTR lpszEpt)
{
	_ADPRuleFile[0].szBuffer = lpszRaw;
	_ADPRuleFile[1].szBuffer = lpszWin;
	_ADPRuleFile[2].szBuffer = lpszEpt;

	OnParse();
}

void CAdblockPlus::OnParse()
{
//	LOGD("OnParse");
	BOOL bRetAdb = FALSE;

	VCTStr vecCssList;
	for( unsigned int idx = 0; idx < SIZEOF(_ADPRuleFile); idx++ )
	{
		LPCTSTR szBuffer = _ADPRuleFile[idx].szBuffer;
		if( szBuffer && szBuffer[0] )
		{
			LPTSTR pAdbBuf = NULL;
			VCTStr vecUrlList;
			InitRawList( szBuffer, pAdbBuf, vecUrlList, vecCssList );
			if( vecUrlList.size() )
			{
				InitUrlRule( vecUrlList, _ADPRuleFile[idx].uType );
				bRetAdb = TRUE;
			}
		}
	}
	if( vecCssList.size() )
	{
		InitCssRule( vecCssList );
		bRetAdb = TRUE;
	}

	m_bInitAdb = bRetAdb;
	m_bInit = bRetAdb;

//	if (m_bInit) {
//		LOGD("Init success!");
//	} else {
//		LOGD("Init failed!");
//	}

	m_bAdblockInited = m_bInit;
}

UINT CAdblockPlus::MatchUrl(const UrlParam& urlParam, UINT uType)
{
	UINT uRet = ADBLOCK_TYPE_NONE;
	LPCTSTR lpszDocUrl = urlParam.strDocUrl.c_str();
	LPCTSTR lpszUrl = urlParam.strUrl.c_str();
	if( !m_bInit || !m_bInitAdb || !lpszDocUrl || !lpszDocUrl[0] || !lpszUrl || !lpszUrl[0] )
		return uRet;

	vector<MapUrlRuleIter> vecMIter;
	MatchUrlMetaRule( m_MapUrlMRule, lpszUrl, vecMIter );
	for( unsigned int idx = 0; idx < vecMIter.size(); idx++ )
	{
		for( unsigned int idxr = 0; idxr < vecMIter[idx]->second.size(); idxr++ )
		{
			if( !(vecMIter[idx]->second[idxr].uType & uType) )
				continue;
			if( MatchUrlRule(vecMIter[idx]->second[idxr].szValue, lpszDocUrl, lpszUrl, FALSE, urlParam) )
			{
				uRet = vecMIter[idx]->second[idxr].uType & uType;
				break;
			}
		}
		if( uRet!=ADBLOCK_TYPE_NONE )
			break;
	}

	if( uRet!=ADBLOCK_TYPE_NONE )
	{
		vector<MapUrlRuleIter> vecUIter;
		MatchUrlMetaRule( m_MapUrlURule, lpszUrl, vecUIter );
		for( unsigned int idx = 0; idx < vecUIter.size(); idx++ )
		{
			for( unsigned int idxr = 0; idxr < vecUIter[idx]->second.size(); idxr++ )
			{
				if( !(vecUIter[idx]->second[idxr].uType & uType) )
					continue;
				if( MatchUrlRule(vecUIter[idx]->second[idxr].szValue, lpszDocUrl, lpszUrl, TRUE, urlParam) )
				{
					uRet = ADBLOCK_TYPE_NONE;
					break;
				}
			}
			if( ADBLOCK_TYPE_NONE==uRet )
				break;
		}
	}

	if( uRet!=ADBLOCK_TYPE_NONE )
	{
		if( MatchUrlDocRule(lpszDocUrl, FALSE, uType) )
			uRet = ADBLOCK_TYPE_NONE;
	}

	return uRet;
}

BOOL CAdblockPlus::MatchUrlDocRule(LPCTSTR lpszDocUrl, BOOL bCssCheck, UINT uType)
{
	BOOL bRet = FALSE;
	if( !m_bInit || !m_bInitAdb || !lpszDocUrl || !lpszDocUrl[0] )
		return bRet;

	ADBDocUrl adbDocUrl = {ADBLOCK_TYPE_NONE, ADBLOCK_TYPE_NONE};

	vector<MapUrlRuleIter> vecUIter;
	MatchUrlMetaRule( m_MapUrlURule, lpszDocUrl, vecUIter );
	for( unsigned int idx = 0; idx < vecUIter.size(); idx++ )
	{
		for( unsigned int idxr = 0; idxr < vecUIter[idx]->second.size(); idxr++ )
		{
			vector<wstring> vecRule;
			GetSplitStr( vecUIter[idx]->second[idxr].szValue, _T("$"), vecRule );
			if( vecRule.size()!=2 )
				continue;

			vector<wstring> vecOpt;
			GetSplitStr( vecRule[1].c_str(), _T(","), vecOpt );
			if( vecOpt.size()==0 )
				continue;

			UINT idxOpt = 0;
			BOOL bInvert = FALSE;
			BOOL bDocExist = IsOptionExist(vecOpt, _T("document"), idxOpt, bInvert) && !bInvert;
			BOOL bCssExist = IsOptionExist(vecOpt, _T("elemhide"), idxOpt, bInvert) && !bInvert;
			if( !bDocExist && !bCssExist )
				continue;

			if( MatchUrlRegular(vecRule[0].c_str(), lpszDocUrl) )
			{
				if( bDocExist )
					adbDocUrl.uDocType |= vecUIter[idx]->second[idxr].uType;
				if( bCssExist )
					adbDocUrl.uEleType |= vecUIter[idx]->second[idxr].uType;
			}
		}
	}

	if( bCssCheck )
		bRet = (adbDocUrl.uDocType & uType) || (adbDocUrl.uEleType & uType);
	else
		bRet = adbDocUrl.uDocType & uType;

	return bRet;
}

BOOL CAdblockPlus::MatchUrlMetaRule(MapUrlRule& mapUrlRule, LPCTSTR lpszUrl, vector<MapUrlRuleIter>& vecIter)
{
	BOOL bRet = FALSE;
	if( !mapUrlRule.size() || !lpszUrl || !lpszUrl[0] )
		return bRet;

	unsigned int nUrlLen = _tcslen( lpszUrl );
	if( nUrlLen < MIN_URLKEYLEN )
		return bRet;

	for( unsigned int idx = 0; idx <= nUrlLen - MIN_URLKEYLEN; idx++ )
	{
		AD_NKEY strKey(lpszUrl + idx, false);
		MapUrlRuleIter iter = mapUrlRule.find( strKey );
		if( iter!=mapUrlRule.end() )
		{
			vecIter.push_back( iter );
			bRet = TRUE;
		}
	}

	return bRet;
}

BOOL CAdblockPlus::MatchUrlRule(LPCTSTR lpszRule, LPCTSTR lpszDocUrl, LPCTSTR lpszUrl, BOOL bWhiteList, const UrlParam& urlParam)
{
	BOOL bRet =  FALSE;
	if( !lpszRule || !lpszRule[0] || !lpszUrl || !lpszUrl[0] )
		return bRet;

	vector<wstring> vecRule;
	GetSplitStr( lpszRule, _T("$"), vecRule );
	if( vecRule.size()==0 || vecRule.size()>2 )
		return bRet;

	if( vecRule.size()==1 && UrlType_Popup==urlParam.urlType )
		return bRet;

	if( MatchUrlRegular(vecRule[0].c_str(), lpszUrl) )
	{
		if( vecRule.size()==1 || MatchUrlOption(vecRule[1].c_str(), lpszDocUrl, lpszUrl, bWhiteList, urlParam) )
			bRet = TRUE;
	}

	return bRet;
}

BOOL CAdblockPlus::MatchUrlRegular(LPCTSTR lpszReg, LPCTSTR lpszUrl)
{
	BOOL bRet = FALSE;
	if( !lpszReg || !lpszReg[0] || !lpszUrl || !lpszUrl[0] )
		return bRet;

	wstring strRegExp;
	if( GetRegularExp(lpszReg, strRegExp) )
	{
		if( IsRegExpMatch(lpszUrl, strRegExp.c_str()) )
			bRet = TRUE;
	}

	return bRet;
}

BOOL CAdblockPlus::GetRegularExp(LPCTSTR lpszReg, wstring& strRegExp)
{
	BOOL bRet = FALSE;
	if( !lpszReg || !lpszReg[0] )
		return bRet;

	int nLen = _tcslen( lpszReg );
	for( int idx = 0; idx < nLen; idx++ )
	{
		TCHAR ch = lpszReg[idx];
		if( idx==0 )
		{
			if( _T('|')==lpszReg[0] )
			{
				if( nLen>=2 && _T('|')==lpszReg[1] )
				{
					strRegExp = _T("^(http://|https://)([^/]+\\.)?");
					idx++;
				}
				else
					strRegExp = _T("^");
				continue;
			}
			else
				strRegExp = _T(".*");
		}
		else if( idx==nLen-1 )
		{
			if( _T('|')==ch )
			{
				strRegExp += _T("$");
				continue;
			}
		}

		if( _T('*')==ch )
			strRegExp += _T(".*");
		else if( _T('^')==ch )
			strRegExp += _T("[^a-zA-Z0-9_\\-\\.%]");
		else if( _tcschr((LPTSTR)_T(".?+\\${}()[]|"), ch) )
		{
			strRegExp += _T("\\");
			strRegExp += ch;
		}
		else
			strRegExp += ch;

		if( idx==nLen-1 )
			strRegExp += _T(".*");
	}

	return strRegExp.size()!=0;
}

BOOL CAdblockPlus::MatchUrlOption(LPCTSTR lpszOpt, LPCTSTR lpszDocUrl, LPCTSTR lpszUrl, BOOL bWhiteList, const UrlParam& urlParam)
{
	//third-party object-subrequest xmlhttprequest script image stylesheet domain
	BOOL bRet = FALSE;
	if( !lpszOpt || !lpszOpt[0] || !lpszDocUrl || !lpszDocUrl[0] || !lpszUrl || !lpszUrl[0])
		return bRet;

	LPCTSTR szUrlHost = urlParam.strUrlHost.c_str();
	if( !szUrlHost[0] )
		return bRet;

	LPCTSTR szDocHost = urlParam.strDocHost.c_str();
	if( !szDocHost[0] )
		return bRet;

	vector<wstring> vecOpt;
	GetSplitStr( lpszOpt, _T(","), vecOpt );
	if( vecOpt.size()==0 )
		return bRet;

	UINT idxOpt = 0;
	BOOL bInvert = FALSE;

	if( IsOptionExist(vecOpt, _T("xmlhttprequest"), idxOpt, bInvert)
		|| IsOptionExist(vecOpt, _T("object"), idxOpt, bInvert)
		|| IsOptionExist(vecOpt, _T("object-subrequest"), idxOpt, bInvert)
		|| IsOptionExist(vecOpt, _T("object_subrequest"), idxOpt, bInvert)
		|| IsOptionExist(vecOpt, _T("subdocument"), idxOpt, bInvert)
		|| IsOptionExist(vecOpt, _T("popup"), idxOpt, bInvert) )
		return bWhiteList;

	if( UrlType_Popup==urlParam.urlType )
	{
		if( !IsOptionExist(vecOpt, _T("popup"), idxOpt, bInvert) || bInvert )
			return bRet;
	}

	if( IsOptionExist(vecOpt, _T("third-party"), idxOpt, bInvert) )
	{
		BOOL bThird = !IsSameDomain(szUrlHost, szDocHost);
		if( (!bInvert && !bThird) || (bInvert && bThird) )
			return bRet;
	}

	if( IsOptionExist(vecOpt, _T("domain"), idxOpt, bInvert) )
	{
		LPCTSTR szDomains = _tcschr( (LPTSTR)vecOpt[idxOpt].c_str(), _T('=') );
		if( szDomains )
		{
			szDomains++;
			vector<wstring> vecDomain;
			GetSplitStr( szDomains, _T("|"), vecDomain );
			BOOL bHasWhite = FALSE;
			BOOL bMatchWhite = FALSE;
			for( unsigned int idx = 0; idx < vecDomain.size(); idx++ )
			{
				BOOL bIvt = _T('~')==vecDomain[idx][0];
				LPCTSTR szHostOpt = vecDomain[idx].c_str() + bIvt;
				BOOL bMatch = IsHostMatch( szDocHost, szHostOpt );
				if( bIvt && bMatch )
					return bRet;
				if( !bIvt )
				{
					bHasWhite = TRUE;
					if( bMatch )
						bMatchWhite = TRUE;
				}
			}
			if( bHasWhite && !bMatchWhite )
				return bRet;
		}
	}

	BOOL bOptFail = FALSE;
	BOOL bOptNIvt = FALSE;
	BOOL bOptMatch = FALSE;
	for( unsigned int idx = 0; !bOptFail && idx < vecOpt.size(); idx++ )
	{
		BOOL bIvt = FALSE;
		UrlType urlType = GetUrlTypeByOption( vecOpt[idx].c_str(), bIvt );
		switch( urlType )
		{
		case UrlType_Script:
		case UrlType_Image:
		case UrlType_StyleSheet:
		case UrlType_Object:
		case UrlType_ObjectSubRequest:
		case UrlType_SubDocument:
		case UrlType_Document:
		case UrlType_Popup:
		case UrlType_XmlHttpRequest:
		case UrlType_ElemHide:
			{
				if( bIvt )
				{
					if( urlType==urlParam.urlType )
						bOptFail = TRUE;
				}
				else
				{
					bOptNIvt = TRUE;
					if( urlType==urlParam.urlType )
						bOptMatch = TRUE;
				}
			}
			break;
		case UrlType_Others:
			break;
		}
	}

	if( !bOptFail && (!bOptNIvt || bOptMatch) )
		bRet = TRUE;

	if( UrlType_Others==urlParam.urlType )
	{
		BOOL bMisType = FALSE;
		if( (bWhiteList && !bRet) || (!bWhiteList && bRet) )
			bMisType = IsOptionExist(vecOpt, _T("script"), idxOpt, bInvert) || IsOptionExist(vecOpt, _T("image"), idxOpt, bInvert) || IsOptionExist(vecOpt, _T("stylesheet"), idxOpt, bInvert);
		if( bMisType )
			bRet = bWhiteList;
	}

	return bRet;
}

BOOL CAdblockPlus::IsOptionExist(const vector<wstring>& vecOpt, LPCTSTR lpszOpt, UINT& idxOpt, BOOL& bInvert)
{
	BOOL bRet = FALSE;
	if( !lpszOpt || !lpszOpt[0] )
		return bRet;

	int nLen = _tcslen( lpszOpt );
	for( unsigned int idx = 0; idx < vecOpt.size(); idx++ )
	{
		LPCTSTR szTmp = vecOpt[idx].c_str();
		BOOL bIvt = szTmp[0]==_T('~');
		if( !_tcsnicmp(szTmp+bIvt, lpszOpt, nLen) && (_T('\0')==szTmp[bIvt+nLen] || _T('=')==szTmp[bIvt+nLen]) )
		{
			idxOpt = idx;
			bInvert = bIvt;
			bRet = TRUE;
			break;
		}
	}

	return bRet;
}

BOOL CAdblockPlus::MatchCss(LPCTSTR lpszUrl, LPCTSTR lpszUrlHost, VString& vecCss, BOOL bJustFirst)
{
	BOOL bRet = FALSE;
	if( !m_bInit || !lpszUrl || !m_bInitAdb )
		return bRet;

	wstring strDestCss = m_strCssMRule;

	LPCTSTR lpszHost = lpszUrlHost;

//	LOGD("CAdblockPlus::MatchCss");
//	LOGD("m_MapCssMRule.size():%d", m_MapCssMRule.size());
	do{
		if( m_MapCssMRule.end()!=m_MapCssMRule.find(lpszHost) )
		{
//			LOGD("m_MapCssMRule match");
			VCssHost& vCssHost = m_MapCssMRule[lpszHost];
			for( unsigned int idxV = 0; idxV < vCssHost.size(); idxV++ )
			{
				BOOL bMatch = TRUE;
				ADBCssHost& aCssHost = vCssHost[idxV];
				for( unsigned int idxA = 0; idxA < aCssHost.vecHost.size(); idxA++ )
				{
					if( IsHostMatch(lpszUrlHost, aCssHost.vecHost[idxA].c_str()) )
					{
						bMatch = FALSE;
						break;
					}
				}
				if( bMatch )
					strDestCss += aCssHost.strRule.c_str();
			}
		}

		if( _T('\0')==*lpszHost )
			break;
		lpszHost = _tcschr( (LPTSTR)lpszHost, _T('.') );
		if( lpszHost )
			lpszHost++;
		else
			lpszHost = _T("");
//		LOGD("enter!!!!");
	}while( TRUE );

	VString vecWhite;
	GetSplitStr( m_strCssURule.c_str(), ADB_STYLE_SEPST, vecWhite );
	for( unsigned int idxW = 0; idxW < vecWhite.size(); idxW++ )
	{
		if( vecWhite[idxW].size() )
		{
			wstring strRule = vecWhite[idxW].c_str();
			strRule += ADB_STYLE_SEPST;
			StringReplace( strDestCss, strRule.c_str(), _T("") );
		}
	}

	lpszHost = lpszUrlHost;
	do{
		if( m_MapCssURule.end()!=m_MapCssURule.find(lpszHost) )
		{
			VCssHost& vCssHost = m_MapCssURule[lpszHost];
			for( unsigned int idxV = 0; idxV < vCssHost.size(); idxV++ )
			{
				BOOL bMatch = TRUE;
				ADBCssHost& aCssHost = vCssHost[idxV];
				for( unsigned int idxA = 0; idxA < aCssHost.vecHost.size(); idxA++ )
				{
					if( IsHostMatch(lpszUrlHost, aCssHost.vecHost[idxA].c_str()) )
					{
						bMatch = FALSE;
						break;
					}
				}
				if( bMatch )
					StringReplace( strDestCss, aCssHost.strRule.c_str(), _T("") );
			}
		}

		if( _T('\0')==*lpszHost )
			break;
		lpszHost = _tcschr( (LPTSTR)lpszHost, _T('.') );
		if( lpszHost )
			lpszHost++;
		else
			lpszHost = _T("");
	}while( TRUE );

	LPCTSTR lpszStart = strDestCss.c_str();
	do{
		if( !lpszStart || !lpszStart[0] )
			break;

		LPCTSTR lpszEnd = lpszStart;
		int nCount = MAX_STYLE_RULE;
		while( nCount-- )
		{
			LPCTSTR lpszSep = _tcschr( (LPTSTR)lpszEnd, ADB_STYLE_SEPCH );
			if( !lpszSep )
				break;
			lpszEnd = lpszSep + 1;
		}

		if( lpszEnd > lpszStart )
			vecCss.push_back( wstring(lpszStart, lpszEnd - 1) );

		lpszStart = lpszEnd;
		if( bJustFirst )
			break;
	}while( TRUE );

	if( vecCss.size() )
	{
		if( MatchUrlDocRule(lpszUrl, TRUE, ADBLOCK_TYPE_FULL) )
			vecCss.clear();
	}

	return vecCss.size();
}

void CAdblockPlus::InitRawList(LPCTSTR lpszBuffer, LPTSTR& pOutBuffer, VCTStr& vecUrlList, VCTStr& vecCssList)
{
	if( !lpszBuffer || !lpszBuffer[0] )
		return;
	
	int nSize = _tcslen(lpszBuffer);
	pOutBuffer = new TCHAR[nSize + 1];
	if( !pOutBuffer )
		return;
	_tcscpy( pOutBuffer, lpszBuffer );

	int nIdx = 0;
	BOOL bFirstLine = TRUE;
	do{
		LPWSTR lpszLine = NULL;
		GetStrLine( pOutBuffer, nSize, nIdx, lpszLine );
		if( lpszLine && lpszLine[0] )
		{
			if( bFirstLine )
			{
				bFirstLine = FALSE;
				continue;
			}

			if( _T('!')==lpszLine[0] )
				continue;

			if( _tcsstr(lpszLine, _T("##")) || _tcsstr(lpszLine, _T("#@#")) )
			{
				vecCssList.push_back( lpszLine );
			}
			else
				vecUrlList.push_back( lpszLine );
		}
	}while( nIdx < nSize );
}

void CAdblockPlus::GetStrLine(LPWSTR pBuf, const int nSize, int& nIdx, LPWSTR& lpszLine)
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

void CAdblockPlus::GetSplitStr(LPCTSTR lpszRaw, LPCTSTR lpszSep, VString& vecStr)
{
	if( !lpszRaw || !lpszSep )
		return;

	int nLen = _tcslen(lpszRaw);
	int nSepLen = _tcslen(lpszSep);

	LPCTSTR lpszStart = lpszRaw;
	LPCTSTR lpszEnd = NULL;
	while( (lpszEnd = _tcsstr((LPTSTR)lpszStart, lpszSep)) )
	{
		vecStr.push_back( wstring(lpszStart, lpszEnd) );
		lpszStart = lpszEnd + nSepLen;
	}

	if( *lpszStart || lpszStart==lpszRaw+nLen )
		vecStr.push_back( lpszStart );
}

void CAdblockPlus::StringReplace(wstring& strRaw, LPCTSTR lpszOld, LPCTSTR lpszNew)
{
	if( !lpszOld || !lpszOld[0] || !lpszNew )
		return;

	unsigned long nLenOld = wcslen( lpszOld );
	unsigned long nLenNew = wcslen( lpszNew );
	wstring::size_type nStart = 0;
	while( TRUE )
	{
		wstring::size_type nSearch = strRaw.find( lpszOld, nStart );
		if( nSearch==strRaw.npos )
			break;
		wstring::iterator iter = strRaw.begin() + nSearch;
		strRaw.replace( iter, iter + nLenOld, lpszNew );
		nStart = nSearch + nLenNew;
	}
}

BOOL CAdblockPlus::IsHostMatch(LPCTSTR lpszHost, LPCTSTR lpszMatch)
{
	BOOL bRet = FALSE;
	if( !lpszHost || !lpszHost[0] || !lpszMatch || !lpszMatch[0] )
		return bRet;

	int nLenHost = _tcslen(lpszHost);
	int nLenMatch = _tcslen(lpszMatch);
	if( nLenHost < nLenMatch )
		return bRet;

	LPCTSTR szBegin = lpszHost + nLenHost- nLenMatch;
	if( !_tcsicmp(szBegin, lpszMatch) && (nLenHost==nLenMatch || *(szBegin -1)==_T('.')) )
		bRet = TRUE;

	return bRet;
}

BOOL CAdblockPlus::IsSameDomain(LPCTSTR lpszDomain1, LPCTSTR lpszDomain2)
{
	BOOL bRet = FALSE;
	if( !lpszDomain1 || !lpszDomain1[0] || !lpszDomain2 || !lpszDomain2[0] )
		return bRet;

	LPCTSTR szTLD1 = _tcsrchr((LPTSTR)lpszDomain1, _T('.'));
	if( !szTLD1 || isdigit(*(szTLD1+1)) )
		return bRet;
	LPCTSTR szTLD2 = _tcsrchr((LPTSTR)lpszDomain2, _T('.'));
	if( !szTLD2 || isdigit(*(szTLD2+1)) )
		return bRet;
	if( _tcsicmp(szTLD1, szTLD2) )
		return bRet;

	int nIdx1 = _tcslen(lpszDomain1) - 1;
	int nIdx2 = _tcslen(lpszDomain2) - 1;

	int nPos1 = 0;
	int nPos2 = 0;

	int nCount = 0;
	while( nIdx1 >= 0 && nIdx2 >= 0 )
	{
		TCHAR ch1 = tolower( lpszDomain1[nIdx1] );
		TCHAR ch2 = tolower( lpszDomain2[nIdx2] );
		if( ch1!=ch2 )
			break;

		if( _T('.')==ch1 || (!nIdx1&&!nIdx2) || (!nIdx1&&_T('.')==lpszDomain2[nIdx2-1]) || (!nIdx2&&_T('.')==lpszDomain1[nIdx1-1]) )
		{
			nCount++;
			if( 1==nCount )
				nPos1 = nIdx1;
			else if( 2==nCount )
				nPos2 = _T('.')==ch1 ? nIdx1 + 1 : nIdx1;
		}

		nIdx1--;
		nIdx2--;
	}

	if( nCount==2 && nPos1>nPos2 )
	{
		if( nPos1 - nPos2 > 8 )
			bRet = TRUE;
		else
		{
			BOOL bExist = FALSE;

			LPCTSTR szFstDom = lpszDomain1 + nPos1 + 1;
			TCHAR szSndDom[10] = {0};
			_tcsncpy( szSndDom, lpszDomain1 + nPos2, nPos1 - nPos2 );

			if( !bExist )
			{
				LPCTSTR szDomGEN[] = {_T("com"),_T("net"),_T("org"),_T("gov"),_T("edu"),_T("mil"),_T("biz"), _T("name"),_T("coop"),_T("co"),_T("ac"),
					_T("info"),_T("mobi"),_T("pro"),_T("travel"),_T("museum"),_T("int"),_T("aero"),_T("post"),_T("rec"),_T("asia")};
				for( unsigned int idx = 0; idx < SIZEOF(szDomGEN); idx++ )
				{
					if( !_tcsicmp(szSndDom, szDomGEN[idx]) )
					{
						bExist = TRUE;
						break;
					}
				}
			}

			if( !bExist && !_tcsicmp(szFstDom, _T("cn")) )
			{
				LPCTSTR szDomCN[] = {_T("ah"),_T("bj"),_T("cq"),_T("fj"),_T("gd"),_T("gs"),_T("gz"),_T("gx"),_T("ha"),_T("hb"),_T("he"),_T("hi"),
					_T("hk"),_T("hl"),_T("hn"),_T("jl"),_T("js"),_T("jx"),_T("ln"),_T("mo"),_T("nm"),_T("nx"),_T("qh"),_T("sc"),_T("sd"),_T("sh"),
					_T("sn"),_T("sx"),_T("tj"),_T("tw"),_T("xj"),_T("xz"),_T("yn"),_T("zj")};
				for( unsigned int idx = 0; idx < SIZEOF(szDomCN); idx++ )
				{
					if( !_tcsicmp(szSndDom, szDomCN[idx]) )
					{
						bExist = TRUE;
						break;
					}
				}
			}

			bRet = !bExist;
		}
	}
	else if( nCount > 2 )
		bRet = TRUE;

	return bRet;
}

void CAdblockPlus::InitCssRule(VCTStr& vecCssList)
{
	for( unsigned int idx = 0; idx < vecCssList.size(); idx++ )
	{
		if( vecCssList[idx] && !_tcschr((LPTSTR)vecCssList[idx], ADB_STYLE_SEPCH) )
			ParseCssRule( vecCssList[idx] );
	}
}

void CAdblockPlus::ParseCssRule(LPCTSTR lpszCssRule)
{
	if( !lpszCssRule || !lpszCssRule[0] )
		return;

	LPCTSTR lpszUPos = NULL;
	LPCTSTR lpszMPos = NULL;
	if( (lpszUPos = _tcsstr((LPTSTR)lpszCssRule, _T("#@#"))) )
	{
		LPCTSTR lpszRule = lpszUPos + _tcslen(_T("#@#"));
		if( lpszCssRule==lpszUPos )
		{
			m_strCssURule += lpszRule;
			m_strCssURule += ADB_STYLE_CSS;
		}
		else
		{
			VString vecHost;
			GetSplitStr( wstring(lpszCssRule, lpszUPos).c_str(), _T(","), vecHost );
			VString vecMHost;
			ADBCssHost uCssHost;
			uCssHost.strRule = lpszRule;
			uCssHost.strRule += ADB_STYLE_CSS;
			for( unsigned int idx = 0; idx < vecHost.size(); idx++ )
			{
				if( !vecHost[idx].size() )
					continue;
				if( _T('~')==vecHost[idx][0] )
					uCssHost.vecHost.push_back( vecHost[idx].c_str()+1 );
				else
					vecMHost.push_back( vecHost[idx] );
			}
			if( !vecMHost.size() )
				vecMHost.push_back( _T("") );
			for( unsigned int idxM = 0; idxM < vecMHost.size(); idxM++ )
				m_MapCssURule[vecMHost[idxM].c_str()].push_back( uCssHost );
		}
	}
	else if( (lpszMPos = _tcsstr((LPTSTR)lpszCssRule, _T("##"))) )
	{
		LPCTSTR lpszRule = lpszMPos + _tcslen(_T("##"));
		if( lpszCssRule==lpszMPos )
		{
			m_strCssMRule += lpszRule;
			m_strCssMRule += ADB_STYLE_CSS;
		}
		else
		{
			VString vecHost;
			GetSplitStr( wstring(lpszCssRule, lpszMPos).c_str(), _T(","), vecHost );
			VString vecMHost;
			ADBCssHost uCssHost;
			uCssHost.strRule = lpszRule;
			uCssHost.strRule += ADB_STYLE_CSS;
			for( unsigned int idx = 0; idx < vecHost.size(); idx++ )
			{
				if( !vecHost[idx].size() )
					continue;
				if( _T('~')==vecHost[idx][0] )
					uCssHost.vecHost.push_back( vecHost[idx].c_str()+1 );
				else
					vecMHost.push_back( vecHost[idx] );
			}
			if( !vecMHost.size() )
				vecMHost.push_back( _T("") );
			for (unsigned int idxM = 0; idxM < vecMHost.size(); idxM++) {
				m_MapCssMRule[vecMHost[idxM].c_str()].push_back(uCssHost);
//				if (!_tcsicmp(vecMHost[idxM].c_str(), _T("3g.163.com"))) {
//					LOGD("match 3g");
//					if(m_MapCssMRule.find(vecMHost[idxM].c_str())!=m_MapCssMRule.end())
//					{
//						LOGD("match");
//
//					}
//				}
			}
		}
	}
}

BOOL GetUrlAnsiValue(LPCTSTR lpszValue, string& strValue)
{
	BOOL bRet = FALSE;
	if( !lpszValue )
		return bRet;

	CHAR szValue[INTERNET_MAX_URL_LENGTH] = {0};

	int nLength = _tcslen( lpszValue );
	if( nLength < SIZEOF(szValue) )
	{
		bRet = TRUE;
		for( int idx = 0; idx < nLength; idx++ )
		{
			TCHAR ch = lpszValue[idx];
			if( ch > 0xFF )
			{
				bRet = FALSE;
				break;
			}
			szValue[idx] = ch;
		}
	}

	if( bRet )
		strValue = szValue;

	return bRet;
}

BOOL CAdblockPlus::IsRegExpMatch(LPCTSTR lpszText, LPCTSTR lpszRegExp)
{
	BOOL bRet = FALSE;
	if( !lpszText || !lpszText[0] || !lpszRegExp || !lpszRegExp[0] )
		return bRet;

	do {
		string strText;
		if( !GetUrlAnsiValue(lpszText, strText) )
			break;
		string strRegExp;
		if( !GetUrlAnsiValue(lpszRegExp, strRegExp) )
			break;

		regex_t regExp;
		if( 0!=regcomp(&regExp, strRegExp.c_str(), REG_EXTENDED|REG_NOSUB|REG_NEWLINE|REG_ICASE) )
			break;

		if( 0==regexec(&regExp, strText.c_str(), 0, NULL, 0) )
			bRet = TRUE;

		regfree( &regExp );
	} while (FALSE);

	return bRet;
}

void CAdblockPlus::InitUrlRule(VCTStr& vecUrlList, UINT uType)
{
	for( unsigned int idx = 0; idx < vecUrlList.size(); idx++ )
	{
		if( !vecUrlList[idx] || !vecUrlList[idx][0] || !vecUrlList[idx][1] )
			continue;

		if( _T('@')==vecUrlList[idx][0] && _T('@')==vecUrlList[idx][1] )
		{
			LPCTSTR szRule = vecUrlList[idx] + 2;
			vector<wstring> vecKeyword;
			if( GetBaseKeyword(szRule, vecKeyword) )
				InsertUrlRule( m_MapUrlURule, vecKeyword, szRule, uType );
		}
		else
		{
			vector<wstring> vecKeyword;
			if( GetBaseKeyword(vecUrlList[idx], vecKeyword) )
				InsertUrlRule( m_MapUrlMRule, vecKeyword, vecUrlList[idx], uType );
		}
	}
}

BOOL CAdblockPlus::GetBaseKeyword(LPCTSTR lpszRule, vector<wstring>& vecKeyword)
{
	BOOL bRet = FALSE;
	if( !lpszRule || !lpszRule[0] )
		return bRet;

	vector<wstring> vecRule;
	GetSplitStr( lpszRule, _T("$"), vecRule );
	if( vecRule.size()==0 || vecRule.size()>2 )
		return bRet;

	if( vecRule[0].size()==0 || (vecRule[0].size()>=2 && _T('/')==vecRule[0][0] && _T('/')==vecRule[0][vecRule[0].size()-1]) )
		return bRet;

	LPCTSTR lpszStart = vecRule[0].c_str();
	if( _T('|')==lpszStart[0] )
		lpszStart++;
	if( _T('|')==lpszStart[0] )
		lpszStart++;
	LPCTSTR lpszEnd = vecRule[0].c_str() + vecRule[0].size() - 1;
	if( _T('|')==lpszEnd[0] )
		lpszEnd--;

	if( lpszStart <= lpszEnd )
	{
		vector<wstring> vecStar;
		GetSplitStr( wstring(lpszStart, lpszEnd+1).c_str(), _T("*"), vecStar );
		for( unsigned int idxStar = 0; idxStar < vecStar.size(); idxStar++ )
		{
			vector<wstring> vecHat;
			GetSplitStr( vecStar[idxStar].c_str(), _T("^"), vecHat );
			for( unsigned int idxHat = 0; idxHat < vecHat.size(); idxHat++ )
			{
				if( vecHat[idxHat].size()>=MIN_URLKEYLEN && IsValidUrlKeyword(vecHat[idxHat].c_str()) )
				{
					vecKeyword.push_back( vecHat[idxHat] );
					bRet = TRUE;
				}
			}
		}
	}

	return bRet;
}

BOOL CAdblockPlus::IsValidUrlKeyword(LPCTSTR lpszKeyword)
{
	if( !lpszKeyword || !lpszKeyword[0] )
		return FALSE;

	int nLen = _tcslen(lpszKeyword);

	//such as http:// https://
	if( nLen <= 8 && !_tcsnicmp(lpszKeyword, _T("http"), 4) )
		return FALSE;
	
	//such as .com .com/ v.com .mp4? .html
	if( nLen <= 5 && nLen >= 2 && _T('.')==lpszKeyword[1] )
		lpszKeyword++;
	if( nLen <= 5 && _T('.')==lpszKeyword[0] && !_tcschr((LPTSTR)lpszKeyword+1, _T('.'))
		&& (_T('/')==lpszKeyword[nLen-1] || !_tcschr((LPTSTR)lpszKeyword+1, _T('/'))) )
		return FALSE;

	return TRUE;
}

BOOL CAdblockPlus::InsertUrlRule(MapUrlRule& mapUrlRule, const vector<wstring>& vecKeyword, LPCTSTR lpszRule, UINT uType)
{
	BOOL bRet = FALSE;
	if( !lpszRule || !lpszRule[0] )
		return bRet;

	for( unsigned int idxk = 0; idxk < vecKeyword.size(); idxk++ )
	{
		LPCTSTR lpszKeyword = vecKeyword[idxk].c_str();
		int nLen = _tcslen( lpszKeyword );
		if( nLen < MIN_URLKEYLEN )
			continue;

		int idx = 0;
		while( idx <= nLen-MIN_URLKEYLEN )
		{
			AD_NKEY strKey(lpszKeyword + idx, true);
			MapUrlRuleIter iter = mapUrlRule.find(strKey);
			BOOL bKeyExistSame = iter!=mapUrlRule.end() && iter->first && !_tcsicmp(iter->first, strKey);
			BOOL bKeyNotExist = iter==mapUrlRule.end() && IsValidUrlKeyword(strKey);
			if( bKeyExistSame || bKeyNotExist )
			{
				BOOL bRuleExist = FALSE;
				VADPValue& vADPValue = mapUrlRule[strKey];
				for( unsigned int idxr = 0; idxr < vADPValue.size(); idxr++ )
				{
					if( !_tcsicmp(vADPValue[idxr].szValue, lpszRule) )
					{
						vADPValue[idxr].uType |= uType;
						bRuleExist = TRUE;
						break;
					}
				}
				if( !bRuleExist )
				{
					ADBValue adbValue;
					adbValue.szValue = lpszRule;
					adbValue.uType = uType;
					vADPValue.push_back( adbValue );
				}
				bRet = TRUE;
				break;
			}
			idx++;
		}

		if( TRUE==bRet )
			break;
	}

	return bRet;
}

UrlType CAdblockPlus::GetUrlTypeByOption(LPCTSTR lpszOption, BOOL& bInvert)
{
	UrlType urlType = UrlType_Others;
	if( !lpszOption || !lpszOption[0] )
		return urlType;

	if( _T('~')==lpszOption[0] )
	{
		bInvert = TRUE;
		lpszOption++;
	}
	else
		bInvert = FALSE;

	if( !_tcsicmp(_T("object"), lpszOption) )
		urlType = UrlType_Object;
	else if( !_tcsicmp(_T("object-subrequest"), lpszOption) || !_tcsicmp(_T("object_subrequest"), lpszOption) )
		urlType = UrlType_ObjectSubRequest;
	else if( !_tcsicmp(_T("script"), lpszOption) )
		urlType = UrlType_Script;
	else if( !_tcsicmp(_T("stylesheet"), lpszOption) )
		urlType = UrlType_StyleSheet;
	else if( !_tcsicmp(_T("image"), lpszOption) )
		urlType = UrlType_Image;
	else if( !_tcsicmp(_T("subdocument"), lpszOption) )
		urlType = UrlType_SubDocument;
	else if( !_tcsicmp(_T("document"), lpszOption) )
		urlType = UrlType_Document;
	else if( !_tcsicmp(_T("popup"), lpszOption) )
		urlType = UrlType_Popup;
	else if( !_tcsicmp(_T("xmlhttprequest"), lpszOption) )
		urlType = UrlType_XmlHttpRequest;
	else if( !_tcsicmp(_T("elemhide"), lpszOption) )
		urlType = UrlType_ElemHide;

	return urlType;
}

UrlType CAdblockPlus::GetUrlTypeByUrl(LPCTSTR lpszUrl)
{
	UrlType urlType = UrlType_Others;

	TCHAR szExt[10] = {0};
	GetFileExtbyUrl( lpszUrl, szExt, SIZEOF(szExt)-1 );
	if( !_tcsicmp(szExt, _T(".js")) )
		urlType = UrlType_Script;
	else if( !_tcsicmp(szExt, _T(".css")) )
		urlType = UrlType_StyleSheet;
	else if( IsImageByExt(szExt) )
		urlType = UrlType_Image;

	return urlType;
}

BOOL CAdblockPlus::GetFileExtbyUrl(LPCTSTR lpszUrl, LPTSTR lpszExt, UINT nSize)
{
	BOOL bRet = FALSE;
	if( !lpszUrl || !lpszUrl[0] || !lpszExt || !nSize )
		return bRet;

	int nPosStart = -1;
	int nPosEnd = 0;
	while( lpszUrl[nPosEnd] && lpszUrl[nPosEnd]!=_T('?') && lpszUrl[nPosEnd]!=_T('#') )
	{
		if( _T('.')==lpszUrl[nPosEnd] )
			nPosStart = nPosEnd;
		nPosEnd++;
	}

	if( nPosStart!=-1 && nPosEnd > nPosStart && (UINT)(nPosEnd - nPosStart) < nSize )
	{
		_tcsncpy( lpszExt, lpszUrl+nPosStart, nPosEnd - nPosStart );
		if( !_tcschr(lpszExt, _T('/')) )
			bRet = TRUE;
	}

	return bRet;
}

BOOL CAdblockPlus::IsImageByExt(LPCTSTR lpszExt)
{
	BOOL bRet = FALSE;
	if( !lpszExt || !lpszExt[0] )
		return bRet;

	LPCTSTR szExts[] = {_T(".jpg"), _T(".gif"), _T(".png"), _T(".jpeg"), _T(".bmp"), _T(".pic"), _T(".ico")};
	for( unsigned int idx = 0; idx < SIZEOF(szExts); idx++ )
	{
		if( !_tcsicmp(szExts[idx], lpszExt) )
		{
			bRet = TRUE;
			break;
		}
	}

	return bRet;
}
