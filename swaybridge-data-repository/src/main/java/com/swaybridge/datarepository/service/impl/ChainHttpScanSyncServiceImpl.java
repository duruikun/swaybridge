package com.swaybridge.datarepository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swaybridge.datarepository.entity.ChainHttpScanSyncPO;
import com.swaybridge.datarepository.mapper.ChainHttpScanSyncMapper;
import com.swaybridge.datarepository.service.ChainHttpScanSyncService;
import org.springframework.stereotype.Service;

@Service
public class ChainHttpScanSyncServiceImpl extends ServiceImpl<ChainHttpScanSyncMapper, ChainHttpScanSyncPO> implements ChainHttpScanSyncService {
}
