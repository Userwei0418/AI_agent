package com.zwnsyw.ai_agent_backend.dto.User;

import com.zwnsyw.ai_agent_backend.common.request.PageRequest;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class UserQueryRequest extends PageRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 搜索关键字（可以是id、用户名或账号）
     */
    @Size(max = 50, message = "搜索关键字长度不能超过50")
    private String keyword;
}