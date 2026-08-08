package com.anjingwsno1.langchain4jknowledgebase.common.constant;

import dev.langchain4j.model.input.PromptTemplate;

public class Constant {

    public static final int DEFAULT_PAGE_SIZE = 10;

    public static final PromptTemplate PROMPT_TEMPLATE = PromptTemplate.from("""
            根据以下已知信息:
            {{information}}
            尽可能准确地回答用户的问题,以下是用户的问题:
            {{question}}
            注意,回答的内容不能让用户感知到已知信息的存在
            """);

    public static class EmbeddingMetadataKey {
        public static final String KB_UUID = "kb_uuid";
        public static final String KB_ITEM_UUID = "kb_item_uuid";
    }

    public static final String[] POI_DOC_TYPES = {"doc", "docx", "ppt", "pptx", "xls", "xlsx"};

    public static class SSEEventName {
        public static final String START = "[START]";
        public static final String DONE = "[DONE]";
        public static final String ERROR = "[ERROR]";
    }
}
