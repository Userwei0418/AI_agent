package com.zwnsyw.ai_agent_backend.dto.App;

import com.zwnsyw.ai_agent_backend.common.request.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@Data
@EqualsAndHashCode(callSuper = true)
public class AppQueryRequest extends PageRequest implements Serializable {
    /**
     * id
     */
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 代码生成类型
     */
    private String codeGenType;

    /**
     * 创建用户ID
     */
    private Long userId;

    /**
     * 搜索关键词
     */
    private String keyword;

    private static final long serialVersionUID = 1L;
}
