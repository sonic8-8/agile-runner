package com.agilerunner.client.github.auth;

import com.agilerunner.domain.exception.AgileRunnerException;
import com.agilerunner.domain.exception.ErrorCode;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.kohsuke.github.GitHub;
import org.kohsuke.github.GitHubBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

@Component
public class GitHubClientFactory {

    @Value("${spring.github.app-id:}")
    private String appId;

    @Value("${spring.github.private-key:}")
    private String privateKey;

    public GitHub createGitHubClient(long installationId) throws Exception {
        validateConfiguration();

        String jwt = createJWT();
        GitHub gitHubApp = new GitHubBuilder().withJwtToken(jwt).build();

        String installationAccessToken = createInstallationAccessToken(installationId, gitHubApp);

        return new GitHubBuilder()
                .withAppInstallationToken(installationAccessToken)
                .build();
    }

    private void validateConfiguration() {
        if (appId == null || appId.isBlank()) {
            throw new AgileRunnerException(
                    ErrorCode.GITHUB_APP_CONFIGURATION_MISSING,
                    "GitHub App ID가 설정되지 않았습니다."
            );
        }
        if (privateKey == null || privateKey.isBlank()) {
            throw new AgileRunnerException(
                    ErrorCode.GITHUB_APP_CONFIGURATION_MISSING,
                    "GitHub Private Key가 설정되지 않았습니다."
            );
        }
    }

    private String createInstallationAccessToken(long installationId, GitHub gitHubApp) throws IOException {
        return gitHubApp.getApp()
                .getInstallationById(installationId)
                .createToken()
                .create()
                .getToken();
    }

    private String createJWT() {
        try {
            PrivateKey privateKey = loadPrivateKey();
            Instant now = Instant.now();

            return Jwts.builder()
                    .setIssuer(appId)
                    .setIssuedAt(Date.from(now))
                    .setExpiration(Date.from(now.plusSeconds(600)))
                    .signWith(privateKey, SignatureAlgorithm.RS256)
                    .compact();
        } catch (Exception exception) {
            throw new AgileRunnerException(
                    ErrorCode.GITHUB_APP_CONFIGURATION_MISSING,
                    "GitHub App 설정을 해석할 수 없습니다.",
                    exception
            );
        }
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
