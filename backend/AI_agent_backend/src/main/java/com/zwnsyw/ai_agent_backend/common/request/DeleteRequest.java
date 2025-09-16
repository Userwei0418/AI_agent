package com.zwnsyw.ai_agent_backend.common.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;

@Data
public class DeleteRequest implements Serializable {
    /**
     * id
     */
    @NotNull(message = "id不能为空")
    private Long id;

    private static final long serialVersionUID = 1L;
}