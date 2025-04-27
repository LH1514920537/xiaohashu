package com.xiaohashu.xiaohashu.auth.controller;

import com.alibaba.nacos.api.config.annotation.NacosValue;
import com.quanxiaoha.framework.biz.operationlog.aspect.ApiOperationLog;
import com.quanxiaoha.framework.common.responnse.Response;
import com.xiaohashu.xiaohashu.auth.alarm.AlarmInterface;
import com.xiaohashu.xiaohashu.auth.model.vo.user.UserLoginReqVO;
import com.xiaohashu.xiaohashu.auth.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Slf4j
@RequestMapping("/user")
public class UserController {

    @Resource
    private AlarmInterface alarm;

    @Resource
    private UserService userService;

    @PostMapping("/login")
    @ApiOperationLog(description = "用户登录/注册")
    public Response<String> loginAndRegister(@Validated @RequestBody UserLoginReqVO userLoginReqVO) {
        return userService.loginAndRegister(userLoginReqVO);
    }


    @PostMapping("/logout")
    @ApiOperationLog(description = "账号登出")
    public Response<?> logout() {

        // todo 账号退出登录逻辑待实现

        return Response.success();
    }

}
