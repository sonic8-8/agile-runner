package com.agilerunner.client.agentruntime;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.IOException;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ManualRerunSeedCommandScriptTest {

  @TempDir
  Path tempDir;

  @DisplayName("유효한 입력으로 준비 스크립트를 실행하면 준비 로그와 H2 적용 결과를 남긴다")
  @Test
  void runPrepareSeedScript() throws Exception {
    Path outputDir = Files.createDirectory(tempDir.resolve("output"));
    Path resetSql = tempDir.resolve("reset.sql");
    Path applySql = tempDir.resolve("apply.sql");
    Path h2Path = tempDir.resolve("prepare-seed-db");

    Files.writeString(
        resetSql,
        """
        DROP TABLE IF EXISTS SCRIPT_CHECK;
        CREATE TABLE SCRIPT_CHECK (
          id INT PRIMARY KEY,
          script_value VARCHAR(32)
        );
        """,
        StandardCharsets.UTF_8);
    Files.writeString(
        applySql,
        """
        INSERT INTO SCRIPT_CHECK (id, script_value) VALUES (1, 'applied');
        """,
        StandardCharsets.UTF_8);

    int exitCode =
        runScript(
            outputDir,
            resetSql,
            applySql,
            "jdbc:h2:file:" + h2Path + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");

    assertThat(exitCode).isZero();
    assertThat(Files.exists(outputDir.resolve("prepare.log"))).isTrue();
    assertThat(Files.readString(outputDir.resolve("prepare.log"))).contains("준비 데이터 적용 완료");

    try (Connection connection =
            DriverManager.getConnection(
                "jdbc:h2:file:" + h2Path + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
                "sa",
                "")) {
      try (PreparedStatement statement =
              connection.prepareStatement("SELECT script_value FROM SCRIPT_CHECK WHERE id = 1")) {
        try (ResultSet resultSet = statement.executeQuery()) {
          assertThat(resultSet.next()).isTrue();
          assertThat(resultSet.getString(1)).isEqualTo("applied");
        }
      }
    }
  }

  @DisplayName("포트가 이미 사용 중이면 준비 스크립트가 포트 충돌 종료 코드 10을 남긴다")
  @Test
  void stopPrepareSeedScriptWhenPortIsOccupied() throws Exception {
    Path outputDir = Files.createDirectory(tempDir.resolve("output-port"));
    Path resetSql = tempDir.resolve("reset-port.sql");
    Path applySql = tempDir.resolve("apply-port.sql");
    Path h2Path = tempDir.resolve("prepare-seed-port-db");

    Files.writeString(
        resetSql,
        """
        DROP TABLE IF EXISTS SCRIPT_CHECK;
        CREATE TABLE SCRIPT_CHECK (
          id INT PRIMARY KEY,
          script_value VARCHAR(32)
        );
        """,
        StandardCharsets.UTF_8);
    Files.writeString(
        applySql,
        """
        INSERT INTO SCRIPT_CHECK (id, script_value) VALUES (1, 'applied');
        """,
        StandardCharsets.UTF_8);

    try (ServerSocket occupiedSocket = new ServerSocket(0)) {
      int exitCode =
          runScript(
              outputDir,
              resetSql,
              applySql,
              "jdbc:h2:file:" + h2Path + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE",
              occupiedSocket.getLocalPort());

      assertThat(exitCode).isEqualTo(10);
      assertThat(Files.exists(outputDir.resolve("prepare.log"))).isTrue();
      assertThat(Files.readString(outputDir.resolve("prepare.log"))).contains("시작 전 포트 충돌");
    }
  }

  @DisplayName("H2 명령줄 도구가 이미 실행 중이면 준비 스크립트가 중복 실행 종료 코드 11을 남긴다")
  @Test
  void stopPrepareSeedScriptWhenH2CliIsAlreadyRunning() throws Exception {
    Path outputDir = Files.createDirectory(tempDir.resolve("output-h2-cli"));
    Path resetSql = tempDir.resolve("reset-h2-cli.sql");
    Path applySql = tempDir.resolve("apply-h2-cli.sql");
    Path h2Path = tempDir.resolve("prepare-seed-h2-cli-db");

    Files.writeString(
        resetSql,
        """
        DROP TABLE IF EXISTS SCRIPT_CHECK;
        CREATE TABLE SCRIPT_CHECK (
          id INT PRIMARY KEY,
          script_value VARCHAR(32)
        );
        """,
        StandardCharsets.UTF_8);
    Files.writeString(
        applySql,
        """
        INSERT INTO SCRIPT_CHECK (id, script_value) VALUES (1, 'applied');
        """,
        StandardCharsets.UTF_8);

    Process h2ShellProcess =
        new ProcessBuilder("bash", "-lc", "exec -a org.h2.tools.Shell sleep 20")
            .redirectErrorStream(true)
            .redirectOutput(outputDir.resolve("h2-cli.log").toFile())
            .start();

    try {
      TimeUnit.MILLISECONDS.sleep(200);

      int exitCode =
          runScript(
              outputDir,
              resetSql,
              applySql,
              "jdbc:h2:file:" + h2Path + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");

      assertThat(exitCode).isEqualTo(11);
      assertThat(Files.exists(outputDir.resolve("prepare.log"))).isTrue();
      assertThat(Files.readString(outputDir.resolve("prepare.log"))).contains("H2 명령줄 도구 중복 실행");
    } finally {
      h2ShellProcess.destroyForcibly();
      h2ShellProcess.waitFor(5, TimeUnit.SECONDS);
    }
  }

  @DisplayName("정리 SQL이 실패하면 준비 스크립트가 정리 SQL 실패 종료 코드 12를 남긴다")
  @Test
  void stopPrepareSeedScriptWhenResetSqlFails() throws Exception {
    Path outputDir = Files.createDirectory(tempDir.resolve("output-reset-fail"));
    Path resetSql = tempDir.resolve("reset-invalid.sql");
    Path applySql = tempDir.resolve("apply-after-reset-fail.sql");
    Path h2Path = tempDir.resolve("prepare-seed-reset-fail-db");

    Files.writeString(
        resetSql,
        """
        INSERT INTO UNKNOWN_TABLE (id, script_value) VALUES (1, 'broken');
        """,
        StandardCharsets.UTF_8);
    Files.writeString(
        applySql,
        """
        INSERT INTO SCRIPT_CHECK (id, script_value) VALUES (1, 'applied');
        """,
        StandardCharsets.UTF_8);

    int exitCode =
        runScript(
            outputDir,
            resetSql,
            applySql,
            "jdbc:h2:file:" + h2Path + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");

    assertThat(exitCode).isEqualTo(12);
    assertThat(Files.exists(outputDir.resolve("prepare.log"))).isTrue();
    assertThat(Files.readString(outputDir.resolve("prepare.log"))).contains("정리 SQL 실행 실패");
  }

  @DisplayName("적용 SQL이 실패하면 준비 스크립트가 준비 데이터 적용 SQL 실패 종료 코드 13을 남긴다")
  @Test
  void stopPrepareSeedScriptWhenApplySqlFails() throws Exception {
    Path outputDir = Files.createDirectory(tempDir.resolve("output-fail"));
    Path resetSql = tempDir.resolve("reset-fail.sql");
    Path applySql = tempDir.resolve("apply-fail.sql");
    Path h2Path = tempDir.resolve("prepare-seed-fail-db");

    Files.writeString(
        resetSql,
        """
        DROP TABLE IF EXISTS SCRIPT_CHECK;
        CREATE TABLE SCRIPT_CHECK (
          id INT PRIMARY KEY,
          script_value VARCHAR(32)
        );
        """,
        StandardCharsets.UTF_8);
    Files.writeString(
        applySql,
        """
        INSERT INTO UNKNOWN_TABLE (id, script_value) VALUES (1, 'broken');
        """,
        StandardCharsets.UTF_8);

    int exitCode =
        runScript(
            outputDir,
            resetSql,
            applySql,
            "jdbc:h2:file:" + h2Path + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE");

    assertThat(exitCode).isEqualTo(13);
    assertThat(Files.exists(outputDir.resolve("prepare.log"))).isTrue();
    assertThat(Files.readString(outputDir.resolve("prepare.log"))).contains("준비 데이터 적용 SQL 실행 실패");
  }

  private int runScript(Path outputDir, Path resetSql, Path applySql, String jdbcUrl) throws Exception {
    return runScript(outputDir, resetSql, applySql, jdbcUrl, findFreePort());
  }

  private int runScript(Path outputDir, Path resetSql, Path applySql, String jdbcUrl, int appPort)
      throws Exception {
    ProcessBuilder builder =
        new ProcessBuilder("bash", "scripts/manual-rerun-response/prepare-seed.sh")
            .directory(Path.of("/home/seaung13/workspace/agile-runner").toFile())
            .redirectErrorStream(true)
            .redirectOutput(outputDir.resolve("process.log").toFile());

    builder.environment().put("APP_PORT", Integer.toString(appPort));
    builder.environment().put("OUTPUT_DIR", outputDir.toString());
    builder.environment().put("SEED_RESET_SQL", resetSql.toString());
    builder.environment().put("SEED_APPLY_SQL", applySql.toString());
    builder.environment().put("JDBC_URL", jdbcUrl);
    builder.environment().put("H2_JAR", resolveH2JarPath().toString());

    Process process = builder.start();
    boolean finished = process.waitFor(20, TimeUnit.SECONDS);
    assertThat(finished).isTrue();
    return process.exitValue();
  }

  private int findFreePort() throws IOException {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    }
  }

  private Path resolveH2JarPath() throws Exception {
    Path h2CacheRoot =
        Path.of(
            System.getProperty("user.home"),
            ".gradle",
            "caches",
            "modules-2",
            "files-2.1",
            "com.h2database",
            "h2");

    try (var stream = Files.walk(h2CacheRoot)) {
      return stream
          .filter(Files::isRegularFile)
          .filter(file -> file.getFileName().toString().matches("h2-.*\\.jar"))
          .sorted()
          .reduce((first, second) -> second)
          .orElseThrow(() -> new IllegalStateException("h2 jar를 찾지 못했습니다."));
    }
  }
}
