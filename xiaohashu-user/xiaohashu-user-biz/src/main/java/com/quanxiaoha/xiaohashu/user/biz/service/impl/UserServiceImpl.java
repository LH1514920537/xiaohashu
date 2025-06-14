package com.quanxiaoha.xiaohashu.user.biz.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.RandomUtil;
import com.alibaba.nacos.shaded.com.google.common.base.Preconditions;
import com.github.benmanes.caffeine.cache.Cache;
import com.google.common.collect.Lists;
import com.quanxiaoha.framework.biz.context.holder.LoginUserContextHolder;
import com.quanxiaoha.framework.common.enums.DeletedEnum;
import com.quanxiaoha.framework.common.enums.StatusEnum;
import com.quanxiaoha.framework.common.exception.BizException;
import com.quanxiaoha.framework.common.response.Response;
import com.quanxiaoha.framework.common.utils.JsonUtils;
import com.quanxiaoha.framework.common.utils.ParamUtils;
import com.quanxiaoha.xiaohashu.user.biz.constant.RedisKeyConstants;
import com.quanxiaoha.xiaohashu.user.biz.constant.RoleConstants;
import com.quanxiaoha.xiaohashu.user.biz.domain.dataobject.RoleDO;
import com.quanxiaoha.xiaohashu.user.biz.domain.dataobject.UserDO;
import com.quanxiaoha.xiaohashu.user.biz.domain.dataobject.UserRoleDO;
import com.quanxiaoha.xiaohashu.user.biz.domain.mapper.RoleDOMapper;
import com.quanxiaoha.xiaohashu.user.biz.domain.mapper.UserDOMapper;
import com.quanxiaoha.xiaohashu.user.biz.domain.mapper.UserRoleDOMapper;
import com.quanxiaoha.xiaohashu.user.biz.enums.ResponseCodeEnum;
import com.quanxiaoha.xiaohashu.user.biz.enums.SexEnum;
import com.quanxiaoha.xiaohashu.user.biz.model.vo.UpdateUserInfoReqVO;
import com.quanxiaoha.xiaohashu.user.biz.rpc.DistributedIdGeneratorRpcService;
import com.quanxiaoha.xiaohashu.user.biz.rpc.OssRpcService;
import com.quanxiaoha.xiaohashu.user.biz.service.UserService;
import com.quanxiaoha.xiaohashu.user.dto.req.*;
import com.quanxiaoha.xiaohashu.user.dto.resp.FindUserByIdRespDTO;
import com.quanxiaoha.xiaohashu.user.dto.resp.FindUserByPhoneRespDTO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Resource
    private UserDOMapper userDOMapper;

    @Resource
    private OssRpcService ossRpcService;

    @Resource
    private UserRoleDOMapper userRoleDOMapper;
    @Resource
    private RoleDOMapper roleDOMapper;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private TransactionTemplate transactionTemplate;

    @Resource
    private DistributedIdGeneratorRpcService distributedIdGeneratorRpcService;


    @Resource(name = "taskExecutor")
    private ThreadPoolTaskExecutor threadPoolTaskExecutor;

    @Resource
    private Cache<Long,FindUserByIdRespDTO> caffeineCache;


    @Override
    public Response<?> updateUserInfo(UpdateUserInfoReqVO updateUserInfoReqVO) {
        UserDO userDO = new UserDO();
        // 设置当前需要更新的用户 ID
        userDO.setId(LoginUserContextHolder.getUserId());
        // 标识位：是否需要更新
        boolean needUpdate = false;

        // 头像
        MultipartFile avatarFile = updateUserInfoReqVO.getAvatar();

        if (Objects.nonNull(avatarFile)) {
            // todo: 调用对象存储服务上传文件
            String avatarUrl = ossRpcService.uploadFile(avatarFile);

            // 若上传头像失败，则抛出业务异常
            if (StringUtils.isBlank(avatarUrl)) {
                throw new BizException(ResponseCodeEnum.UPLOAD_AVATAR_FAIL);
            }
            log.info("==> 调用 oss 服务成功，上传头像，url：{}", avatarUrl);

            userDO.setAvatar(avatarUrl);
            needUpdate = true;
        }

        // 昵称
        String nickname = updateUserInfoReqVO.getNickname();
        if (StringUtils.isNotBlank(nickname)) {
            Preconditions.checkArgument(ParamUtils.checkNickname(nickname), ResponseCodeEnum.NICK_NAME_VALID_FAIL.getErrorMessage());
            userDO.setNickname(nickname);
            needUpdate = true;
        }

        // 小哈书号
        String xiaohashuId = updateUserInfoReqVO.getXiaohashuId();
        if (StringUtils.isNotBlank(xiaohashuId)) {
            Preconditions.checkArgument(ParamUtils.checkXiaohashuId(xiaohashuId), ResponseCodeEnum.XIAOHASHU_ID_VALID_FAIL.getErrorMessage());
            userDO.setXiaohashuId(xiaohashuId);
            needUpdate = true;
        }

        // 性别
        Integer sex = updateUserInfoReqVO.getSex();
        if (Objects.nonNull(sex)) {
            Preconditions.checkArgument(SexEnum.isValid(sex), ResponseCodeEnum.SEX_VALID_FAIL.getErrorMessage());
            userDO.setSex(sex);
            needUpdate = true;
        }

        // 生日
        LocalDate birthday = updateUserInfoReqVO.getBirthday();
        if (Objects.nonNull(birthday)) {
            userDO.setBirthday(birthday);
            needUpdate = true;
        }

        // 个人简介
        String introduction = updateUserInfoReqVO.getIntroduction();
        if (StringUtils.isNotBlank(introduction)) {
            Preconditions.checkArgument(ParamUtils.checkLength(introduction, 100), ResponseCodeEnum.INTRODUCTION_VALID_FAIL.getErrorMessage());
            userDO.setIntroduction(introduction);
            needUpdate = true;
        }

        // 背景图
        MultipartFile backgroundImgFile = updateUserInfoReqVO.getBackgroundImg();
        if (Objects.nonNull(backgroundImgFile)) {
            // todo: 调用对象存储服务上传文件
            String backgroundUrl = ossRpcService.uploadFile(backgroundImgFile);
            // 若上传背景图失败，则抛出业务异常
            if (StringUtils.isBlank(backgroundUrl)) {
                throw new BizException(ResponseCodeEnum.UPLOAD_BACKGROUND_IMG_FAIL);
            }
            log.info("==> 调用 oss 服务成功，上传背景图，url：{}", backgroundUrl);
            userDO.setBackgroundImg(backgroundUrl);
            needUpdate = true;
        }

        if (needUpdate) {
            // 更新用户信息
            userDO.setUpdateTime(LocalDate.now());
            userDOMapper.updateByPrimaryKeySelective(userDO);
        }
        return Response.success();
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Response<Long> register(RegisterUserReqDTO registerUserReqDTO) {
        return transactionTemplate.execute(status -> {
            try {
                // 获取全局自增的小哈书 ID 使用分布式id调用RPC
                String xiaohashuId = distributedIdGeneratorRpcService.getXiaohashuId();

                // RPC: 调用分布式 ID 生成服务生成用户 ID 避免使用到数据库的自增影响性能
                String userIdStr = distributedIdGeneratorRpcService.getUserId();


                UserDO userDO = UserDO.builder()
                        .id(Long.valueOf(userIdStr))
                        .phone(registerUserReqDTO.getPhone())
                        .xiaohashuId(xiaohashuId) // 自动生成小红书号 ID
                        .nickname("小红薯" + xiaohashuId) // 自动生成昵称, 如：小红薯10000
                        .status(StatusEnum.ENABLE.getValue()) // 状态为启用
                        .createTime(LocalDate.now())
                        .updateTime(LocalDate.now())
                        .isDeleted(DeletedEnum.NO.getValue()) // 逻辑删除
                        .build();

                // 添加入库
                userDOMapper.insert(userDO);


                // 获取刚刚添加入库的用户 ID
                Long userId = userDO.getId();

                // 给该用户分配一个默认角色
                UserRoleDO userRoleDO = UserRoleDO.builder()
                        .userId(userId)
                        .roleId(RoleConstants.COMMON_USER_ROLE_ID)
                        .createTime(LocalDate.now())
                        .updateTime(LocalDate.now())
                        .isDeleted(DeletedEnum.NO.getValue())
                        .build();
                userRoleDOMapper.insert(userRoleDO);

                // 将该用户的角色 ID 存入 Redis 中
                RoleDO roleDO = roleDOMapper.selectByPrimaryKey(RoleConstants.COMMON_USER_ROLE_ID);

                List<String> roles = new ArrayList<>(1);
                roles.add(roleDO.getRoleKey());
                String userRolesKey = RedisKeyConstants.buildUserRoleKey(userId);
                redisTemplate.opsForValue().set(userRolesKey, JsonUtils.toJsonString(roles));

                return Response.success(userId);
            }catch (Exception e) {
                status.setRollbackOnly(); // 标记事务为回滚
                log.error("==> 系统注册用户异常: ", e);
                return null;
            }
        });
    }

    @Override
    public Response<FindUserByPhoneRespDTO> findByPhone(FindUserByPhoneReqDTO findUserByPhoneReqDTO) {

        String phone = findUserByPhoneReqDTO.getPhone();

        // 根据手机号查询用户信息
        UserDO userDO = userDOMapper.selectByPhone(phone);

        // 判空
        if (Objects.isNull(userDO)) {
            throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
        }

        // 构建返参
        FindUserByPhoneRespDTO findUserByPhoneRspDTO = FindUserByPhoneRespDTO.builder()
                .id(userDO.getId())
                .password(userDO.getPassword())
                .build();

        return Response.success(findUserByPhoneRspDTO);
    }

    @Override
    public Response<?> updatePassword(UpdateUserPasswordReqDTO updateUserPasswordReqDTO) {
        // 获取当前请求对应的用户 ID
        Long userId = LoginUserContextHolder.getUserId();

        UserDO userDO = UserDO.builder()
                .id(userId)
                .password(updateUserPasswordReqDTO.getEncodePassword()) // 加密后的密码
                .updateTime(LocalDate.now())
                .build();
        // 更新密码
        userDOMapper.updateByPrimaryKeySelective(userDO);

        return Response.success();
    }

    @Override
    public Response<FindUserByIdRespDTO> findById(FindUserByIdReqDTO findUserByIdReqDTO) {
        Long userId = findUserByIdReqDTO.getId();

        // 先从本地内存中获取数据
        FindUserByIdRespDTO findUserByIdRespDTOLocalCache = caffeineCache.getIfPresent(userId);
        if (Objects.nonNull(findUserByIdRespDTOLocalCache)){
            log.info("==> 命中了本地缓存；{}", findUserByIdRespDTOLocalCache);
            return Response.success(findUserByIdRespDTOLocalCache);
        }

        // 用户缓存 Redis Key
        String userInfoRedisKey = RedisKeyConstants.buildUserInfoKey(userId);

        // 先从 Redis 缓存中查询
        String userInfoRedisValue = (String) redisTemplate.opsForValue().get(userInfoRedisKey);

        // 若 Redis 缓存中存在该用户信息
        if (StringUtils.isNotBlank(userInfoRedisValue)) {
            // 将存储的 Json 字符串转换成对象，并返回
            FindUserByIdRespDTO findUserByIdRespDTO = JsonUtils.parseObject(userInfoRedisValue, FindUserByIdRespDTO.class);
            // 使用异步线程将用户信息存入本地缓存
            threadPoolTaskExecutor.submit(() -> {
                if (Objects.nonNull(findUserByIdRespDTO)) {
                    // 写入本地缓存
                    caffeineCache.put(userId,findUserByIdRespDTO);
                }
            });
            return Response.success(findUserByIdRespDTO);
        }

        // 否则, 从数据库中查询
        // 根据用户 ID 查询用户信息
        UserDO userDO = userDOMapper.selectByPrimaryKey(userId);

        // 判空
        if (Objects.isNull(userDO)) {
            threadPoolTaskExecutor.execute(() -> {
                // 防止缓存穿透，将空数据存入 Redis 缓存 (过期时间不宜设置过长)
                // 保底1分钟 + 随机秒数
                long expireSeconds = 60 + RandomUtil.randomInt(60);
                redisTemplate.opsForValue().set(userInfoRedisKey, "null", expireSeconds, TimeUnit.SECONDS);
            });
            throw new BizException(ResponseCodeEnum.USER_NOT_FOUND);
        }

        // 构建返参
        FindUserByIdRespDTO findUserByIdRspDTO = FindUserByIdRespDTO.builder()
                .id(userDO.getId())
                .nickName(userDO.getNickname())
                .avatar(userDO.getAvatar())
                .introduction(userDO.getIntroduction())
                .build();

        // 异步将用户信息存入 Redis 缓存，提升响应速度
        threadPoolTaskExecutor.submit(() -> {
            // 过期时间（保底1天 + 随机秒数，将缓存过期时间打散，防止同一时间大量缓存失效，导致数据库压力太大）
            long expireSeconds = 60*60*24 + RandomUtil.randomInt(60*60*24);
            redisTemplate.opsForValue()
                    .set(userInfoRedisKey, JsonUtils.toJsonString(findUserByIdRspDTO), expireSeconds, TimeUnit.SECONDS);

            // 再存入Caffeine中
            caffeineCache.put(userId,findUserByIdRspDTO);
        });

        return Response.success(findUserByIdRspDTO);
    }
    /**
     * 批量根据用户 ID 查询用户信息
     *
     * @param findUsersByIdsReqDTO
     * @return
     */
    @Override
    public Response<List<FindUserByIdRespDTO>> findByIds(FindUserByIdsReqDTO findUsersByIdsReqDTO) {
        // 需要查询的用户 ID 集合
        List<Long> userIds = findUsersByIdsReqDTO.getIds();

        // 构建 Redis Key 集合
        List<String> redisKeys = userIds.stream()
                .map(RedisKeyConstants::buildUserInfoKey)
                .toList();

        // 先从 Redis 缓存中查, multiGet 批量查询提升性能
        List<Object> redisValues = redisTemplate.opsForValue().multiGet(redisKeys);
        // 如果缓存中不为空
        if (CollUtil.isNotEmpty(redisValues)) {
            // 过滤掉为空的数据
            redisValues = redisValues.stream().filter(Objects::nonNull).toList();
        }

        // 返参
        List<FindUserByIdRespDTO> findUserByIdRspDTOS = Lists.newArrayList();

        // 将过滤后的缓存集合，转换为 DTO 返参实体类
        if (CollUtil.isNotEmpty(redisValues)) {
            findUserByIdRspDTOS = redisValues.stream()
                    .map(value -> JsonUtils.parseObject(String.valueOf(value), FindUserByIdRespDTO.class))
                    .collect(Collectors.toList());
        }

        // 如果被查询的用户信息，都在 Redis 缓存中, 则直接返回
        if (CollUtil.size(userIds) == CollUtil.size(findUserByIdRspDTOS)) {
            return Response.success(findUserByIdRspDTOS);
        }

        // 还有另外两种情况：一种是缓存里没有用户信息数据，还有一种是缓存里数据不全，需要从数据库中补充
        // 筛选出缓存里没有的用户数据，去查数据库
        List<Long> userIdsNeedQuery = null;

        if (CollUtil.isNotEmpty(findUserByIdRspDTOS)) {
            // 将 findUserInfoByIdRspDTOS 集合转 Map
            Map<Long, FindUserByIdRespDTO> map = findUserByIdRspDTOS.stream()
                    .collect(Collectors.toMap(FindUserByIdRespDTO::getId, p -> p));

            // 筛选出需要查 DB 的用户 ID
            userIdsNeedQuery = userIds.stream()
                    .filter(id -> Objects.isNull(map.get(id)))
                    .toList();
        } else { // 缓存中一条用户信息都没查到，则提交的用户 ID 集合都需要查数据库
            userIdsNeedQuery = userIds;
        }

        // 从数据库中批量查询
        List<UserDO> userDOS = userDOMapper.selectByIds(userIdsNeedQuery);

        List<FindUserByIdRespDTO> findUserByIdRspDTOS2 = null;

        // 若数据库查询的记录不为空
        if (CollUtil.isNotEmpty(userDOS)) {
            // DO 转 DTO
            findUserByIdRspDTOS2 = userDOS.stream()
                    .map(userDO -> FindUserByIdRespDTO.builder()
                            .id(userDO.getId())
                            .nickName(userDO.getNickname())
                            .avatar(userDO.getAvatar())
                            .introduction(userDO.getIntroduction())
                            .build())
                    .collect(Collectors.toList());

            // TODO: 异步线程将用户信息同步到 Redis 中
            List<FindUserByIdRespDTO> finalFindUserByIdRspDTOS = findUserByIdRspDTOS2;
            threadPoolTaskExecutor.submit(() -> {
                // DTO 集合转 Map
                Map<Long, FindUserByIdRespDTO> map = finalFindUserByIdRspDTOS.stream()
                        .collect(Collectors.toMap(FindUserByIdRespDTO::getId, p -> p));

                // 执行 pipeline 操作
                redisTemplate.executePipelined(new SessionCallback<>() {
                    @Override
                    public Object execute(RedisOperations operations) {
                        for (UserDO userDO : userDOS) {
                            Long userId = userDO.getId();

                            // 用户信息缓存 Redis Key
                            String userInfoRedisKey = RedisKeyConstants.buildUserInfoKey(userId);

                            // DTO 转 JSON 字符串
                            FindUserByIdRespDTO findUserInfoByIdRspDTO = map.get(userId);
                            String value = JsonUtils.toJsonString(findUserInfoByIdRspDTO);

                            // 过期时间（保底1天 + 随机秒数，将缓存过期时间打散，防止同一时间大量缓存失效，导致数据库压力太大）
                            long expireSeconds = 60*60*24 + RandomUtil.randomInt(60*60*24);
                            operations.opsForValue().set(userInfoRedisKey, value, expireSeconds, TimeUnit.SECONDS);
                        }
                        return null;
                    }
                });
            });
        }

        // 合并数据
        if (CollUtil.isNotEmpty(findUserByIdRspDTOS2)) {
            findUserByIdRspDTOS.addAll(findUserByIdRspDTOS2);
        }

        return Response.success(findUserByIdRspDTOS);
    }

}
