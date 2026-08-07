package com.anjingwsno1.langchain4jknowledgebase.service;

import com.anjingwsno1.langchain4jknowledgebase.entity.KnowledgeBaseItem;
import com.anjingwsno1.langchain4jknowledgebase.mapper.KnowledgeBaseItemMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class KnowledgeBaseItemService extends ServiceImpl<KnowledgeBaseItemMapper, KnowledgeBaseItem> {
}
