#include "globaldef.h"
#if !defined(SX_NAVIGATEQUERY_H)
#define SX_NAVIGATEQUERY_H

enum 
{
	NavQuery_Preset = 0,
	NavQuery_History,
	NavQuery_Bookmark,
	NavQuery_All,	// must be the last one!
};

const std::string HISTORY_JSON_NODE_url = "url";
const std::string HISTORY_JSON_NODE_id = "id";
const std::string HISTORY_JSON_NODE_src = "src";
const std::string HISTORY_JSON_NODE_ts = "ts";
const std::string HISTORY_JSON_NODE_title = "title";

const std::string BOOKMARK_JSON_NODE_url = "url";
const std::string BOOKMARK_JSON_NODE_name = "name";

string unicodeToUtf8(std::wstring unicodeStr);
wstring utf8strTowstr(std::string utf8str);

typedef struct _tagNavInputItem
{
	int nType;
	wstring strKey;
	wstring strTitle;
	wstring strUrl;
	DWORD dwProtLen;
	DWORD dwWeight;
	BOOL bCanAC;
	DWORD arrTitleHilight[10];  // hiword is start, loword is length
	DWORD arrUrlHilight[10];
	_tagNavInputItem()
	{
		nType = NavQuery_All;
		dwProtLen = 0;
		dwWeight = 0;
		bCanAC = FALSE;
		memset(&arrTitleHilight, 0, sizeof(arrTitleHilight));
		memset(&arrUrlHilight, 0, sizeof(arrUrlHilight));
	}
}NAVINPUTITEM, *LPNAVINPUTITEM;

//////////////////////////////////////////////////////////////////////////
// CNavigateQuery

class CNavigateQuery
{
public:
	typedef struct _tagAutoCompleteItem
	{
		UINT nType;
		LPTSTR lpszTitle;
		LPTSTR lpszUrl;
		DWORD dwProtLen;
		time_t tAccTime;
		BOOL bValid;
		_tagAutoCompleteItem()
		{
			nType = NavQuery_All;
			lpszTitle = NULL;
			lpszUrl = NULL;
			dwProtLen = 0;
			tAccTime = 0;
			bValid = FALSE;
		}
	}AUTOCOMPLETEITEM, *LPAUTOCOMPLETEITEM;

public:
	CNavigateQuery(void);
	~CNavigateQuery(void);

	static CNavigateQuery& GetInstance();
		
	void SetMaxRetCount(UINT nCount);
	std::string Input(LPCTSTR lpszKey);
	void Load(UINT nType, void* lpszData);

	void AddItem(UINT nType, LPCTSTR lpszTitle, LPCTSTR lpszUrl, UINT nNavType, time_t tAccTime);

	void LoadPreset(LPCTSTR lpszData);
	void LoadHistory(LPCSTR lpszData);
	void LoadBookmark(LPCSTR lpszData);

protected:

	UINT m_nMaxRetCount;
	BOOL m_bInit;
	BOOL m_bLoaded[NavQuery_All];
	TCHAR m_szLastKey[MAX_URL_LEN];

	typedef struct _tagNavItem
	{
		wstring strTitle;
		wstring strUrl;
		DWORD dwProtLen;
		DWORD dwWeight;
		time_t tAccTime;
		int nSrcType;
		_tagNavItem()
		{
			dwWeight = 0;
			tAccTime = 0;
			nSrcType = 0;
			dwProtLen = 0;
		}
	}NAVITEM, *LPNAVITEM;
	typedef std::vector<NAVITEM> tagNavItemVec;

	tagNavItemVec m_vNavItems[3];
	typedef std::map<wstring, DWORD> tagNavUrlIndexMap;
	tagNavUrlIndexMap m_mpHistoryIndex;

	typedef struct _tagRetItem
	{
		UINT nType;
		LPCTSTR lpszTitle;
		LPCTSTR lpszUrl;
		DWORD dwProtLen;
		time_t tAccTime;
		int nSrcType;
		DWORD dwWeight;
		DWORD dwDynWeight;
		BOOL bCanAC;
		_tagRetItem()
		{
			nType = NavQuery_All;
			lpszTitle = NULL;
			lpszUrl = NULL;
			dwProtLen = 0;
			tAccTime = 0;
			nSrcType = 0;
			dwWeight = 0;
			dwDynWeight = 0;
			bCanAC = FALSE;
		}
	}RETITEM, *LPRETITEM;
	typedef std::vector<RETITEM> tagRetItemVec;

	tagRetItemVec m_vRetItems;

protected:
	static bool NavRetVecGreater(RETITEM item1, RETITEM item2);

};

#define _NavQuery CNavigateQuery::GetInstance()	


#endif
