package com.template.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.template.social.entity.SocialFollow;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户关注关系 Mapper。
 */
@Mapper
public interface SocialFollowMapper extends BaseMapper<SocialFollow> {
}
