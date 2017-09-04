#include "NavigateQuery.h"

extern "C" {
	void load(UINT nType, void* lpszData);
	std::string input(LPCTSTR lpszKey);
	void addItem(UINT nType, LPCTSTR lpszTitle, LPCTSTR lpszUrl, UINT nNavType, time_t tAccTime);

	void test(LPCTSTR lpszData);
}
