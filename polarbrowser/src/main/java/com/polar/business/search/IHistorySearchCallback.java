package com.polar.business.search;

import java.util.List;

public interface IHistorySearchCallback {
	public void notifyQueryResult(List<HistorySearchInfo> result);
}
