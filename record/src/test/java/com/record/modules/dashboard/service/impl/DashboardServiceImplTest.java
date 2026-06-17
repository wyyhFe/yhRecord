package com.record.modules.dashboard.service.impl;

import com.record.modules.checkin.mapper.CheckinRecordMapper;
import com.record.modules.dashboard.model.vo.DashboardStatsVO;
import com.record.modules.diary.mapper.DiaryMapper;
import com.record.modules.knowledge.mapper.KnowledgeBaseMapper;
import com.record.modules.ledger.mapper.LedgerEntryMapper;
import com.record.modules.memorial.mapper.MemorialDayMapper;
import com.record.modules.user.mapper.UserMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("DashboardServiceImpl 单元测试")
class DashboardServiceImplTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private DiaryMapper diaryMapper;
    @Mock
    private CheckinRecordMapper checkinRecordMapper;
    @Mock
    private LedgerEntryMapper ledgerEntryMapper;
    @Mock
    private MemorialDayMapper memorialDayMapper;
    @Mock
    private KnowledgeBaseMapper knowledgeBaseMapper;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @Test
    @DisplayName("返回各模块的正确统计数据")
    void getStats_returnsCorrectCounts() {
        when(userMapper.selectCount(null)).thenReturn(10L);
        when(diaryMapper.selectCount(null)).thenReturn(23L);
        when(checkinRecordMapper.selectCount(null)).thenReturn(100L);
        when(ledgerEntryMapper.selectCount(null)).thenReturn(50L);
        when(memorialDayMapper.selectCount(null)).thenReturn(5L);
        when(knowledgeBaseMapper.selectCount(null)).thenReturn(3L);

        DashboardStatsVO stats = dashboardService.getStats();

        assertEquals(10L, stats.getUserCount());
        assertEquals(23L, stats.getDiaryCount());
        assertEquals(100L, stats.getCheckinCount());
        assertEquals(50L, stats.getLedgerCount());
        assertEquals(5L, stats.getMemorialCount());
        assertEquals(3L, stats.getKnowledgeCount());
    }

    @Test
    @DisplayName("各模块为空时返回零")
    void getStats_emptyReturnsZeros() {
        when(userMapper.selectCount(null)).thenReturn(0L);
        when(diaryMapper.selectCount(null)).thenReturn(0L);
        when(checkinRecordMapper.selectCount(null)).thenReturn(0L);
        when(ledgerEntryMapper.selectCount(null)).thenReturn(0L);
        when(memorialDayMapper.selectCount(null)).thenReturn(0L);
        when(knowledgeBaseMapper.selectCount(null)).thenReturn(0L);

        DashboardStatsVO stats = dashboardService.getStats();

        assertEquals(0L, stats.getUserCount());
        assertEquals(0L, stats.getDiaryCount());
        assertEquals(0L, stats.getCheckinCount());
        assertEquals(0L, stats.getLedgerCount());
        assertEquals(0L, stats.getMemorialCount());
        assertEquals(0L, stats.getKnowledgeCount());
    }
}
