package com.swaybridge.datarepository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swaybridge.common.model.persistence.entity.BlockchainEvent;
import com.swaybridge.datarepository.entity.BlockchainEventPO;
import com.swaybridge.datarepository.mapper.BlockchainEventMapper;
import com.swaybridge.datarepository.service.BlockchainEventService;
import org.springframework.stereotype.Service;

@Service
public class BlockchainEventServiceImpl extends ServiceImpl<BlockchainEventMapper, BlockchainEventPO> implements BlockchainEventService {
}
