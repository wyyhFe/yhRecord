package com.record.modules.memorial.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.record.common.exception.MemorialException;
import com.record.modules.memorial.mapper.MemorialDayMapper;
import com.record.modules.memorial.model.dto.CreateMemorialDayRequest;
import com.record.modules.memorial.model.dto.UpdateMemorialDayRequest;
import com.record.modules.memorial.model.entity.MemorialDay;
import com.record.modules.memorial.model.vo.MemorialDayVO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("MemorialDayServiceImpl 单元测试")
class MemorialDayServiceImplTest {

    @Mock
    private MemorialDayMapper memorialDayMapper;

    @InjectMocks
    private MemorialDayServiceImpl memorialDayService;

    private static final Long USER_ID = 10001L;
    private static final Long MEMORIAL_ID = 1L;

    private MemorialDay createSampleMemorialDay() {
        MemorialDay md = new MemorialDay();
        md.setId(MEMORIAL_ID);
        md.setUserId(USER_ID);
        md.setTitle("第一次旅行");
        md.setType("LOVE");
        md.setMemorialDate(LocalDate.of(2026, 3, 21));
        md.setAnnualRepeat(true);
        md.setRemark("第一次一起去看海");
        return md;
    }

    private CreateMemorialDayRequest createSampleRequest() {
        CreateMemorialDayRequest req = new CreateMemorialDayRequest();
        req.setTitle("第一次旅行");
        req.setType("LOVE");
        req.setMemorialDate(LocalDate.of(2026, 3, 21));
        req.setAnnualRepeat(true);
        req.setRemark("第一次一起去看海");
        return req;
    }

    @Nested
    @DisplayName("create 方法")
    class CreateTests {

        @Test
        @DisplayName("创建纪念日成功")
        void create_success() {
            CreateMemorialDayRequest request = createSampleRequest();
            when(memorialDayMapper.insert(any(MemorialDay.class))).thenReturn(1);

            MemorialDayVO result = memorialDayService.create(USER_ID, request);

            assertNotNull(result);
            assertEquals("第一次旅行", result.getTitle());
            assertEquals("LOVE", result.getType());
            assertEquals(LocalDate.of(2026, 3, 21), result.getMemorialDate());
            assertTrue(result.getAnnualRepeat());
            verify(memorialDayMapper).insert(any(MemorialDay.class));
        }
    }

    @Nested
    @DisplayName("list 方法")
    class ListTests {

        @Test
        @DisplayName("返回用户的所有纪念日")
        void list_returnsAll() {
            MemorialDay md = createSampleMemorialDay();
            when(memorialDayMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(md));

            List<MemorialDayVO> result = memorialDayService.list(USER_ID);

            assertEquals(1, result.size());
            assertEquals("第一次旅行", result.get(0).getTitle());
        }

        @Test
        @DisplayName("无纪念日时返回空列表")
        void list_empty() {
            when(memorialDayMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of());

            List<MemorialDayVO> result = memorialDayService.list(USER_ID);

            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("update 方法")
    class UpdateTests {

        @Test
        @DisplayName("更新纪念日成功")
        void update_success() {
            MemorialDay md = createSampleMemorialDay();
            when(memorialDayMapper.selectById(MEMORIAL_ID)).thenReturn(md);
            when(memorialDayMapper.updateById(any(MemorialDay.class))).thenReturn(1);

            UpdateMemorialDayRequest request = new UpdateMemorialDayRequest();
            request.setTitle("更新后的标题");
            request.setType("WORK");
            request.setMemorialDate(LocalDate.of(2026, 6, 1));
            request.setAnnualRepeat(false);

            MemorialDayVO result = memorialDayService.update(USER_ID, MEMORIAL_ID, request);

            assertEquals("更新后的标题", result.getTitle());
            assertEquals("WORK", result.getType());
            verify(memorialDayMapper).updateById(any(MemorialDay.class));
        }

        @Test
        @DisplayName("更新不存在的纪念日抛异常")
        void update_notFound() {
            when(memorialDayMapper.selectById(999L)).thenReturn(null);

            UpdateMemorialDayRequest request = new UpdateMemorialDayRequest();
            assertThrows(MemorialException.class,
                    () -> memorialDayService.update(USER_ID, 999L, request));
        }

        @Test
        @DisplayName("更新他人纪念日抛异常")
        void update_notOwned() {
            MemorialDay md = createSampleMemorialDay();
            md.setUserId(99999L);
            when(memorialDayMapper.selectById(MEMORIAL_ID)).thenReturn(md);

            UpdateMemorialDayRequest request = new UpdateMemorialDayRequest();
            assertThrows(MemorialException.class,
                    () -> memorialDayService.update(USER_ID, MEMORIAL_ID, request));
        }
    }

    @Nested
    @DisplayName("delete 方法")
    class DeleteTests {

        @Test
        @DisplayName("删除纪念日成功")
        void delete_success() {
            MemorialDay md = createSampleMemorialDay();
            when(memorialDayMapper.selectById(MEMORIAL_ID)).thenReturn(md);
            when(memorialDayMapper.deleteById(MEMORIAL_ID)).thenReturn(1);

            assertDoesNotThrow(() -> memorialDayService.delete(USER_ID, MEMORIAL_ID));
            verify(memorialDayMapper).deleteById(MEMORIAL_ID);
        }

        @Test
        @DisplayName("删除不存在的纪念日抛异常")
        void delete_notFound() {
            when(memorialDayMapper.selectById(999L)).thenReturn(null);

            assertThrows(MemorialException.class,
                    () -> memorialDayService.delete(USER_ID, 999L));
        }
    }

    @Nested
    @DisplayName("listByDate 方法")
    class ListByDateTests {

        @Test
        @DisplayName("按日期查询纪念日（非重复）")
        void listByDate_nonRepeating() {
            MemorialDay md = createSampleMemorialDay();
            md.setAnnualRepeat(false);
            when(memorialDayMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(md));

            List<MemorialDayVO> result = memorialDayService.listByDate(
                    USER_ID, LocalDate.of(2026, 3, 21));

            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("按日期查询纪念日（每年重复）")
        void listByDate_annualRepeat() {
            MemorialDay md = createSampleMemorialDay();
            md.setAnnualRepeat(true);
            md.setMemorialDate(LocalDate.of(2024, 3, 21));
            when(memorialDayMapper.selectList(any(LambdaQueryWrapper.class)))
                    .thenReturn(List.of(md));

            // 不同年份的同一天应该匹配
            List<MemorialDayVO> result = memorialDayService.listByDate(
                    USER_ID, LocalDate.of(2026, 3, 21));

            assertEquals(1, result.size());
        }
    }
}
