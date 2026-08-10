package com.anjingwsno1.langchain4jknowledgebase.controller;

import com.anjingwsno1.langchain4jknowledgebase.common.dto.QAReq;
import com.anjingwsno1.langchain4jknowledgebase.entity.KnowledgeBaseQaRecord;
import com.anjingwsno1.langchain4jknowledgebase.service.KnowledgeBaseQaRecordService;
import com.anjingwsno1.langchain4jknowledgebase.service.KnowledgeBaseService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/knowledge-base/qa/")
@Validated
public class KnowledgeBaseQAController {
    @Resource
    private KnowledgeBaseService knowledgeBaseService;

    @Resource
    private KnowledgeBaseQaRecordService knowledgeBaseQaRecordService;

    @PostMapping("/ask/{kbUuid}")
    public KnowledgeBaseQaRecord ask(@PathVariable String kbUuid, @RequestBody @Validated QAReq req) {
        return knowledgeBaseService.ask(kbUuid, req.getQuestion(), req.getModelName());
    }
}
