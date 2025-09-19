package com.zwnsyw.ai_agent_backend.dto.App;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AppDeployRequest implements Serializable {

    /**
     * 应用 id
     */
    @NotNull(message = "应用ID不能为空")
    private Long appId;

    @Serial
    private static final long serialVersionUID = 1L;
}
