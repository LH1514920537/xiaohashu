package com.quanxiaoha.xiaohashu.user.biz.domain.dataobject;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleDO {
    private Long id;

    private String roleName;

    private String roleKey;

    private Integer status;

    private Integer sort;

    private String remark;

    private LocalDate createTime;

    private LocalDate updateTime;

    private Boolean isDeleted;

}