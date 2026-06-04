package com.template.social.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.template.social.entity.SocialBlock;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户拉黑关系 Mapper。
 */
@Mapper
public interface SocialBlockMapper extends BaseMapper<SocialBlock> {
}
