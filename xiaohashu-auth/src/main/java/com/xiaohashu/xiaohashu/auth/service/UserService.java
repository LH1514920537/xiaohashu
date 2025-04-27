package com.xiaohashu.xiaohashu.auth.service;

import com.quanxiaoha.framework.common.responnse.Response;
import com.xiaohashu.xiaohashu.auth.model.vo.user.UserLoginReqVO;

public interface UserService {
    /**
     * 登录与注册
     * @param userLoginReqVO
     * @return
     */
    Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO);
}
