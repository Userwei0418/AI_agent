package com.zwnsyw.ai_agent_backend.common.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.servlet.http.HttpServletRequest;
import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PageRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    public static final Long DEFAULT_PAGE = 1L;
    public static final Long DEFAULT_PAGE_SIZE = 10L;
    public static final String DEFAULT_SORT_ORDER = "descend";
    public static final Long MAX_PAGE_SIZE = 100L;

    private Long page = DEFAULT_PAGE;
    private Long pageSize = DEFAULT_PAGE_SIZE;
    private String sortField;
    private String sortOrder = DEFAULT_SORT_ORDER;

    public static PageRequest fromRequest(HttpServletRequest request) {
        PageRequest pageRequest = new PageRequest();
        String pageParam = request.getParameter("page");
        String pageSizeParam = request.getParameter("pageSize");
        String sortFieldParam = request.getParameter("sortField");
        String sortOrderParam = request.getParameter("sortOrder");

        // 处理页码
        if (pageParam != null && !pageParam.isEmpty()) {
            try {
                long page = Long.parseLong(pageParam);
                if (page > 0) {
                    pageRequest.setPage(page);
                }
            } catch (NumberFormatException e) {
                // 使用默认值
            }
        }

        // 处理页面大小
        if (pageSizeParam != null && !pageSizeParam.isEmpty()) {
            try {
                long pageSize = Long.parseLong(pageSizeParam);
                if (pageSize > 0 && pageSize <= MAX_PAGE_SIZE) {
                    pageRequest.setPageSize(pageSize);
                }
            } catch (NumberFormatException e) {
                // 使用默认值
            }
        }

        // 处理排序字段
        if (sortFieldParam != null && !sortFieldParam.isEmpty()) {
            pageRequest.setSortField(sortFieldParam);
        }

        // 处理排序顺序
        if ("ascend".equals(sortOrderParam) || "descend".equals(sortOrderParam)) {
            pageRequest.setSortOrder(sortOrderParam);
        }

        return pageRequest;
    }
}