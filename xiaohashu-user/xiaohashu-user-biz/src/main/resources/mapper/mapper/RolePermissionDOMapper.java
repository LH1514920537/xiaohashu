package com.quanxiaoha.xiaohashu.user.biz.domain.mapper;

import com.xiaohashu.xiaohashu.auth.domain.dataobject.RolePermissionDO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface RolePermissionDOMapper {
    int deleteByPrimaryKey(Long id);

    int insert(RolePermissionDO record);

    int insertSelective(RolePermissionDO record);

    RolePermissionDO selectByPrimaryKey(Long id);

    int updateByPrimaryKeySelective(RolePermissionDO record);

    int updateByPrimaryKey(RolePermissionDO record);

    List<RolePermissionDO> selectByRoleId(@Param("roleIds") List<Long> roleIds);
}