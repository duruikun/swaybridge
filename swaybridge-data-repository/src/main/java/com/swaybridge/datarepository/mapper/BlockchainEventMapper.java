package com.swaybridge.datarepository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swaybridge.common.model.persistence.entity.BlockchainEvent;
import org.springframework.stereotype.Repository;

@Repository
public interface BlockchainEventMapper extends BaseMapper<BlockchainEvent> {
}
