package com.record.common.interceptor;

import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.DateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * MyBatis SQL 日志拦截器。
 * <p>拦截所有 {@link Executor#query} 和 {@link Executor#update} 调用，
 * 将 SQL 模板 + 参数拼接为可执行 SQL 并输出到日志。</p>
 * <p>日志级别 INFO，前缀 {@code [sql]}，方便在控制台中 grep 过滤。</p>
 */
@Component
@Intercepts({
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
})
public class SqlLogInterceptor implements Interceptor {

    private static final Logger log = LoggerFactory.getLogger("com.record.sql");

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long start = System.currentTimeMillis();
        try {
            return invocation.proceed();
        } finally {
            long elapsed = System.currentTimeMillis() - start;

            MappedStatement ms = (MappedStatement) invocation.getArgs()[0];
            Object parameter = invocation.getArgs()[1];
            BoundSql boundSql = ms.getBoundSql(parameter);
            Configuration configuration = ms.getConfiguration();

            String sql = beautifySql(boundSql.getSql());
            String params = resolveParams(configuration, boundSql);

            log.info("[sql] {} | {}ms | {}", sql, elapsed, params);
        }
    }

    /** 将 SQL 压缩为一行（去掉多余换行和空白）。 */
    private String beautifySql(String sql) {
        return sql.trim()
                .replaceAll("\\s+", " ")
                .replaceAll("\\s*,\\s*", ", ")
                .replaceAll("\\s*=\\s*", "=")
                .replaceAll("\\( ", "(")
                .replaceAll(" \\)", ")");
    }

    /**
     * 将 MyBatis 参数占位符替换为实际值（近似值，非真正执行时的值）。
     * 字符串和日期加引号，数值直接输出。
     */
    private String resolveParams(Configuration configuration, BoundSql boundSql) {
        Object parameterObject = boundSql.getParameterObject();
        List<ParameterMapping> mappings = boundSql.getParameterMappings();
        if (mappings == null || mappings.isEmpty()) {
            return "";
        }

        TypeHandlerRegistry registry = configuration.getTypeHandlerRegistry();
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < mappings.size(); i++) {
            if (i > 0) sb.append(", ");
            ParameterMapping mapping = mappings.get(i);
            String propertyName = mapping.getProperty();
            Object value;

            if (boundSql.hasAdditionalParameter(propertyName)) {
                value = boundSql.getAdditionalParameter(propertyName);
            } else if (parameterObject == null) {
                value = null;
            } else if (registry.hasTypeHandler(parameterObject.getClass())) {
                value = parameterObject;
            } else {
                MetaObject metaObject = configuration.newMetaObject(parameterObject);
                value = metaObject.getValue(propertyName);
            }

            sb.append(formatParam(value));
        }

        return sb.toString();
    }

    /** 格式化参数值：字符串加引号，日期格式化，null 输出 null。 */
    private String formatParam(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String s) {
            return "'" + escapeSql(s) + "'";
        }
        if (value instanceof Date d) {
            DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.MEDIUM, Locale.CHINA);
            return "'" + df.format(d) + "'";
        }
        return String.valueOf(value);
    }

    /** 转义 SQL 字符串中的单引号。 */
    private String escapeSql(String s) {
        return s.replace("'", "''");
    }
}
