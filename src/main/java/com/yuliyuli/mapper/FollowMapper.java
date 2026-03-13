package com.yuliyuli.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.yuliyuli.entity.Follow;

public interface FollowMapper extends BaseMapper<Follow> {

    /**
     * 关注用户
     *
     * @param followUserId 关注用户ID
     * @param fanUserId 粉丝用户ID
     * @return 影响行数
     */
    int followUser(Long followUserId, Long fanUserId);
}
