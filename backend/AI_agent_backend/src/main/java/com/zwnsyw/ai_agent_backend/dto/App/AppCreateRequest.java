package com.zwnsyw.ai_agent_backend.dto.App;

import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import java.io.Serializable;

import static com.zwnsyw.ai_agent_backend.enums.AiEnums.CodeGenTypeEnum.MULTI_FILE;

@Data
public class AppCreateRequest implements Serializable {

    /**
     * 应用初始化的 prompt
     */
    @NotBlank(message = "初始提示词不能为空")
    private String initPrompt;

    /**
     * 代码生成类型（枚举）
     * 默认为多文件模式
     */
    private String codeGenType = MULTI_FILE.getValue();

    private static final long serialVersionUID = 1L;
}
