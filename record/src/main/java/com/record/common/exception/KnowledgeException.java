package com.record.common.exception;

public class KnowledgeException extends BusinessException {

    public KnowledgeException(String message) {
        super(ErrorCode.KNOWLEDGE_ERROR, message);
    }
}
