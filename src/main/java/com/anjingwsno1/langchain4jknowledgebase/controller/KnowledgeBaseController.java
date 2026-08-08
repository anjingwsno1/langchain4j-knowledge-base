package com.anjingwsno1.langchain4jknowledgebase.controller;

import com.anjingwsno1.langchain4jknowledgebase.common.dto.KbEditReq;
import com.anjingwsno1.langchain4jknowledgebase.entity.File;
import com.anjingwsno1.langchain4jknowledgebase.entity.KnowledgeBase;
import com.anjingwsno1.langchain4jknowledgebase.service.KnowledgeBaseService;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import jakarta.annotation.Resource;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;

import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;

@RestController
@RequestMapping("/knowledge-base")
@Validated
public class KnowledgeBaseController {
    @Resource
    private KnowledgeBaseService knowledgeBaseService;

    @PostMapping("/saveOrUpdate")
    public KnowledgeBase saveOrUpdate(@RequestBody KbEditReq kbEditReq) {
        return knowledgeBaseService.saveOrUpdate(kbEditReq);
    }

    @GetMapping("/info/{uuid}")
    public KnowledgeBase info(@PathVariable String uuid) {
        return knowledgeBaseService.lambdaQuery()
                .eq(KnowledgeBase::getUuid, uuid)
                .eq(KnowledgeBase::getIsDeleted, false)
                .one();
    }

    @PostMapping(path = "/upload/{uuid}", headers = "content-type=multipart/form-data", produces = MediaType.APPLICATION_JSON_VALUE)
    public File upload(@PathVariable String uuid, @RequestParam(value = "embedding", defaultValue = "true") Boolean embedding, @RequestParam("file") MultipartFile doc) {
        //通过FileSystemDocumentLoader加载本地文件到内存中
        return knowledgeBaseService.uploadDoc(uuid, embedding, doc);
    }
}
