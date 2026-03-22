package com.record.modules.diary.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.record.common.enums.ResourceType;
import com.record.common.enums.VisibilityType;
import com.record.common.exception.DiaryException;
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
import com.record.modules.user.mapper.UserMapper;
import com.record.modules.user.model.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.List;

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
    private final UserMapper userMapper;
    private final RecycleBinService recycleBinService;
    private final LocationService locationService;

    public DiaryServiceImpl(DiaryMapper diaryMapper,
                            DiaryMediaMapper diaryMediaMapper,
                            DiaryTagRelMapper diaryTagRelMapper,
                            DiaryCommentMapper diaryCommentMapper,
                            DiaryLikeMapper diaryLikeMapper,
                            UserMapper userMapper,
                            RecycleBinService recycleBinService,
                            LocationService locationService) {
        this.diaryMapper = diaryMapper;
        this.diaryMediaMapper = diaryMediaMapper;
        this.diaryTagRelMapper = diaryTagRelMapper;
        this.diaryCommentMapper = diaryCommentMapper;
        this.diaryLikeMapper = diaryLikeMapper;
        this.userMapper = userMapper;
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
    public Page<DiaryVO> list(Long userId, long current, long size, VisibilityType visibility, Long tagId, String keyword) {
        Page<Diary> page = diaryMapper.selectPage(new Page<>(current, size), new LambdaQueryWrapper<Diary>()
                .eq(Diary::getUserId, userId)
                .isNull(Diary::getDeletedAt)
                .eq(visibility != null, Diary::getVisibility, visibility)
                .and(keyword != null && !keyword.isBlank(),
                        wrapper -> wrapper.like(Diary::getTitle, keyword).or().like(Diary::getContent, keyword))
                .orderByDesc(Diary::getRecordDate));

        User user = userMapper.selectById(userId);
        List<DiaryVO> records = page.getRecords().stream()
                .filter(item -> tagId == null || hasTag(item.getId(), tagId))
                .map(item -> buildVO(item, user))
                .toList();

        Page<DiaryVO> result = new Page<>(current, size, page.getTotal());
        result.setRecords(records);
        return result;
    }

    @Override
    public DiaryVO detail(Long userId, Long diaryId) {
        return buildVO(requireOwnedDiary(userId, diaryId, false), userMapper.selectById(userId));
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
        User user = userMapper.selectById(userId);
        return diaryMapper.selectList(new LambdaQueryWrapper<Diary>()
                        .eq(Diary::getUserId, userId)
                        .eq(Diary::getRecordDate, date)
                        .isNull(Diary::getDeletedAt))
                .stream()
                .map(item -> buildVO(item, user))
                .toList();
    }

    @Override
    public long countByUser(Long userId) {
        return diaryMapper.selectCount(new LambdaQueryWrapper<Diary>()
                .eq(Diary::getUserId, userId)
                .isNull(Diary::getDeletedAt));
    }

    @Override
    @Transactional
    public void toggleLike(Long userId, Long diaryId) {
        Diary diary = requireOwnedDiary(userId, diaryId, false);
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
        Diary diary = requireOwnedDiary(userId, diaryId, false);
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
        requireOwnedDiary(userId, diaryId, false);
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

    private boolean hasTag(Long diaryId, Long tagId) {
        return diaryTagRelMapper.selectCount(new LambdaQueryWrapper<DiaryTagRel>()
                .eq(DiaryTagRel::getDiaryId, diaryId)
                .eq(DiaryTagRel::getTagId, tagId)) > 0;
    }

    /**
     * 校验日记是否存在、是否归当前用户所有，以及是否允许读取已删除数据。
     */
    private Diary requireOwnedDiary(Long userId, Long diaryId, boolean includeDeleted) {
        Diary diary = diaryMapper.selectById(diaryId);
        if (diary == null || !diary.getUserId().equals(userId) || (!includeDeleted && diary.getDeletedAt() != null)) {
            throw new DiaryException("日记不存在或无权限访问");
        }
        return diary;
    }

    /**
     * 组装前端需要的日记详情对象。
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
