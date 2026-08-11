package com.anjingwsno1.langchain4jknowledgebase.service;

import com.anjingwsno1.langchain4jknowledgebase.common.base.MockUser;
import com.anjingwsno1.langchain4jknowledgebase.common.exception.BaseException;
import com.anjingwsno1.langchain4jknowledgebase.entity.KnowledgeBase;
import com.anjingwsno1.langchain4jknowledgebase.entity.KnowledgeBaseItem;
import com.anjingwsno1.langchain4jknowledgebase.entity.User;
import com.anjingwsno1.langchain4jknowledgebase.mapper.KnowledgeBaseItemMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.anjingwsno1.langchain4jknowledgebase.common.enums.ErrorEnum.*;

@Slf4j
@Service
public class KnowledgeBaseItemService extends ServiceImpl<KnowledgeBaseItemMapper, KnowledgeBaseItem> {

    @Resource
    private KnowledgeBaseEmbeddingService knowledgeBaseEmbeddingService;

    @Lazy
    @Resource
    private KnowledgeBaseService knowledgeBaseService;

    public KnowledgeBaseItem getEnable(String uuid) {
        return ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(KnowledgeBaseItem::getUuid, uuid)
                .eq(KnowledgeBaseItem::getIsDeleted, false)
                .one();
    }

    public boolean softDelete(String uuid) {
        boolean privilege = checkPrivilege(uuid);
        if (!privilege) throw new BaseException(A_USER_NOT_AUTH);
        boolean success = ChainWrappers.lambdaUpdateChain(baseMapper)
                .eq(KnowledgeBaseItem::getUuid, uuid)
                .set(KnowledgeBaseItem::getIsDeleted, true)
                .update();
        if (!success) {
            return false;
        }
        knowledgeBaseEmbeddingService.deleteByItemUuid(uuid);

        KnowledgeBaseItem item = baseMapper.getByUuid(uuid);
        if (null != item) {
            knowledgeBaseService.updateStatistic(item.getKbUuid());
        }
        return true;
    }

    private boolean checkPrivilege(String uuid) {
        if (StringUtils.isBlank(uuid)) {
            throw new BaseException(A_PARAMS_ERROR);
        }
        User user = MockUser.getCurrentUser();
        if (null == user) {
            throw new BaseException(A_USER_NOT_EXIST);
        }
        if (user.getIsAdmin()) {
            return true;
        }
        Optional<KnowledgeBaseItem> kbItem = ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(KnowledgeBaseItem::getUuid, uuid)
                .oneOpt();
        if (kbItem.isPresent()) {
            KnowledgeBase kb = knowledgeBaseService.getById(kbItem.get().getKbId());
            if (null != kb) {
                return kb.getOwnerId().equals(user.getId());
            }
        }
        return false;
    }
}
