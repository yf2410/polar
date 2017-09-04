#include "NavigateQueryInvoke.h"

extern "C" {
	void load(UINT nType, void* lpszData);
	std::string input(LPCTSTR lpszKey);
	void addItem(UINT nType, LPCTSTR lpszTitle, LPCTSTR lpszUrl, UINT nNavType, time_t tAccTime);

	void test(LPCTSTR lpszData);
}

void load(UINT nType, void* lpszData) {
	CNavigateQuery::GetInstance().Load(nType, lpszData);
}

std::string input(LPCTSTR lpszKey) {
	std::string result = CNavigateQuery::GetInstance().Input(lpszKey);
	return result;
}

void addItem(UINT nType, LPCTSTR lpszTitle, LPCTSTR lpszUrl, UINT nNavType, time_t tAccTime) {
	CNavigateQuery::GetInstance().AddItem(nType, lpszTitle, lpszUrl, nNavType, tAccTime);
}
