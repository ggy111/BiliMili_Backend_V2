package com.bilimili.buaa13.tools;

import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class JwtTool {
    @Autowired
    private RedisTool redisTool;
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    // 有效期2天，记得修改 UserAccountServiceImpl 的 login 中redis的时间，注意单位，这里是毫秒
    private static final long JWT_TTL = 1000L * 60 * 60 * 24 * 2;
    private static final String JWT_KEY = "bEn2xiAnG0mU2BILIMILI0YOu5HzH0hE1CwJ1GOnG1tOnG6kAifAwAnchEnG";
    private static String getUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    /**
     * 获取token密钥
     * @return 加密后的token密钥
     */
    private static SecretKey getTokenSecret() {
        byte[] encodeKey = Base64.getDecoder().decode(JwtTool.JWT_KEY);
        return new SecretKeySpec(encodeKey, 0, encodeKey.length, "HmacSHA256");
    }

    /**
     * 生成token
     * @param uid 用户id
     * @param role 用户角色 user/admin
     * @return token
     */
    public String createToken(String uid, String role) {
        String uuid = getUUID();
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;
        SecretKey secretKey = getTokenSecret();
        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);
        long expMillis = nowMillis + JwtTool.JWT_TTL;
        Date expDate = new Date(expMillis);

        String token = Jwts.builder()
                .setId(uuid)    // 随机id，用于生成无规则token
                .setSubject(uid)    // 加密主体
                .claim("role", role)    // token角色参数 user/admin 用于区分普通用户和管理员
                .signWith(secretKey, signatureAlgorithm)
                .setIssuedAt(now)
                .setExpiration(expDate)
                .compact();

        try {
            //缓存token信息，管理员和用户之间不要冲突
            //使用 指定有效期 和 指定时间单位 存储简单数据类型
            redisTemplate.opsForValue().set("token:" + role + ":" + uid, token, JwtTool.JWT_TTL, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            log.error("存储redis数据异常", e);
        }
        return token;
    }

    /**
     * 获取Claims信息
     * @param token token
     * @return token的claims
     */
    private static Claims getAllClaimsFromToken(String token) {
        if (StringUtils.isEmpty(token)) {
            return null;
        }
        Claims claims;
        try {
            claims = Jwts.parserBuilder()
                    .setSigningKey(getTokenSecret())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException eje) {
            claims = null;
            log.error("获取token信息异常，jwt已过期");
        } catch (Exception e) {
            claims = null;
            log.error("获取token信息失败", e);
        }
        return claims;
    }

    /**
     * 获取token主题，即uid
     * @param token token
     * @return uid的字符串类型
     */
    public static String getSubjectFromToken(String token) {
        String subject = null;
        try {
            Claims claims = getAllClaimsFromToken(token);
            if (claims != null) {
                subject = claims.getSubject();
            }
        } catch (Exception e) {
            log.error("从token里获取不到主题", e);
        }
        return subject;
    }

    /**
     * 在token里获取对应参数的值
     * @param token token
     * @param param 参数名
     * @return 参数值
     */
    public static String getClaimFromToken(String token, String param) {
        Claims claims = getAllClaimsFromToken(token);
        if (null == claims) {
            return "";
        }
        if (claims.containsKey(param)) {
            return claims.get(param).toString();
        }
        return "";
    }

    /**
     * 校验传送来的token和缓存的token是否一致
     * @param token token
     * @return true/false
     */
    public boolean verifyToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        if (null == claims) {
            return false;
        }
        String uid = claims.getSubject();
        String role;
        if (claims.containsKey("role")) {
            role = claims.get("role").toString();
        } else {
            role = "";
        }
        String cacheToken;
        try {
            cacheToken = String.valueOf(redisTemplate.opsForValue().get("token:" + role + ":" + uid));
        } catch (Exception e) {
            cacheToken = null;
            log.error("获取不到缓存的token", e);
        }
        return StringUtils.equals(token, cacheToken);
    }
}
