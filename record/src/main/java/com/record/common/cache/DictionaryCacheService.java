package com.record.common.cache;

import com.record.modules.tag.model.entity.TagTemplate;
import com.record.modules.tag.mapper.TagTemplateMapper;
import com.record.modules.user.mapper.UserMapper;
import com.record.modules.user.model.entity.User;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 字典数据缓存服务。
 * <p>
 * 封装 User、TagTemplate 等高频读取、低频变更的"字典"数据查询，
 * 通过 Spring Cache + Caffeine 做本地缓存，避免频繁查 DB。
 * </p>
 *
 * <h3>缓存键约定</h3>
 * <ul>
 *   <li>{@code dict:user:{id}} — 用户基础信息</li>
 *   <li>{@code dict:tag:templates} — 标签模板全量列表</li>
 * </ul>
 *
 * <h3>失效时机</h3>
 * <ul>
 *   <li>用户修改昵称/生日等资料 → 需手动 evict 用户缓存（见 UserServiceImpl.updateProfile）</li>
 *   <li>标签模板被管理员修改 → 需手动 evict 模板缓存</li>
 * </ul>
 */
@Service
public class DictionaryCacheService {

    private final UserMapper userMapper;
    private final TagTemplateMapper tagTemplateMapper;

    public DictionaryCacheService(UserMapper userMapper,
                                  TagTemplateMapper tagTemplateMapper) {
        this.userMapper = userMapper;
        this.tagTemplateMapper = tagTemplateMapper;
    }

    /**
     * 按用户 ID 查询用户信息，缓存 10 分钟。
     */
    @Cacheable(value = "dict:user", key = "#userId")
    public User getUserById(Long userId) {
        return userMapper.selectById(userId);
    }

    /**
     * 清除指定用户的缓存（用户修改资料后调用）。
     */
    @CacheEvict(value = "dict:user", key = "#userId")
    public void evictUser(Long userId) {
        // 注解自动清理
    }

    /**
     * 查询所有启用的标签模板，缓存 10 分钟。
     */
    @Cacheable(value = "dict:tagTemplates")
    public List<TagTemplate> getAllTagTemplates() {
        return tagTemplateMapper.selectList(null);
    }

    /**
     * 清除标签模板缓存（模板被增删改后调用）。
     */
    @CacheEvict(value = "dict:tagTemplates", allEntries = true)
    public void evictTagTemplates() {
        // 注解自动清理
    }
}
