package com.anjingwsno1.langchain4jknowledgebase.service;

import com.anjingwsno1.langchain4jknowledgebase.common.base.MockUser;
import com.anjingwsno1.langchain4jknowledgebase.common.constant.Constant;
import com.anjingwsno1.langchain4jknowledgebase.common.constant.RedisKeyConstant;
import com.anjingwsno1.langchain4jknowledgebase.common.dto.KbEditReq;
import com.anjingwsno1.langchain4jknowledgebase.common.exception.BaseException;
import com.anjingwsno1.langchain4jknowledgebase.entity.File;
import com.anjingwsno1.langchain4jknowledgebase.entity.KnowledgeBase;
import com.anjingwsno1.langchain4jknowledgebase.entity.KnowledgeBaseItem;
import com.anjingwsno1.langchain4jknowledgebase.entity.User;
import com.anjingwsno1.langchain4jknowledgebase.mapper.KnowledgeBaseMapper;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.extension.toolkit.ChainWrappers;
import dev.langchain4j.data.document.Document;
import dev.langchain4j.data.document.parser.TextDocumentParser;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

import static com.anjingwsno1.langchain4jknowledgebase.common.enums.ErrorEnum.*;
import static com.anjingwsno1.langchain4jknowledgebase.common.utils.Utils.toPath;
import static dev.langchain4j.data.document.loader.FileSystemDocumentLoader.loadDocument;

@Slf4j
@Service
public class KnowledgeBaseService extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> {
    @Resource
    private FileService fileService;

    @Resource
    private KnowledgeBaseItemService knowledgeBaseItemService;

    @Resource
    private RAGService ragService;

    @Resource
    StringRedisTemplate stringRedisTemplate;

    public KnowledgeBase saveOrUpdate(KbEditReq kbEditReq) {
        String uuid = kbEditReq.getUuid();
        KnowledgeBase knowledgeBase = new KnowledgeBase();
        knowledgeBase.setTitle(kbEditReq.getTitle());
        knowledgeBase.setRemark(kbEditReq.getRemark());
        if (null != kbEditReq.getIsPublic()) {
            knowledgeBase.setIsPublic(kbEditReq.getIsPublic());
        }
        if (null == kbEditReq.getId() || kbEditReq.getId() < 1) {
            User user = MockUser.getCurrentUser();
            uuid = UUID.randomUUID().toString().replace("-", "");
            knowledgeBase.setUuid(uuid);
            knowledgeBase.setOwnerId(user.getId());
            knowledgeBase.setOwnerUuid(user.getUuid());
            knowledgeBase.setOwnerName(user.getName());
            baseMapper.insert(knowledgeBase);
        } else {
            checkPrivilege(kbEditReq.getId(), null);
            knowledgeBase.setId(kbEditReq.getId());
            baseMapper.updateById(knowledgeBase);
        }
        return ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(KnowledgeBase::getUuid, uuid)
                .one();
    }

    public File uploadDoc(String kbUuid, Boolean embedding, MultipartFile doc) {
        KnowledgeBase knowledgeBase = ChainWrappers.lambdaQueryChain(baseMapper)
                .eq(KnowledgeBase::getUuid, kbUuid)
                .eq(KnowledgeBase::getIsDeleted, false)
                .oneOpt()
                .orElseThrow(() -> new BaseException(A_DATA_NOT_FOUND));
        try {
            Document document = loadDocument(toPath("documents/sushi.txt"), new TextDocumentParser());
            String fileName = doc.getOriginalFilename();
            File file = fileService.writeToLocal(doc);
            //创建知识库条目
            String uuid = UUID.randomUUID().toString().replace("-", "");
            KnowledgeBaseItem knowledgeBaseItem = new KnowledgeBaseItem();
            knowledgeBaseItem.setUuid(uuid);
            knowledgeBaseItem.setKbId(knowledgeBase.getId());
            knowledgeBaseItem.setKbUuid(knowledgeBase.getUuid());
            knowledgeBaseItem.setSourceFileId(file.getId());
            knowledgeBaseItem.setTitle(fileName);
            knowledgeBaseItem.setBrief(StringUtils.substring(document.text(), 0, 200));
            knowledgeBaseItem.setRemark(document.text());
            knowledgeBaseItem.setIsEmbedded(true);
            boolean success = knowledgeBaseItemService.save(knowledgeBaseItem);
            if (success && Boolean.TRUE.equals(embedding)) {
                knowledgeBaseItem = knowledgeBaseItemService.getEnable(uuid);

                //向量化
                Document docWithoutPath = new Document(document.text());
                docWithoutPath.metadata()
                        .add(Constant.EmbeddingMetadataKey.KB_UUID, knowledgeBase.getUuid())
                        .add(Constant.EmbeddingMetadataKey.KB_ITEM_UUID, knowledgeBaseItem.getUuid());
                ragService.ingest(docWithoutPath);

                knowledgeBaseItemService
                        .lambdaUpdate()
                        .eq(KnowledgeBaseItem::getId, knowledgeBaseItem.getId())
                        .set(KnowledgeBaseItem::getIsEmbedded, true)
                        .update();

                updateStatistic(knowledgeBase.getUuid());
            }
            return file;
        } catch (Exception e) {
            log.error("upload error", e);
            throw new BaseException(A_UPLOAD_FAIL);
        }
    }

    public void updateStatistic(String kbUuid) {
        stringRedisTemplate.opsForSet().add(RedisKeyConstant.KB_STATISTIC_RECALCULATE_SIGNAL, kbUuid);
    }

    private void checkPrivilege(Long kbId, String kbUuid) {
        if (null == kbId && StringUtils.isBlank(kbUuid)) {
            throw new BaseException(A_PARAMS_ERROR);
        }
        User user = MockUser.getCurrentUser();
        if (null == user) {
            throw new BaseException(A_USER_NOT_EXIST);
        }
        boolean privilege = user.getIsAdmin();
        if (privilege) {
            return;
        }
        LambdaQueryWrapper<KnowledgeBase> wrapper = new LambdaQueryWrapper();
        wrapper.eq(KnowledgeBase::getOwnerId, user.getId());
        if (null != kbId) {
            wrapper = wrapper.eq(KnowledgeBase::getId, kbId);
        } else if (StringUtils.isNotBlank(kbUuid)) {
            wrapper = wrapper.eq(KnowledgeBase::getUuid, kbUuid);
        }
        boolean exists = baseMapper.exists(wrapper);
        if (!exists) {
            throw new BaseException(A_USER_NOT_AUTH);
        }
    }
}
