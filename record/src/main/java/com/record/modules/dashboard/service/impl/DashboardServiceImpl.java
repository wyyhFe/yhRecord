package com.record.modules.dashboard.service.impl;

import com.record.modules.checkin.mapper.CheckinRecordMapper;
import com.record.modules.dashboard.model.vo.DashboardStatsVO;
import com.record.modules.dashboard.service.DashboardService;
import com.record.modules.diary.mapper.DiaryMapper;
import com.record.modules.knowledge.mapper.KnowledgeBaseMapper;
import com.record.modules.ledger.mapper.LedgerEntryMapper;
import com.record.modules.memorial.mapper.MemorialDayMapper;
import com.record.modules.user.mapper.UserMapper;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {

    private final UserMapper userMapper;
    private final DiaryMapper diaryMapper;
    private final CheckinRecordMapper checkinRecordMapper;
    private final LedgerEntryMapper ledgerEntryMapper;
    private final MemorialDayMapper memorialDayMapper;
    private final KnowledgeBaseMapper knowledgeBaseMapper;

    public DashboardServiceImpl(UserMapper userMapper,
                                DiaryMapper diaryMapper,
                                CheckinRecordMapper checkinRecordMapper,
                                LedgerEntryMapper ledgerEntryMapper,
                                MemorialDayMapper memorialDayMapper,
                                KnowledgeBaseMapper knowledgeBaseMapper) {
        this.userMapper = userMapper;
        this.diaryMapper = diaryMapper;
        this.checkinRecordMapper = checkinRecordMapper;
        this.ledgerEntryMapper = ledgerEntryMapper;
        this.memorialDayMapper = memorialDayMapper;
        this.knowledgeBaseMapper = knowledgeBaseMapper;
    }

    @Override
    public DashboardStatsVO getStats() {
        return DashboardStatsVO.builder()
                .userCount(userMapper.selectCount(null))
                .diaryCount(diaryMapper.selectCount(null))
                .checkinCount(checkinRecordMapper.selectCount(null))
                .ledgerCount(ledgerEntryMapper.selectCount(null))
                .memorialCount(memorialDayMapper.selectCount(null))
                .knowledgeCount(knowledgeBaseMapper.selectCount(null))
                .build();
    }
}
