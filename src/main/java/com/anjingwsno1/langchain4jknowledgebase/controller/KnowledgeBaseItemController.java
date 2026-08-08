package com.anjingwsno1.langchain4jknowledgebase.controller;

import com.anjingwsno1.langchain4jknowledgebase.entity.KnowledgeBaseItem;
import com.anjingwsno1.langchain4jknowledgebase.service.KnowledgeBaseItemService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/knowledge-base-item")
@Validated
public class KnowledgeBaseItemController {
    @Resource
    private KnowledgeBaseItemService knowledgeBaseItemService;

    @GetMapping("/info/{uuid}")
    public KnowledgeBaseItem info(@PathVariable String uuid) {
        return knowledgeBaseItemService.lambdaQuery()
                .eq(KnowledgeBaseItem::getUuid, uuid)
                .eq(KnowledgeBaseItem::getIsDeleted, false)
                .one();
    }
}
