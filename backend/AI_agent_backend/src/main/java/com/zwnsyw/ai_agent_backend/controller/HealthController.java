package com.zwnsyw.ai_agent_backend.controller;

import com.zwnsyw.ai_agent_backend.common.response.BaseResponse;
import com.zwnsyw.ai_agent_backend.common.response.ResultUtils;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
public class HealthController {

    @GetMapping("/login")
    public BaseResponse<String> userLoginBySession() {
        return ResultUtils.success("ok");
    }
}