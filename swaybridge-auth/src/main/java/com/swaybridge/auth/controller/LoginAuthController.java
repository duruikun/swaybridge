package com.swaybridge.auth.controller;

import com.swaybridge.datarepository.entity.SwayUserPO;
import com.swaybridge.datarepository.mapper.SwayUserMapper;
import com.swaybridge.datarepository.service.SwayUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("login/auth")
public class LoginAuthController {

    @Autowired
    private SwayUserService swayUserService;

    @Autowired
    private SwayUserMapper swayUserMapper;

    @GetMapping("test")
    public String test() {
        List<SwayUserPO> list = swayUserService.list();

        SwayUserPO swayUserPO = swayUserMapper.queryByXml();


        return list.toString();
    }

}
