package com.anjingwsno1.langchain4jknowledgebase.controller;

import com.anjingwsno1.langchain4jknowledgebase.common.dto.KbQaRecordDto;
import com.anjingwsno1.langchain4jknowledgebase.common.dto.QAReq;
import com.anjingwsno1.langchain4jknowledgebase.entity.KnowledgeBaseQaRecord;
import com.anjingwsno1.langchain4jknowledgebase.service.KnowledgeBaseQaRecordService;
import com.anjingwsno1.langchain4jknowledgebase.service.KnowledgeBaseService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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

    @Operation(summary = "流式响应")
    @PostMapping(value = "/process/{kbUuid}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter sseAsk(@PathVariable String kbUuid, @RequestBody @Validated QAReq req) {
        return knowledgeBaseService.sseAsk(kbUuid, req);
    }

    @GetMapping("/record/search")
    public Page<KbQaRecordDto> list(String kbUuid, String keyword, @NotNull @Min(1) Integer currentPage, @NotNull @Min(10) Integer pageSize) {
        return knowledgeBaseQaRecordService.search(kbUuid, keyword, currentPage, pageSize);
    }
}
