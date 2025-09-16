package com.zwnsyw.ai_agent_backend.dto.App;

import lombok.Data;

import jakarta.validation.constraints.NotNull;
import java.io.Serializable;

import static com.zwnsyw.ai_agent_backend.contant.AppConstant.DEFAULT_APP_PRIORITY;


@Data
public class AppUpdateRequest implements Serializable {
    /**
     * id
     */
    @NotNull(message = "应用ID不能为空")
    private Long id;

    /**
     * 应用名称
     */
    private String appName;

    /**
     * 应用封面
     */
    private String cover;

    /**
     * 优先级
     * 默认为 DEFAULT_APP_PRIORITY(0)
     */
    private Integer priority = DEFAULT_APP_PRIORITY;

    private static final long serialVersionUID = 1L;
}
