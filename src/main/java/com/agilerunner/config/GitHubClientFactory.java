package com.agilerunner.config;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
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
        String jwt = createJWT();
        GitHub gitHubApp = new GitHubBuilder().withJwtToken(jwt).build();

        String installationAccessToken = createInstallationAccessToken(installationId, gitHubApp);

        return new GitHubBuilder()
                .withAppInstallationToken(installationAccessToken)
                .build();
    }

    private String createInstallationAccessToken(long installationId, GitHub gitHubApp) throws IOException {
        return gitHubApp.getApp()
                .getInstallationById(installationId)
                .createToken()
                .create()
                .getToken();
    }

    private String createJWT() throws Exception {
        PrivateKey privateKey = loadPrivateKey();
        Instant now = Instant.now();

        return Jwts.builder()
                .setIssuer(appId)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plusSeconds(600)))
                .signWith(privateKey, SignatureAlgorithm.RS256)
                .compact();
    }

    private PrivateKey loadPrivateKey() throws NoSuchAlgorithmException, InvalidKeySpecException {
        String normalizedPem = normalizePrivateKey(privateKey);
        byte[] keyBytes = Base64.getDecoder().decode(normalizedPem);

        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        return keyFactory.generatePrivate(spec);
    }

    private String normalizePrivateKey(String privateKeyPem) {
        privateKeyPem = privateKeyPem.replace("\\n", "\n");

        return privateKeyPem
                .replaceAll("-----BEGIN PRIVATE KEY-----", "")
                .replaceAll("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
    }
}