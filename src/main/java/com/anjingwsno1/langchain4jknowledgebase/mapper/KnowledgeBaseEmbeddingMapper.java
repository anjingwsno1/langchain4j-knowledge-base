package com.anjingwsno1.langchain4jknowledgebase.mapper;

import com.anjingwsno1.langchain4jknowledgebase.entity.KnowledgeBaseEmbedding;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface KnowledgeBaseEmbeddingMapper extends BaseMapper<KnowledgeBaseEmbedding> {
    Page<KnowledgeBaseEmbedding> selectByItemUuid(Page<KnowledgeBaseEmbedding> page, @Param("kbItemUuid") String uuid);
}
