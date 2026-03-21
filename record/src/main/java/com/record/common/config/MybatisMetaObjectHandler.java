package com.record.common.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.record.common.context.UserContext;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus 自动填充处理器。
 * 普通请求从用户上下文读取操作人，定时任务等场景统一写入 0。
 */
@Component
public class MybatisMetaObjectHandler implements MetaObjectHandler {

    @Override
    public void insertFill(MetaObject metaObject) {
        Long operatorId = resolveOperatorId();
        this.strictInsertFill(metaObject, "createdAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "createdBy", Long.class, operatorId);
        this.strictInsertFill(metaObject, "updatedBy", Long.class, operatorId);
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        this.strictUpdateFill(metaObject, "updatedAt", LocalDateTime.class, LocalDateTime.now());
        this.strictUpdateFill(metaObject, "updatedBy", Long.class, resolveOperatorId());
    }

    /**
     * 解析当前操作人。
     */
    private Long resolveOperatorId() {
        Long userId = UserContext.getUserId();
        return userId == null ? 0L : userId;
    }
}
