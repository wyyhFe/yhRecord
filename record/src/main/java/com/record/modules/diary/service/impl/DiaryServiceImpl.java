package com.record.modules.diary.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.record.common.enums.ResourceType;
import com.record.common.enums.VisibilityType;
import com.record.common.exception.DiaryException;
import com.record.common.context.UserContext;
import com.record.common.util.AuthUtil;
import com.record.common.util.PageQuery;
import com.record.common.cache.DictionaryCacheService;
import com.record.modules.diary.mapper.DiaryCommentMapper;
import com.record.modules.diary.mapper.DiaryLikeMapper;
import com.record.modules.diary.mapper.DiaryMapper;
import com.record.modules.diary.mapper.DiaryMediaMapper;
import com.record.modules.diary.mapper.DiaryTagRelMapper;
import com.record.modules.diary.model.dto.CreateDiaryRequest;
import com.record.modules.diary.model.dto.DiaryCommentRequest;
import com.record.modules.diary.model.dto.DiaryLocationDTO;
import com.record.modules.diary.model.dto.DiaryMediaDTO;
import com.record.modules.diary.model.dto.UpdateDiaryRequest;
import com.record.modules.diary.model.entity.Diary;
import com.record.modules.diary.model.entity.DiaryComment;
import com.record.modules.diary.model.entity.DiaryLike;
import com.record.modules.diary.model.entity.DiaryMedia;
import com.record.modules.diary.model.entity.DiaryTagRel;
import com.record.modules.diary.model.vo.DiaryCommentVO;
import com.record.modules.diary.model.vo.DiaryVO;
import com.record.modules.diary.service.DiaryService;
import com.record.modules.location.service.LocationService;
import com.record.modules.recycle.service.RecycleBinService;
import com.record.modules.user.model.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 日记服务实现。
 * 统一处理日记主表、附件、标签、点赞、评论和回收站逻辑。
 */
@Service
public class DiaryServiceImpl implements DiaryService {

    private final DiaryMapper diaryMapper;
    private final DiaryMediaMapper diaryMediaMapper;
    private final DiaryTagRelMapper diaryTagRelMapper;
    private final DiaryCommentMapper diaryCommentMapper;
    private final DiaryLikeMapper diaryLikeMapper;
    private final DictionaryCacheService dictionaryCache;
    private final RecycleBinService recycleBinService;
    private final LocationService locationService;

    public DiaryServiceImpl(DiaryMapper diaryMapper,
                            DiaryMediaMapper diaryMediaMapper,
                            DiaryTagRelMapper diaryTagRelMapper,
                            DiaryCommentMapper diaryCommentMapper,
                            DiaryLikeMapper diaryLikeMapper,
                            DictionaryCacheService dictionaryCache,
                            RecycleBinService recycleBinService,
                            LocationService locationService) {
        this.diaryMapper = diaryMapper;
        this.diaryMediaMapper = diaryMediaMapper;
        this.diaryTagRelMapper = diaryTagRelMapper;
        this.diaryCommentMapper = diaryCommentMapper;
        this.diaryLikeMapper = diaryLikeMapper;
        this.dictionaryCache = dictionaryCache;
        this.recycleBinService = recycleBinService;
        this.locationService = locationService;
    }

    @Override
    @Transactional
    public DiaryVO create(Long userId, CreateDiaryRequest request) {
        Diary diary = new Diary();
        fillDiary(diary, userId, request);
        diary.setLikeCount(0);
        diary.setCommentCount(0);
        diaryMapper.insert(diary);
        replaceMediaAndTags(diary.getId(), request.getMediaList(), request.getTagIds());
        return detail(userId, diary.getId());
    }

    @Override
    @Transactional
    public DiaryVO update(Long userId, Long diaryId, UpdateDiaryRequest request) {
        Diary diary = requireOwnedDiary(userId, diaryId, false);
        fillDiary(diary, userId, request);
        diaryMapper.updateById(diary);
        replaceMediaAndTags(diaryId, request.getMediaList(), request.getTagIds());
        return detail(userId, diaryId);
    }

