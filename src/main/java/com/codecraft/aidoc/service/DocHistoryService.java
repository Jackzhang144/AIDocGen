package com.codecraft.aidoc.service;

import com.codecraft.aidoc.common.PageResponse;
import com.codecraft.aidoc.pojo.request.DocHistoryQueryRequest;
import com.codecraft.aidoc.pojo.response.DocHistoryItem;

/**
 * Provides paginated views over historical documentation generation records.
 */
public interface DocHistoryService {

    PageResponse<DocHistoryItem> pageHistory(DocHistoryQueryRequest request);
}
