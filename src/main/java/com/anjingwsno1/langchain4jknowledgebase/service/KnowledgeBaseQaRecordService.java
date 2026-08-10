package com.anjingwsno1.langchain4jknowledgebase.service;

import com.anjingwsno1.langchain4jknowledgebase.entity.KnowledgeBase;
import com.anjingwsno1.langchain4jknowledgebase.entity.KnowledgeBaseQaRecord;
import com.anjingwsno1.langchain4jknowledgebase.entity.User;
import com.anjingwsno1.langchain4jknowledgebase.mapper.KnowledgeBaseQaRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class KnowledgeBaseQaRecordService extends ServiceImpl<KnowledgeBaseQaRecordMapper, KnowledgeBaseQaRecord> {
    public KnowledgeBaseQaRecord createNewRecord(User user, KnowledgeBase knowledgeBase, String question, String prompt, int promptTokens, String answer, int answerTokens, String modelName) {
        String uuid = UUID.randomUUID().toString().replace("-", "");
        KnowledgeBaseQaRecord newObj = new KnowledgeBaseQaRecord();
        newObj.setKbId(knowledgeBase.getId());
        newObj.setKbUuid((knowledgeBase.getUuid()));
        newObj.setUuid(uuid);
        newObj.setUserId(user.getId());
        newObj.setQuestion(question);
        newObj.setPrompt(prompt);
        newObj.setPromptTokens(promptTokens);
        newObj.setAnswer(answer);
        newObj.setAnswerTokens(answerTokens);
        baseMapper.insert(newObj);

        LambdaQueryWrapper<KnowledgeBaseQaRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBaseQaRecord::getUuid, uuid);
        return baseMapper.selectOne(wrapper);
    }
}
