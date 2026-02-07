package com.swaybridge.datarepository.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.swaybridge.datarepository.entity.SwayUserPO;
import org.springframework.stereotype.Repository;

@Repository
public interface SwayUserMapper extends BaseMapper<SwayUserPO> {

    SwayUserPO queryByXml();

}