    @Override
    public Page<DiaryVO> list(Long userId, PageQuery pageQuery, VisibilityType visibility, Long tagId, String keyword) {
        LambdaQueryWrapper<Diary> wrapper = new LambdaQueryWrapper<Diary>()
                .isNull(Diary::getDeletedAt)
                .eq(visibility != null, Diary::getVisibility, visibility)
                .and(keyword != null && !keyword.isBlank(),
                        w -> w.like(Diary::getTitle, keyword).or().like(Diary::getContent, keyword))
                .orderByDesc(Diary::getRecordDate);
        // 管理员查看全量，普通用户只看自己的
        if (!AuthUtil.isAdmin()) {
            wrapper.eq(Diary::getUserId, userId);
        }
        Page<Diary> page = diaryMapper.selectPage(pageQuery.toPage(), wrapper);

        List<Diary> diaryList = page.getRecords();
        if (diaryList.isEmpty()) {
            return new Page<>(pageQuery.getCurrent(), pageQuery.getSize(), 0);
        }

        // 批量加载关联数据，消除 N+1 查询
        List<Long> diaryIds = diaryList.stream().map(Diary::getId).toList();

        // 批量查 media: diaryId -> List<filePath>
        Map<Long, List<String>> mediaMap = diaryMediaMapper.selectList(
                        new LambdaQueryWrapper<DiaryMedia>()
                                .in(DiaryMedia::getDiaryId, diaryIds)
                                .orderByAsc(DiaryMedia::getSortOrder))
                .stream()
                .collect(Collectors.groupingBy(
                        DiaryMedia::getDiaryId,
                        Collectors.mapping(DiaryMedia::getFilePath, Collectors.toList())
                ));

        // 批量查 tag 关系: diaryId -> List<tagId>
        Map<Long, List<Long>> tagMap = diaryTagRelMapper.selectList(
                        new LambdaQueryWrapper<DiaryTagRel>()
                                .in(DiaryTagRel::getDiaryId, diaryIds))
                .stream()
                .collect(Collectors.groupingBy(
                        DiaryTagRel::getDiaryId,
                        Collectors.mapping(DiaryTagRel::getTagId, Collectors.toList())
                ));

        // 按日期降序排列，在内存中过滤 tag
        List<DiaryVO> records = diaryList.stream()
                .filter(item -> tagId == null || tagMap.getOrDefault(item.getId(), List.of()).contains(tagId))
                .map(item -> buildVO(item, mediaMap.getOrDefault(item.getId(), List.of()),
                        tagMap.getOrDefault(item.getId(), List.of())))
                .toList();

        Page<DiaryVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public DiaryVO detail(Long userId, Long diaryId) {
        return buildVO(requireOwnedDiary(userId, diaryId, false), dictionaryCache.getUserById(userId));
    }

    @Override
    @Transactional
    public void delete(Long userId, Long diaryId) {
        Diary diary = requireOwnedDiary(userId, diaryId, false);
        diary.setDeletedAt(LocalDateTime.now());
        diaryMapper.updateById(diary);
        recycleBinService.add(userId, ResourceType.DIARY, diaryId);
    }

    @Override
    @Transactional
    public void restore(Long userId, Long diaryId) {
        Diary diary = requireOwnedDiary(userId, diaryId, true);
        diary.setDeletedAt(null);
        diaryMapper.updateById(diary);
        recycleBinService.removeByResource(ResourceType.DIARY, diaryId);
    }

    @Override
    @Transactional
    public void forceDelete(Long userId, Long diaryId) {
        requireOwnedDiary(userId, diaryId, true);
        diaryMediaMapper.delete(new LambdaQueryWrapper<DiaryMedia>().eq(DiaryMedia::getDiaryId, diaryId));
        diaryTagRelMapper.delete(new LambdaQueryWrapper<DiaryTagRel>().eq(DiaryTagRel::getDiaryId, diaryId));
        diaryCommentMapper.delete(new LambdaQueryWrapper<DiaryComment>().eq(DiaryComment::getDiaryId, diaryId));
        diaryLikeMapper.delete(new LambdaQueryWrapper<DiaryLike>().eq(DiaryLike::getDiaryId, diaryId));
        diaryMapper.deleteById(diaryId);
        recycleBinService.removeByResource(ResourceType.DIARY, diaryId);
    }

    @Override
    public List<DiaryVO> listByDate(Long userId, LocalDate date) {
        LambdaQueryWrapper<Diary> wrapper = new LambdaQueryWrapper<Diary>()
                .eq(Diary::getRecordDate, date)
                .isNull(Diary::getDeletedAt);
        if (!AuthUtil.isAdmin()) {
            wrapper.eq(Diary::getUserId, userId);
        }
        List<Diary> diaryList = diaryMapper.selectList(wrapper);
        if (diaryList.isEmpty()) {
            return List.of();
        }

        // 批量加载 media/tag 数据，消除 N+1
        List<Long> diaryIds = diaryList.stream().map(Diary::getId).toList();
        Map<Long, List<String>> mediaMap = diaryMediaMapper.selectList(
                        new LambdaQueryWrapper<DiaryMedia>()
                                .in(DiaryMedia::getDiaryId, diaryIds)
                                .orderByAsc(DiaryMedia::getSortOrder))
                .stream()
                .collect(Collectors.groupingBy(
                        DiaryMedia::getDiaryId,
                        Collectors.mapping(DiaryMedia::getFilePath, Collectors.toList())
                ));
        Map<Long, List<Long>> tagMap = diaryTagRelMapper.selectList(
                        new LambdaQueryWrapper<DiaryTagRel>()
                                .in(DiaryTagRel::getDiaryId, diaryIds))
                .stream()
                .collect(Collectors.groupingBy(
                        DiaryTagRel::getDiaryId,
                        Collectors.mapping(DiaryTagRel::getTagId, Collectors.toList())
                ));

        return diaryList.stream()
                .map(item -> buildVO(item, mediaMap.getOrDefault(item.getId(), List.of()),
                        tagMap.getOrDefault(item.getId(), List.of())))
                .toList();
    }

    @Override
    public long countByUser(Long userId) {
        return diaryMapper.selectCount(new LambdaQueryWrapper<Diary>()
                .eq(Diary::getUserId, userId)
                .isNull(Diary::getDeletedAt));
    }

    @Override
    public Map<LocalDate, Long> countByDateRange(Long userId, LocalDate start, LocalDate end) {
        List<Map<String, Object>> rows = diaryMapper.countByDateRange(userId, start, end);
        Map<LocalDate, Long> result = new HashMap<>();
        for (Map<String, Object> row : rows) {
            LocalDate date = (LocalDate) row.get("date");
            Long cnt = ((Number) row.get("cnt")).longValue();
            result.put(date, cnt);
        }
        return result;
    }

    @Override
    public Page<DiaryVO> hall(PageQuery pageQuery) {
        LambdaQueryWrapper<Diary> wrapper = new LambdaQueryWrapper<Diary>()
                .isNull(Diary::getDeletedAt)
                .eq(Diary::getVisibility, VisibilityType.PUBLIC)
                .orderByDesc(Diary::getCreatedAt);
        Page<Diary> page = diaryMapper.selectPage(pageQuery.toPage(), wrapper);

        List<Diary> diaryList = page.getRecords();
        if (diaryList.isEmpty()) {
            return new Page<>(pageQuery.getCurrent(), pageQuery.getSize(), 0);
        }

        List<Long> diaryIds = diaryList.stream().map(Diary::getId).toList();

        Map<Long, List<String>> mediaMap = diaryMediaMapper.selectList(
                        new LambdaQueryWrapper<DiaryMedia>()
                                .in(DiaryMedia::getDiaryId, diaryIds)
                                .orderByAsc(DiaryMedia::getSortOrder))
                .stream()
                .collect(Collectors.groupingBy(
                        DiaryMedia::getDiaryId,
                        Collectors.mapping(DiaryMedia::getFilePath, Collectors.toList())
                ));

        Map<Long, List<Long>> tagMap = diaryTagRelMapper.selectList(
                        new LambdaQueryWrapper<DiaryTagRel>()
                                .in(DiaryTagRel::getDiaryId, diaryIds))
                .stream()
                .collect(Collectors.groupingBy(
                        DiaryTagRel::getDiaryId,
                        Collectors.mapping(DiaryTagRel::getTagId, Collectors.toList())
                ));

        List<DiaryVO> records = diaryList.stream()
                .map(item -> buildVO(item, mediaMap.getOrDefault(item.getId(), List.of()),
                        tagMap.getOrDefault(item.getId(), List.of())))
                .toList();

        Page<DiaryVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public Page<DiaryVO> publicListByUser(Long userId, PageQuery pageQuery) {
        LambdaQueryWrapper<Diary> wrapper = new LambdaQueryWrapper<Diary>()
                .isNull(Diary::getDeletedAt)
                .eq(Diary::getUserId, userId)
                .eq(Diary::getVisibility, VisibilityType.PUBLIC)
                .orderByDesc(Diary::getRecordDate);
        Page<Diary> page = diaryMapper.selectPage(pageQuery.toPage(), wrapper);

        List<Diary> diaryList = page.getRecords();
        if (diaryList.isEmpty()) {
            return new Page<>(pageQuery.getCurrent(), pageQuery.getSize(), 0);
        }

        List<Long> diaryIds = diaryList.stream().map(Diary::getId).toList();

        Map<Long, List<String>> mediaMap = diaryMediaMapper.selectList(
                        new LambdaQueryWrapper<DiaryMedia>()
                                .in(DiaryMedia::getDiaryId, diaryIds)
                                .orderByAsc(DiaryMedia::getSortOrder))
                .stream()
                .collect(Collectors.groupingBy(
                        DiaryMedia::getDiaryId,
                        Collectors.mapping(DiaryMedia::getFilePath, Collectors.toList())
                ));

        Map<Long, List<Long>> tagMap = diaryTagRelMapper.selectList(
                        new LambdaQueryWrapper<DiaryTagRel>()
                                .in(DiaryTagRel::getDiaryId, diaryIds))
                .stream()
                .collect(Collectors.groupingBy(
                        DiaryTagRel::getDiaryId,
                        Collectors.mapping(DiaryTagRel::getTagId, Collectors.toList())
                ));

        List<DiaryVO> records = diaryList.stream()
                .map(item -> buildVO(item, mediaMap.getOrDefault(item.getId(), List.of()),
                        tagMap.getOrDefault(item.getId(), List.of())))
                .toList();

        Page<DiaryVO> result = new Page<>(page.getCurrent(), page.getSize(), page.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public DiaryVO publicDetail(Long diaryId) {
        Diary diary = diaryMapper.selectById(diaryId);
        if (diary == null || diary.getDeletedAt() != null) {
            throw new DiaryException("日记不存在");
        }
        if (diary.getVisibility() != VisibilityType.PUBLIC) {
            throw new DiaryException("该日记不是公开日记，无法查看");
        }
        return buildVO(diary, dictionaryCache.getUserById(diary.getUserId()));
    }

    @Override
    public long countPublicByUser(Long userId) {
        return diaryMapper.selectCount(new LambdaQueryWrapper<Diary>()
                .eq(Diary::getUserId, userId)
                .eq(Diary::getVisibility, VisibilityType.PUBLIC)
                .isNull(Diary::getDeletedAt));
    }

    @Override
    @Transactional
    public void toggleLike(Long userId, Long diaryId) {
        Diary diary = requireAccessibleDiary(diaryId);
        DiaryLike existed = diaryLikeMapper.selectOne(new LambdaQueryWrapper<DiaryLike>()
                .eq(DiaryLike::getDiaryId, diaryId)
                .eq(DiaryLike::getUserId, userId));
        int likeCount = diary.getLikeCount() == null ? 0 : diary.getLikeCount();

        // 已点赞则取消，否则新增点赞。
        if (existed == null) {
            DiaryLike like = new DiaryLike();
            like.setDiaryId(diaryId);
            like.setUserId(userId);
            diaryLikeMapper.insert(like);
            diary.setLikeCount(likeCount + 1);
        } else {
            diaryLikeMapper.deleteById(existed.getId());
            diary.setLikeCount(Math.max(likeCount - 1, 0));
        }
        diaryMapper.updateById(diary);
    }

    @Override
    @Transactional
    public void comment(Long userId, Long diaryId, DiaryCommentRequest request) {
        Diary diary = requireAccessibleDiary(diaryId);
        Long parentId = request.getParentId();
        if (parentId != null) {
            DiaryComment parentComment = diaryCommentMapper.selectById(parentId);
            if (parentComment == null || !parentComment.getDiaryId().equals(diaryId)) {
                throw new DiaryException("父评论不存在或不属于当前日记");
            }
        }

        DiaryComment comment = new DiaryComment();
        comment.setDiaryId(diaryId);
        comment.setUserId(userId);
        comment.setParentId(parentId);
        comment.setContent(request.getContent());
        diaryCommentMapper.insert(comment);

        diary.setCommentCount((diary.getCommentCount() == null ? 0 : diary.getCommentCount()) + 1);
        diaryMapper.updateById(diary);
    }

    @Override
    public List<DiaryCommentVO> comments(Long userId, Long diaryId) {
        requireAccessibleDiary(diaryId);
        return diaryCommentMapper.selectList(new LambdaQueryWrapper<DiaryComment>()
                        .eq(DiaryComment::getDiaryId, diaryId)
                        .orderByAsc(DiaryComment::getCreatedAt)
                        .orderByAsc(DiaryComment::getId))
                .stream()
                .map(item -> DiaryCommentVO.builder()
                        .id(item.getId())
                        .diaryId(item.getDiaryId())
                        .userId(item.getUserId())
                        .parentId(item.getParentId())
                        .content(item.getContent())
                        .createdAt(item.getCreatedAt())
                        .build())
                .toList();
    }

    /**
     * 将请求体字段写入日记实体。
     * 如果前端只传经纬度，服务端会兜底补齐结构化地址。
     */
    private void fillDiary(Diary diary, Long userId, CreateDiaryRequest request) {
        diary.setUserId(userId);
        diary.setTitle(request.getTitle());
        diary.setContent(request.getContent());
        diary.setRecordDate(request.getRecordDate());
        diary.setWeather(request.getWeather());
        diary.setMood(request.getMood());
        diary.setVisibility(request.getVisibility());
        DiaryLocationDTO location = request.getLocation();
        if (location != null) {
            diary.setLocationName(location.getLocationName());
            diary.setAddress(location.getAddress());
            diary.setProvince(location.getProvince());
            diary.setCity(location.getCity());
            diary.setDistrict(location.getDistrict());
            diary.setLatitude(location.getLatitude());
            diary.setLongitude(location.getLongitude());
            diary.setLocationSourceType(location.getSourceType());

            boolean needGeoFill =
                    (location.getAddress() == null || location.getAddress().isBlank())
                            || (location.getProvince() == null || location.getProvince().isBlank())
                            || (location.getCity() == null || location.getCity().isBlank())
                            || (location.getDistrict() == null || location.getDistrict().isBlank());

            if (needGeoFill && location.getLatitude() != null && location.getLongitude() != null) {
                try {
                    var geo = locationService.reverseGeocode(location.getLatitude(), location.getLongitude());
                    if (diary.getAddress() == null || diary.getAddress().isBlank()) {
                        diary.setAddress(geo.getAddress());
                    }
                    if (diary.getProvince() == null || diary.getProvince().isBlank()) {
                        diary.setProvince(geo.getProvince());
                    }
                    if (diary.getCity() == null || diary.getCity().isBlank()) {
                        diary.setCity(geo.getCity());
                    }
                    if (diary.getDistrict() == null || diary.getDistrict().isBlank()) {
                        diary.setDistrict(geo.getDistrict());
                    }
                } catch (RuntimeException ignored) {
                    // 地图服务只用于补全地址信息，不应阻断日记保存主流程。
                }
            }
        }
    }

    /**
     * 用全量覆盖的方式更新附件和标签关系。
     */
    private void replaceMediaAndTags(Long diaryId, List<DiaryMediaDTO> mediaList, List<Long> tagIds) {
        diaryMediaMapper.delete(new LambdaQueryWrapper<DiaryMedia>().eq(DiaryMedia::getDiaryId, diaryId));
        diaryTagRelMapper.delete(new LambdaQueryWrapper<DiaryTagRel>().eq(DiaryTagRel::getDiaryId, diaryId));

        if (mediaList != null) {
            for (DiaryMediaDTO media : mediaList) {
                DiaryMedia entity = new DiaryMedia();
                entity.setDiaryId(diaryId);
                entity.setMediaType(media.getMediaType());
                entity.setFilePath(media.getFilePath());
                entity.setSortOrder(media.getSortOrder());
                diaryMediaMapper.insert(entity);
            }
        }

        if (tagIds != null) {
            for (Long tagId : tagIds) {
                DiaryTagRel rel = new DiaryTagRel();
                rel.setDiaryId(diaryId);
                rel.setTagId(tagId);
                diaryTagRelMapper.insert(rel);
            }
        }
    }

    /**
     * 校验日记是否存在、是否归当前用户所有，以及是否允许读取已删除数据。
     */
    private Diary requireOwnedDiary(Long userId, Long diaryId, boolean includeDeleted) {
        Diary diary = diaryMapper.selectById(diaryId);
        if (diary == null) {
            throw new DiaryException("日记不存在");
        }
        // 管理员可操作任意日记
        if (AuthUtil.isAdmin()) {
            return diary;
        }
        if (!diary.getUserId().equals(userId) || (!includeDeleted && diary.getDeletedAt() != null)) {
            throw new DiaryException("日记不存在或无权限访问");
        }
        return diary;
    }

    /**
     * 校验日记是否可被当前用户访问（自己的日记或公开日记）。
     * 用于点赞、评论等互动操作，允许对公开日记进行互动。
     */
    private Diary requireAccessibleDiary(Long diaryId) {
        Diary diary = diaryMapper.selectById(diaryId);
        if (diary == null || diary.getDeletedAt() != null) {
            throw new DiaryException("日记不存在");
        }
        if (AuthUtil.isAdmin()) {
            return diary;
        }
        Long currentUserId = UserContext.getUserId();
        if (!diary.getUserId().equals(currentUserId) && diary.getVisibility() != VisibilityType.PUBLIC) {
            throw new DiaryException("日记不存在或无权限访问");
        }
        return diary;
    }

    /**
     * 组装前端需要的日记详情对象（批量模式，接收预加载的 media/tag 数据）。
     * 用于 list() 分页查询，避免逐条查询 N+1。
     */
    private DiaryVO buildVO(Diary diary, List<String> preloadedMediaPaths, List<Long> preloadedTagIds) {
        User user = dictionaryCache.getUserById(diary.getUserId());
        return DiaryVO.builder()
                .id(diary.getId())
                .authorId(diary.getUserId())
                .authorNickname(user != null ? user.getNickname() : null)
                .authorAvatar(user != null ? user.getAvatarPath() : null)
                .title(diary.getTitle())
                .content(diary.getContent())
                .recordDate(diary.getRecordDate())
                .weather(diary.getWeather())
                .mood(diary.getMood())
                .visibility(diary.getVisibility())
                .locationName(diary.getLocationName())
                .address(diary.getAddress())
                .province(diary.getProvince())
                .city(diary.getCity())
                .district(diary.getDistrict())
                .latitude(diary.getLatitude())
                .longitude(diary.getLongitude())
                .locationSourceType(diary.getLocationSourceType())
                .likeCount(diary.getLikeCount())
                .commentCount(diary.getCommentCount())
                .mediaPaths(preloadedMediaPaths)
                .tagIds(preloadedTagIds)
                .ageLabel(buildAgeLabel(user, diary.getRecordDate()))
                .build();
    }

    /**
     * 组装前端需要的日记详情对象（单篇模式，逐条查关联数据）。
     * 仅在 detail / create / update 等单日记操作中使用，不会产生 N+1。
     */
    private DiaryVO buildVO(Diary diary, User user) {
        List<String> mediaPaths = diaryMediaMapper.selectList(new LambdaQueryWrapper<DiaryMedia>()
                        .eq(DiaryMedia::getDiaryId, diary.getId())
                        .orderByAsc(DiaryMedia::getSortOrder))
                .stream()
                .map(DiaryMedia::getFilePath)
                .toList();
        List<Long> tagIds = diaryTagRelMapper.selectList(new LambdaQueryWrapper<DiaryTagRel>()
                        .eq(DiaryTagRel::getDiaryId, diary.getId()))
                .stream()
                .map(DiaryTagRel::getTagId)
                .toList();

        return DiaryVO.builder()
                .id(diary.getId())
                .authorId(diary.getUserId())
                .authorNickname(user != null ? user.getNickname() : null)
                .authorAvatar(user != null ? user.getAvatarPath() : null)
                .title(diary.getTitle())
                .content(diary.getContent())
                .recordDate(diary.getRecordDate())
                .weather(diary.getWeather())
                .mood(diary.getMood())
                .visibility(diary.getVisibility())
                .locationName(diary.getLocationName())
                .address(diary.getAddress())
                .province(diary.getProvince())
                .city(diary.getCity())
                .district(diary.getDistrict())
                .latitude(diary.getLatitude())
                .longitude(diary.getLongitude())
                .locationSourceType(diary.getLocationSourceType())
                .likeCount(diary.getLikeCount())
                .commentCount(diary.getCommentCount())
                .mediaPaths(mediaPaths)
                .tagIds(tagIds)
                .ageLabel(buildAgeLabel(user, diary.getRecordDate()))
                .build();
    }

    /**
     * 计算“记于 22 岁 1 月 10 天”这类年龄文案。
     */
    private String buildAgeLabel(User user, LocalDate recordDate) {
        if (user == null || user.getBirthday() == null || recordDate == null || user.getBirthday().isAfter(recordDate)) {
            return null;
        }
        Period period = Period.between(user.getBirthday(), recordDate);
        return "记于" + period.getYears() + "岁" + period.getMonths() + "月" + period.getDays() + "天";
    }
}
