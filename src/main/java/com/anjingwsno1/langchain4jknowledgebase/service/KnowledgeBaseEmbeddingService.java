package com.anjingwsno1.langchain4jknowledgebase.service;

import com.anjingwsno1.langchain4jknowledgebase.common.dto.KbItemEmbeddingDto;
import com.anjingwsno1.langchain4jknowledgebase.common.utils.MPPageUtil;
import com.anjingwsno1.langchain4jknowledgebase.entity.KnowledgeBaseEmbedding;
import com.anjingwsno1.langchain4jknowledgebase.mapper.KnowledgeBaseEmbeddingMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KnowledgeBaseEmbeddingService extends ServiceImpl<KnowledgeBaseEmbeddingMapper, KnowledgeBaseEmbedding> {
    public Page<KbItemEmbeddingDto> listByItemUuid(String kbItemUuid, int currentPage, int pageSize) {
        Page<KnowledgeBaseEmbedding> sourcePage = baseMapper.selectByItemUuid(new Page<>(currentPage, pageSize), kbItemUuid);
        Page<KbItemEmbeddingDto> result = new Page<>();
        MPPageUtil.convertToPage(sourcePage, result, KbItemEmbeddingDto.class, (source, target) -> {
            target.setEmbedding(source.getEmbedding().toArray());
            return target;
        });
        return result;
    }
}
