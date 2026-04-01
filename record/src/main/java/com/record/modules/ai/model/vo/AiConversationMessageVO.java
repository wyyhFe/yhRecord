package com.record.modules.ai.model.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class AiConversationMessageVO {

    private String id;

    private String conversationId;

    private String role;

    private String content;

    private LocalDateTime createdAt;

    private List<AiCitationVO> sources;
}
