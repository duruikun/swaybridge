package com.swaybridge.datarepository.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.swaybridge.datarepository.entity.SwayUserPO;
import com.swaybridge.datarepository.mapper.SwayUserMapper;
import com.swaybridge.datarepository.service.SwayUserService;
import org.springframework.stereotype.Service;

@Service
public class SwayUserServiceImpl extends ServiceImpl<SwayUserMapper, SwayUserPO> implements SwayUserService {
}
