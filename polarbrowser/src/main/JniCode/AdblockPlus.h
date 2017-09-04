#include "globaldef.h"
#if !defined(SX_ADBLOCKPLUS_H)
#define SX_ADBLOCKPLUS_H

#define ADBLOCK_TYPE_NONE		0x00000000
#define ADBLOCK_TYPE_FULL		0xFFFFFFFF
#define ADBLOCK_TYPE_RAW		0x00000001
#define ADBLOCK_TYPE_WIN		0x00000002

typedef class struRKey
{
public:
	struRKey(LPCTSTR lpszKey):strKey(lpszKey){}
	struRKey(wstring& lpszKey):strKey(lpszKey){}
	bool  operator <(const struRKey& szCmp) const {return _tcsicmp(strKey.c_str(), szCmp.strKey.c_str())<0;}
	bool  operator ==(LPCTSTR szCmp) const {return _tcsicmp(strKey.c_str(), szCmp) == 0;}
	inline operator LPCTSTR() const {return strKey.c_str();}

private:
	wstring strKey;
}AD_RKEY;

typedef class struNKey
{
public:
	struNKey(LPCTSTR lpszKey, bool bRule) : szKey(lpszKey), nKeySize(0), bKeyAlloc(false), bKeyRule(bRule){
		if( szKey ) nKeySize = _tcslen( szKey );
	}
	struNKey(const struNKey& sNKey){
		DepthCopy( sNKey );
	}
	virtual ~struNKey(){
		if( bKeyAlloc && szKey ){delete []szKey; szKey = NULL;}
	}
	inline struNKey& operator=(const struNKey& sNKey){
		if( bKeyAlloc && szKey ){delete []szKey; szKey = NULL;}
		DepthCopy( sNKey );
		return *this;
	}
	bool operator<(const struNKey& strCmp) const {
		if( !szKey || !strCmp.szKey )
			return false;
		int nSize = bKeyRule ? nKeySize : strCmp.nKeySize;
		return _tcsnicmp(szKey, strCmp.szKey, nSize) < 0;
	}
	inline operator LPCTSTR() const {return szKey;}

protected:
	void DepthCopy(const struNKey& sNKey){
		if( !sNKey.szKey ) return;
		bKeyAlloc = true;
		bKeyRule = sNKey.bKeyRule;
		nKeySize = _tcslen( sNKey.szKey );
		szKey = new TCHAR[nKeySize+1];
		if( szKey )
			_tcscpy( (LPTSTR)szKey, sNKey.szKey );
	}

private:
	LPCTSTR szKey;
	int nKeySize;
	bool bKeyAlloc;
	bool bKeyRule;
}AD_NKEY;

typedef struct tagSwfReplace 
{
	wstring strUrlPattern;
	BOOL bRedirect;
	wstring strRedirPattern;
	BOOL bSelector;
	wstring strSelector;
	wstring strSelFReplace;
	wstring strReplace;
}SwfReplace;

typedef enum tagUrlType{
	UrlType_Others = 0,
	UrlType_Script,
	UrlType_Image,
	UrlType_StyleSheet,
	UrlType_Object,
	UrlType_ObjectSubRequest,
	UrlType_SubDocument,
	UrlType_XmlHttpRequest,
	UrlType_Document,
	UrlType_Popup,
	UrlType_ElemHide
}UrlType;

typedef struct tagUrlParam
{
	wstring strDocUrl;
	wstring strUrl;
	UrlType urlType;
	//assist
	wstring strDocHost;
	wstring strUrlHost;
}UrlParam;

typedef struct tagADBValue
{
	LPCTSTR szValue;
	UINT uType;
}ADBValue;

typedef struct tagADBDocUrl
{
	UINT uDocType;
	UINT uEleType;
}ADBDocUrl;

typedef vector<LPCTSTR> VCTStr;
typedef vector<wstring> VString;
typedef vector<ADBValue> VADPValue;

typedef struct tagADBCssHost
{
	VString vecHost;
	wstring strRule;
}ADBCssHost;

typedef vector<ADBCssHost> VCssHost;
typedef VCssHost::iterator VCssHostIter;
typedef map<AD_RKEY, VCssHost> MapCssRule;
typedef MapCssRule::iterator MapCssRuleIter;

