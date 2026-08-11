package com.anjingwsno1.langchain4jknowledgebase.service;

import com.anjingwsno1.langchain4jknowledgebase.common.base.MockUser;
import com.anjingwsno1.langchain4jknowledgebase.common.dto.KbQaRecordDto;
import com.anjingwsno1.langchain4jknowledgebase.common.exception.BaseException;
import com.anjingwsno1.langchain4jknowledgebase.common.utils.MPPageUtil;
import com.anjingwsno1.langchain4jknowledgebase.entity.KnowledgeBase;
import com.anjingwsno1.langchain4jknowledgebase.entity.KnowledgeBaseQaRecord;
import com.anjingwsno1.langchain4jknowledgebase.entity.User;
import com.anjingwsno1.langchain4jknowledgebase.mapper.KnowledgeBaseQaRecordMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.UUID;

import static com.anjingwsno1.langchain4jknowledgebase.common.enums.ErrorEnum.A_DATA_NOT_FOUND;

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

    public Page<KbQaRecordDto> search(String kbUuid, String keyword, Integer currentPage, Integer pageSize) {
        LambdaQueryWrapper<KnowledgeBaseQaRecord> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(KnowledgeBaseQaRecord::getKbUuid, kbUuid);
        wrapper.eq(KnowledgeBaseQaRecord::getIsDeleted, false);
        wrapper.eq(KnowledgeBaseQaRecord::getUserId, MockUser.getCurrentUser().getId());
        if (StringUtils.isNotBlank(keyword)) {
            wrapper.like(KnowledgeBaseQaRecord::getQuestion, keyword);
        }
        wrapper.orderByDesc(KnowledgeBaseQaRecord::getUpdateTime);
        Page<KnowledgeBaseQaRecord> page = baseMapper.selectPage(new Page<>(currentPage, pageSize), wrapper);

        Page<KbQaRecordDto> result = new Page<>();
        MPPageUtil.convertToPage(page, result, KbQaRecordDto.class, (t1, t2) -> {
            t2.setAiModelPlatform(t1.getAiModelId().toString());
            return t2;
        });
        return result;
    }

    public boolean softDelete(String uuid) {
        KnowledgeBaseQaRecord exist = ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(KnowledgeBaseQaRecord::getUuid, uuid)
                .one();
        if (null == exist) {
            throw new BaseException(A_DATA_NOT_FOUND);
        }
        return ChainWrappers.lambdaUpdateChain(baseMapper)
                .eq(KnowledgeBaseQaRecord::getId, exist.getId())
                .set(KnowledgeBaseQaRecord::getIsDeleted, true)
                .update();
    }
}
