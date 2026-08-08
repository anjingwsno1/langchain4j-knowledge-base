package com.anjingwsno1.langchain4jknowledgebase.service;

import com.anjingwsno1.langchain4jknowledgebase.common.base.MockUser;
import com.anjingwsno1.langchain4jknowledgebase.common.utils.FileUtil;
import com.anjingwsno1.langchain4jknowledgebase.common.utils.MD5Utils;
import com.anjingwsno1.langchain4jknowledgebase.entity.File;
import com.anjingwsno1.langchain4jknowledgebase.mapper.FileMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
public class FileService extends ServiceImpl<FileMapper, File> {
    @Value("${local.images}")
    private String imagePath;

    public File writeToLocal(MultipartFile file) {
        String md5 = MD5Utils.md5ByMultipartFile(file);
        Optional<File> existFile = this.lambdaQuery()
                .eq(File::getMd5, md5)
                .eq(File::getIsDeleted, false)
                .oneOpt();
        if (existFile.isPresent()) {
            return existFile.get();
        }
        String uuid = UUID.randomUUID().toString().replace("-", "");
        Pair<String, String> originalFile = FileUtil.saveToLocal(file, imagePath, uuid);
        File adiFile = new File();
        adiFile.setName(file.getOriginalFilename());
        adiFile.setUuid(uuid);
        adiFile.setMd5(md5);
        adiFile.setPath(originalFile.getLeft());
        adiFile.setExt(originalFile.getRight());
        adiFile.setUserId(MockUser.getCurrentUser().getId());
        this.getBaseMapper().insert(adiFile);
        return adiFile;
    }
}
