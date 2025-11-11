package com.agilerunner.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Configuration
public class GitHubConfig {

    @Value("${GITHUB_APP_ID}")
    private String appId;

    @Value("${GITHUB_PRIVATE_KEY}")
    private String privateKey;

    // GitHub 인스턴스를 빈으로 등록하지 않음!
    // 대신 Factory 메서드 제공

    public GitHub createGitHubClient(long installationId) throws Exception {
        String jwt = createJWT(appId, privateKey);

        GitHub gitHubApp = new GitHubBuilder().withJwtToken(jwt).build();
        String token = gitHubApp.getApp()
                .getInstallationById(installationId)
                .createToken()
                .create()
                .getToken();

        return new GitHubBuilder()
                .withAppInstallationToken(token)
                .build();
    }

    private String createJWT(String appId, String privateKeyPem) throws Exception {
        privateKeyPem = privateKeyPem.replace("\\n", "\n");

        // PEM 형식의 개인키를 PrivateKey 객체로 변환
        String privateKeyContent = privateKeyPem
                .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        // 3. 디버깅 출력
        System.out.println("처리된 Key 길이: " + privateKeyContent.length());
        System.out.println("처리된 Key 첫 30자: " + privateKeyContent.substring(0, Math.min(30, privateKeyContent.length())));

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(spec);

        // JWT 생성 (10분 유효)
        Instant now = Instant.now();

        return Jwts.builder()
                .setIssuer(appId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(600)))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    @PostConstruct
    public void debugPrivateKey() {
        System.out.println("=== Private Key Debug ===");
        System.out.println("원본 길이: " + privateKey.length());
        System.out.println("원본 첫 100자:");
        System.out.println(privateKey.substring(0, Math.min(100, privateKey.length())));

        // \\n이 실제 줄바꿈인지 확인
        if (privateKey.contains("\\n")) {
            System.out.println("⚠️ Warning: \\n이 문자열로 포함됨");
        }

        // 헤더 확인
        if (privateKey.contains("-----BEGIN")) {
            System.out.println("✅ 헤더 포함됨");
        }
    }

}