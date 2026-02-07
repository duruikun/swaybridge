package com.swaybridge.auth.controller;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.swaybridge.auth.model.dto.request.NonceReq;
import com.swaybridge.auth.model.dto.request.VerifyReq;
import com.swaybridge.auth.utils.NonceUtil;
import com.swaybridge.common.constants.RedisKeyConstants;
import com.swaybridge.common.model.dto.response.Result;
import com.swaybridge.datarepository.entity.SwayUserPO;
import com.swaybridge.datarepository.service.SwayUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SignatureException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

@RestController
@CrossOrigin
@RequestMapping("login/auth")
public class LoginAuthController {

    @Autowired
    private SwayUserService swayUserService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @PostMapping("nonce")
    public Result<String> nonce(@RequestBody NonceReq nonceReq) {

        String address = nonceReq.getAddress();
        Integer chainId = nonceReq.getChainId();

        if (StrUtil.isEmpty(address) || chainId == null) {
            return Result.fail("address or chainId is null");
        }

        String nonce = NonceUtil.randomNonce();

        String redisKey = RedisKeyConstants.WEB3_NONCE_PREFIX + chainId + ":" + address.toLowerCase();
        stringRedisTemplate.opsForValue().set(redisKey, nonce, 5 * 60, TimeUnit.SECONDS);

        String msgPrefix = "SwayBridge Login\n\nAddress: %s\nChainId: %d\nNonce: %s\nIssued At: %s";
        String message = String.format(msgPrefix, address, chainId, nonce, DateTimeFormatter.ISO_INSTANT.format(Instant.now()));

        return Result.ok(message);
    }

    @PostMapping("verify")
    public Result<SaTokenInfo> verify(@RequestBody VerifyReq verifyReq) throws SignatureException {
        String redisKey = RedisKeyConstants.WEB3_NONCE_PREFIX + verifyReq.getChainId() + ":" + verifyReq.getAddress().toLowerCase();
        String redisValue = stringRedisTemplate.opsForValue().get(redisKey);

        if (StrUtil.isEmpty(redisValue)) return Result.fail("nonce is expired, request nonce again");

        String message = verifyReq.getMessage();

        if (!message.contains(redisValue)) return Result.fail("nonce don't match, request nonce again");

        byte[] msgHash = Hash.sha3(("\u0019Ethereum Signed Message:\n" + message.length() + message).getBytes(StandardCharsets.UTF_8));

        // 解析签名
        byte[] sigBytes = Numeric.hexStringToByteArray(verifyReq.getSignature());
        if (sigBytes.length != 65) {
            return Result.error("无效的签名长度(不等于65位)");
        }
        byte v = sigBytes[64];
        if (v < 27) v += 27;

        Sign.SignatureData signatureData = new Sign.SignatureData(v, Arrays.copyOfRange(sigBytes, 0, 32), Arrays.copyOfRange(sigBytes, 32, 64));

        // 恢复公钥
        BigInteger publicKey = Sign.signedMessageHashToKey(msgHash, signatureData);
        String recoveredAddress = "0x" + Keys.getAddress(publicKey);

        if (!recoveredAddress.equalsIgnoreCase(verifyReq.getAddress())) {
            return Result.error("签名验证失败, 非本人发起的签名");
        }

        stringRedisTemplate.delete(redisKey);

        // 签名验证通过, 查询数据库
        SwayUserPO one = swayUserService.lambdaQuery().eq(SwayUserPO::getAddress, verifyReq.getAddress()).eq(SwayUserPO::getChainId, verifyReq.getChainId()).one();
        if (ObjUtil.isEmpty(one)) {
            SwayUserPO po = new SwayUserPO();
            po.setAddress(verifyReq.getAddress());
            po.setChainId(verifyReq.getChainId());
            String timeNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            po.setCreateTime(timeNow);
            po.setLastLoginTime(timeNow);
            swayUserService.save(po);
        } else {
            UpdateWrapper<SwayUserPO> updateWrapper = new UpdateWrapper<>();
            updateWrapper.eq("address", verifyReq.getAddress());
            updateWrapper.eq("chain_id", verifyReq.getChainId());
            updateWrapper.set("last_login_time", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            swayUserService.update(updateWrapper);
        }

        StpUtil.login(verifyReq.getAddress() + ":" + verifyReq.getChainId());
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();

        return Result.ok(tokenInfo);
    }

    @GetMapping("getTokenInfo")
    public Result<SaTokenInfo> getTokenInfo() {
        return Result.ok(StpUtil.getTokenInfo());
    }

    @GetMapping("logout")
    public Result<String> logout() {
        StpUtil.logout();
        return Result.ok("当前请求头携带的token对应的用户已成功退出登录");
    }

}
