package com.agilerunner.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
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
public class GitHubClientFactory {

    @Value("${GITHUB_APP_ID}")
    private String appId;

    @Value("${GITHUB_PRIVATE_KEY}")
    private String privateKey;

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

        String privateKeyContent = privateKeyPem
                .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");

        byte[] keyBytes = Base64.getDecoder().decode(privateKeyContent);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        PrivateKey privateKey = kf.generatePrivate(spec);

        Instant now = Instant.now();

        return Jwts.builder()
                .setIssuer(appId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(600)))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }
}