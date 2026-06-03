package com.template.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.template.social.entity.SocialConversation;
import org.apache.ibatis.annotations.Mapper;

/**
 * 社交会话 Mapper。
 */
@Mapper
public interface SocialConversationMapper extends BaseMapper<SocialConversation> {
}
