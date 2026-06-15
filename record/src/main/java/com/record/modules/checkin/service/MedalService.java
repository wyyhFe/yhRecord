package com.record.modules.checkin.service;

import com.record.modules.checkin.model.vo.MedalVO;

import java.util.List;

/**
 * 勋章服务。
 */
public interface MedalService {

    /**
     * 获取所有勋章（含用户解锁状态）。
     */
    List<MedalVO> listMedals(Long userId);

    /**
     * 打卡后检查并解锁勋章。返回本次新解锁的勋章列表。
     */
    List<MedalVO> checkAndUnlock(Long userId);
}
