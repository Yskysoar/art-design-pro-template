package com.template.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.template.social.entity.SocialMessage;
import org.apache.ibatis.annotations.Mapper;

/**
 * 社交消息 Mapper。
 */
@Mapper
public interface SocialMessageMapper extends BaseMapper<SocialMessage> {
}
