package com.xiaohashu.xiaohashu.auth.service;

import com.quanxiaoha.framework.common.response.Response;
import com.xiaohashu.xiaohashu.auth.model.vo.user.UpdatePasswordReqVO;
import com.xiaohashu.xiaohashu.auth.model.vo.user.UserLoginReqVO;

public interface AuthService {
    /**
     * 登录与注册
     * @param userLoginReqVO
     * @return
     */
    Response<String> loginAndRegister(UserLoginReqVO userLoginReqVO);

    /**
     * 修改密码
     * @param updatePasswordReqVO
     * @return
     */
    Response<?> updatePassword(UpdatePasswordReqVO updatePasswordReqVO);

    /**
     * 退出登录
     * @return
     */
    Response<?> logout();
}