typedef map<AD_NKEY, VADPValue> MapUrlRule;
typedef MapUrlRule::iterator MapUrlRuleIter;
typedef map<AD_RKEY, BOOL> MapUrlPass;
typedef MapUrlPass::iterator MapUrlPassIter;
typedef map<AD_RKEY, ADBDocUrl> MapUrlDoc;
typedef MapUrlDoc::iterator MapUrlDocIter;

class CAdblockPlus
{
public:
	CAdblockPlus();
	virtual ~CAdblockPlus();

	static CAdblockPlus* GetInstance();
	static BOOL HasInited() {return m_bAdblockInited;}
	void Initialize(LPCTSTR lpszRaw, LPCTSTR lpszWin, LPCTSTR lpszEpt);
	UINT MatchUrl(const UrlParam& urlParam, UINT uType);
	BOOL MatchCss(LPCTSTR lpszUrl, LPCTSTR lpszUrlHost, VString& vecCss, BOOL bJustFirst);
	BOOL IsSameDomain(LPCTSTR lpszDomain1, LPCTSTR lpszDomain2);

public:
	UrlType GetUrlTypeByUrl(LPCTSTR lpszUrl);

protected:
	void OnParse();

	void InitRawList(LPCTSTR lpszBuffer, LPTSTR& pOutBuffer, VCTStr& vecUrlList, VCTStr& vecCssList);
	void InitCssRule(VCTStr& vecCssList);
	void InitUrlRule(VCTStr& vecUrlList, UINT uType);

	void ParseCssRule(LPCTSTR lpszCssRule);

	BOOL GetBaseKeyword(LPCTSTR lpszRule, vector<wstring>& vecKeyword);
	BOOL IsValidUrlKeyword(LPCTSTR lpszKeyword);
	BOOL InsertUrlRule(MapUrlRule& mapUrlRule, const vector<wstring>& vecKeyword, LPCTSTR lpszRule, UINT uType);
	BOOL MatchUrlMetaRule(MapUrlRule& mapUrlRule, LPCTSTR lpszUrl, vector<MapUrlRuleIter>& vecIter);
	BOOL MatchUrlDocRule(LPCTSTR lpszDocUrl, BOOL bCssCheck, UINT uType);
	BOOL MatchUrlRule(LPCTSTR lpszRule, LPCTSTR lpszDocUrl, LPCTSTR lpszUrl, BOOL bWhiteList, const UrlParam& urlParam);
	BOOL MatchUrlRegular(LPCTSTR lpszReg, LPCTSTR lpszUrl);
	BOOL GetRegularExp(LPCTSTR lpszReg, wstring& strRegExp);
	BOOL MatchUrlOption(LPCTSTR lpszOpt, LPCTSTR lpszDocUrl, LPCTSTR lpszUrl, BOOL bWhiteList, const UrlParam& urlParam);
	BOOL IsOptionExist(const vector<wstring>& vecOpt, LPCTSTR lpszOpt, UINT& idxOpt, BOOL& bInvert);
	UrlType GetUrlTypeByOption(LPCTSTR lpszOption, BOOL& bInvert);

	void GetStrLine(LPWSTR pBuf, const int nSize, int& nIdx, LPWSTR& lpszLine);
	void GetSplitStr(LPCTSTR lpszRaw, LPCTSTR lpszSep, VString& vecStr);
	void StringReplace(wstring& strRaw, LPCTSTR lpszOld, LPCTSTR lpszNew);
	BOOL IsHostMatch(LPCTSTR lpszHost, LPCTSTR lpszMatch);

	BOOL IsRegExpMatch(LPCTSTR lpszText, LPCTSTR lpszRegExp);
	BOOL GetFileExtbyUrl(LPCTSTR lpszUrl, LPTSTR lpszExt, UINT nSize);
	BOOL IsImageByExt(LPCTSTR lpszExt);

private:
	static BOOL m_bAdblockInited;
	BOOL m_bInit;
	BOOL m_bInitAdb;

	MapCssRule m_MapCssMRule;
	MapCssRule m_MapCssURule;
	wstring m_strCssMRule;
	wstring m_strCssURule;

	MapUrlRule m_MapUrlMRule;
	MapUrlRule m_MapUrlURule;
};

#endif
