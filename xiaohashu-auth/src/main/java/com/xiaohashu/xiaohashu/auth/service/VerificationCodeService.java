package com.xiaohashu.xiaohashu.auth.service;

import com.xiaohashu.xiaohashu.auth.model.vo.veriticationcode.SendVerificationCodeReqVO;

public interface VerificationCodeService {

    void send(SendVerificationCodeReqVO sendVerificationCodeReqVO);
}
