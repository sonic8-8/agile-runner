package com.agilerunner.client.agentruntime;

import static org.assertj.core.api.Assertions.assertThat;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ManualRerunRunFlowScriptTest {

  private static final Path REPOSITORY_PATH = Path.of("/home/seaung13/workspace/agile-runner");
  private static final String TEST_APP_PROCESS_NAME = "agile-runner-test-app-process";
  private static final String TEST_H2_PROCESS_NAME = "agile-runner-test-h2-process";

  @TempDir
  Path tempDir;

  @DisplayName("재실행 초안 파일이 응답 파일 4개를 남기고 종료한다")
  @Test
  void runRerunScript() throws Exception {
    String rerunExecutionKey = "EXECUTION:MANUAL_RERUN:rerun-success";
    int port = findFreePort();
    Path outputDir = Files.createDirectory(tempDir.resolve("rerun-success-output"));

    Map<String, Deque<ResponsePlan>> routes = new HashMap<>();
    addRoute(
        routes,
        "GET /reviews/rerun/" + rerunExecutionKey,
        jsonResponse("{" + "\"executionKey\":\"" + rerunExecutionKey + "\",\"executionStatus\":\"FAILED\"}"),
        jsonResponse("{" + "\"executionKey\":\"" + rerunExecutionKey + "\",\"executionStatus\":\"FAILED\",\"availableActions\":[]}"));
    addRoute(
        routes,
        "GET /reviews/rerun/" + rerunExecutionKey + "/actions/history",
        jsonResponse("{" + "\"executionKey\":\"" + rerunExecutionKey + "\",\"actions\":[]}"));
    addRoute(
        routes,
        "POST /reviews/rerun/" + rerunExecutionKey + "/actions",
        jsonResponse("{" + "\"executionKey\":\"" + rerunExecutionKey + "\",\"action\":\"UNACKNOWLEDGE\"}"));

    try (StartedServer ignored = startServer(port, routes)) {
      int exitCode = runRerunScript(outputDir, port, rerunExecutionKey, "{\"action\":\"UNACKNOWLEDGE\",\"note\":\"테스트\"}");

      assertThat(exitCode).isZero();
      assertThat(Files.readString(outputDir.resolve("rerun-query-before.json"))).contains(rerunExecutionKey);
      assertThat(Files.readString(outputDir.resolve("rerun-history.json"))).contains("actions");
      assertThat(Files.readString(outputDir.resolve("rerun-action.json"))).contains("UNACKNOWLEDGE");
      assertThat(Files.readString(outputDir.resolve("rerun-query-after.json"))).contains("availableActions");
    } finally {
      destroyAppFromPidFile(outputDir);
    }
  }

  @DisplayName("재실행 초안 파일이 앱 포트 확인 실패 종료 코드 20을 남긴다")
  @Test
  void stopRunRerunScriptWhenAppDoesNotStart() throws Exception {
    int port = findFreePort();
    Path outputDir = Files.createDirectory(tempDir.resolve("rerun-app-start-fail"));

    int exitCode = runRerunScript(outputDir, port, "EXECUTION:MANUAL_RERUN:rerun-app-start-fail", "{\"action\":\"UNACKNOWLEDGE\"}", "exit 1");

    assertThat(exitCode).isEqualTo(20);
    assertThat(Files.readString(outputDir.resolve("run-rerun.log"))).contains("앱 기동 시간 안에 포트 확인 실패");
    assertThat(Files.exists(outputDir.resolve("rerun-query-before.json"))).isFalse();
  }

  @DisplayName("재실행 초안 파일이 단건 조회 실패 종료 코드 21을 남긴다")
  @Test
  void stopRunRerunScriptWhenQueryBeforeFails() throws Exception {
    String rerunExecutionKey = "EXECUTION:MANUAL_RERUN:rerun-query-before-fail";
    int port = findFreePort();
    Path outputDir = Files.createDirectory(tempDir.resolve("rerun-query-before-fail"));

    Map<String, Deque<ResponsePlan>> routes = new HashMap<>();
    addRoute(routes, "GET /reviews/rerun/" + rerunExecutionKey, errorResponse());

    try (StartedServer ignored = startServer(port, routes)) {
      int exitCode = runRerunScript(outputDir, port, rerunExecutionKey, "{\"action\":\"UNACKNOWLEDGE\"}");

      assertThat(exitCode).isEqualTo(21);
      assertThat(Files.readString(outputDir.resolve("run-rerun.log"))).contains("재실행 단건 조회 실패");
      assertThat(Files.exists(outputDir.resolve("rerun-history.json"))).isFalse();
    } finally {
      destroyAppFromPidFile(outputDir);
    }
  }

  @DisplayName("재실행 초안 파일이 이력 조회 실패 종료 코드 22를 남긴다")
  @Test
  void stopRunRerunScriptWhenHistoryFails() throws Exception {
    String rerunExecutionKey = "EXECUTION:MANUAL_RERUN:rerun-history-fail";
    int port = findFreePort();
    Path outputDir = Files.createDirectory(tempDir.resolve("rerun-history-fail"));

    Map<String, Deque<ResponsePlan>> routes = new HashMap<>();
    addRoute(routes, "GET /reviews/rerun/" + rerunExecutionKey, jsonResponse("{\"executionKey\":\"" + rerunExecutionKey + "\"}"));
    addRoute(routes, "GET /reviews/rerun/" + rerunExecutionKey + "/actions/history", errorResponse());

    try (StartedServer ignored = startServer(port, routes)) {
      int exitCode = runRerunScript(outputDir, port, rerunExecutionKey, "{\"action\":\"UNACKNOWLEDGE\"}");

      assertThat(exitCode).isEqualTo(22);
      assertThat(Files.readString(outputDir.resolve("run-rerun.log"))).contains("재실행 이력 조회 실패");
      assertThat(Files.exists(outputDir.resolve("rerun-action.json"))).isFalse();
    } finally {
      destroyAppFromPidFile(outputDir);
    }
  }

  @DisplayName("재실행 초안 파일이 관리자 조치 실패 종료 코드 23을 남긴다")
  @Test
  void stopRunRerunScriptWhenActionFails() throws Exception {
    String rerunExecutionKey = "EXECUTION:MANUAL_RERUN:rerun-action-fail";
    int port = findFreePort();
    Path outputDir = Files.createDirectory(tempDir.resolve("rerun-action-fail"));

    Map<String, Deque<ResponsePlan>> routes = new HashMap<>();
    addRoute(routes, "GET /reviews/rerun/" + rerunExecutionKey, jsonResponse("{\"executionKey\":\"" + rerunExecutionKey + "\"}"));
    addRoute(routes, "GET /reviews/rerun/" + rerunExecutionKey + "/actions/history", jsonResponse("{\"actions\":[]}"));
    addRoute(routes, "POST /reviews/rerun/" + rerunExecutionKey + "/actions", errorResponse());

    try (StartedServer ignored = startServer(port, routes)) {
      int exitCode = runRerunScript(outputDir, port, rerunExecutionKey, "{\"action\":\"UNACKNOWLEDGE\"}");

      assertThat(exitCode).isEqualTo(23);
      assertThat(Files.readString(outputDir.resolve("run-rerun.log"))).contains("재실행 관리자 조치 실패");
      assertThat(Files.exists(outputDir.resolve("rerun-query-after.json"))).isFalse();
    } finally {
      destroyAppFromPidFile(outputDir);
    }
  }

  @DisplayName("재실행 초안 파일이 조치 후 단건 조회 실패 종료 코드 24를 남긴다")
  @Test
  void stopRunRerunScriptWhenQueryAfterFails() throws Exception {
    String rerunExecutionKey = "EXECUTION:MANUAL_RERUN:rerun-query-after-fail";
    int port = findFreePort();
    Path outputDir = Files.createDirectory(tempDir.resolve("rerun-query-after-fail"));

    Map<String, Deque<ResponsePlan>> routes = new HashMap<>();
    addRoute(
        routes,
        "GET /reviews/rerun/" + rerunExecutionKey,
        jsonResponse("{\"executionKey\":\"" + rerunExecutionKey + "\"}"),
        errorResponse());
    addRoute(routes, "GET /reviews/rerun/" + rerunExecutionKey + "/actions/history", jsonResponse("{\"actions\":[]}"));
    addRoute(routes, "POST /reviews/rerun/" + rerunExecutionKey + "/actions", jsonResponse("{\"action\":\"UNACKNOWLEDGE\"}"));

    try (StartedServer ignored = startServer(port, routes)) {
      int exitCode = runRerunScript(outputDir, port, rerunExecutionKey, "{\"action\":\"UNACKNOWLEDGE\"}");

      assertThat(exitCode).isEqualTo(24);
      assertThat(Files.readString(outputDir.resolve("run-rerun.log"))).contains("재실행 조치 후 단건 조회 실패");
    } finally {
      destroyAppFromPidFile(outputDir);
    }
  }

  @DisplayName("재시도 초안 파일이 응답 파일 3개를 남기고 종료한다")
  @Test
  void runRetryScript() throws Exception {
    String sourceExecutionKey = "EXECUTION:MANUAL_RERUN:retry-source";
    String derivedExecutionKey = "EXECUTION:MANUAL_RERUN:retry-derived";
    int port = findFreePort();
    Path outputDir = Files.createDirectory(tempDir.resolve("retry-success-output"));

    Map<String, Deque<ResponsePlan>> routes = new HashMap<>();
    addRoute(
        routes,
        "POST /reviews/rerun/" + sourceExecutionKey + "/retry",
        jsonResponse("{\"executionKey\":\"" + derivedExecutionKey + "\"}"));
    addRoute(
        routes,
        "GET /reviews/rerun/" + derivedExecutionKey,
        jsonResponse("{\"executionKey\":\"" + derivedExecutionKey + "\",\"retrySourceExecutionKey\":\"" + sourceExecutionKey + "\"}"));

    try (StartedServer ignored = startServer(port, routes)) {
      int exitCode = runRetryScript(outputDir, port, sourceExecutionKey, "{\"executionControlMode\":\"DRY_RUN\"}");

      assertThat(exitCode).isZero();
      assertThat(Files.readString(outputDir.resolve("retry-response.json"))).contains(derivedExecutionKey);
      assertThat(Files.readString(outputDir.resolve("retry-derived-execution-key.txt"))).contains(derivedExecutionKey);
      assertThat(Files.readString(outputDir.resolve("retry-derived-query.json"))).contains(sourceExecutionKey);
    } finally {
      destroyAppFromPidFile(outputDir);
    }
  }

  @DisplayName("재시도 초안 파일이 앱 포트 확인 실패 종료 코드 30을 남긴다")
  @Test
  void stopRunRetryScriptWhenAppDoesNotStart() throws Exception {
    int port = findFreePort();
    Path outputDir = Files.createDirectory(tempDir.resolve("retry-app-start-fail"));

    int exitCode = runRetryScript(outputDir, port, "EXECUTION:MANUAL_RERUN:retry-source-app-start-fail", "{\"executionControlMode\":\"DRY_RUN\"}", "exit 1");

    assertThat(exitCode).isEqualTo(30);
    assertThat(Files.readString(outputDir.resolve("run-retry.log"))).contains("앱 기동 시간 안에 포트 확인 실패");
    assertThat(Files.exists(outputDir.resolve("retry-response.json"))).isFalse();
  }

  @DisplayName("재시도 초안 파일이 재시도 요청 실패 종료 코드 31을 남긴다")
  @Test
  void stopRunRetryScriptWhenRetryRequestFails() throws Exception {
    String sourceExecutionKey = "EXECUTION:MANUAL_RERUN:retry-request-fail";
    int port = findFreePort();
    Path outputDir = Files.createDirectory(tempDir.resolve("retry-request-fail"));

    Map<String, Deque<ResponsePlan>> routes = new HashMap<>();
    addRoute(routes, "POST /reviews/rerun/" + sourceExecutionKey + "/retry", errorResponse());

    try (StartedServer ignored = startServer(port, routes)) {
      int exitCode = runRetryScript(outputDir, port, sourceExecutionKey, "{\"executionControlMode\":\"DRY_RUN\"}");

      assertThat(exitCode).isEqualTo(31);
      assertThat(Files.readString(outputDir.resolve("run-retry.log"))).contains("재시도 요청 실패");
      assertThat(Files.exists(outputDir.resolve("retry-derived-execution-key.txt"))).isFalse();
    } finally {
      destroyAppFromPidFile(outputDir);
    }
  }

  @DisplayName("재시도 초안 파일이 파생 실행 키 추출 실패 종료 코드 32를 남긴다")
  @Test
  void stopRunRetryScriptWhenDerivedExecutionKeyIsMissing() throws Exception {
    String sourceExecutionKey = "EXECUTION:MANUAL_RERUN:retry-missing-key";
    int port = findFreePort();
    Path outputDir = Files.createDirectory(tempDir.resolve("retry-missing-key"));

    Map<String, Deque<ResponsePlan>> routes = new HashMap<>();
    addRoute(routes, "POST /reviews/rerun/" + sourceExecutionKey + "/retry", jsonResponse("{\"message\":\"missing key\"}"));

    try (StartedServer ignored = startServer(port, routes)) {
      int exitCode = runRetryScript(outputDir, port, sourceExecutionKey, "{\"executionControlMode\":\"DRY_RUN\"}");

      assertThat(exitCode).isEqualTo(32);
      assertThat(Files.readString(outputDir.resolve("run-retry.log"))).contains("재시도 파생 실행 키 추출 실패");
      assertThat(Files.exists(outputDir.resolve("retry-derived-query.json"))).isFalse();
    } finally {
      destroyAppFromPidFile(outputDir);
    }
  }

  @DisplayName("재시도 초안 파일이 파생 실행 단건 조회 실패 종료 코드 33을 남긴다")
  @Test
  void stopRunRetryScriptWhenDerivedQueryFails() throws Exception {
    String sourceExecutionKey = "EXECUTION:MANUAL_RERUN:retry-derived-query-fail";
    String derivedExecutionKey = "EXECUTION:MANUAL_RERUN:retry-derived-query-fail-derived";
    int port = findFreePort();
    Path outputDir = Files.createDirectory(tempDir.resolve("retry-derived-query-fail"));

    Map<String, Deque<ResponsePlan>> routes = new HashMap<>();
    addRoute(
        routes,
        "POST /reviews/rerun/" + sourceExecutionKey + "/retry",
        jsonResponse("{\"executionKey\":\"" + derivedExecutionKey + "\"}"));
    addRoute(routes, "GET /reviews/rerun/" + derivedExecutionKey, errorResponse());

    try (StartedServer ignored = startServer(port, routes)) {
      int exitCode = runRetryScript(outputDir, port, sourceExecutionKey, "{\"executionControlMode\":\"DRY_RUN\"}");

      assertThat(exitCode).isEqualTo(33);
      assertThat(Files.readString(outputDir.resolve("run-retry.log"))).contains("재시도 파생 실행 단건 조회 실패");
    } finally {
      destroyAppFromPidFile(outputDir);
    }
  }

  @DisplayName("실행 근거 수집 초안 파일이 재실행 실행 근거 파일을 남긴다")
  @Test
  void collectRerunEvidence() throws Exception {
    Path outputDir = Files.createDirectory(tempDir.resolve("collect-rerun-output"));
    Path h2Path = tempDir.resolve("collect-rerun-db");
    String rerunExecutionKey = "EXECUTION:MANUAL_RERUN:collect-rerun";

    createRerunEvidence(h2Path, rerunExecutionKey);
    Process appProcess = startNamedSleepProcess(TEST_APP_PROCESS_NAME, outputDir.resolve("app.log"));
    Files.writeString(outputDir.resolve("app.pid"), Long.toString(appProcess.pid()), StandardCharsets.UTF_8);

    int exitCode = runCollectEvidenceScript(outputDir, "rerun", "jdbc:h2:file:" + h2Path + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE", rerunExecutionKey, null);

    String collectLog = readFileIfExists(outputDir.resolve("collect-evidence.log"));

    assertThat(exitCode)
        .withFailMessage(collectLog)
        .isZero();
    assertThat(Files.readString(outputDir.resolve("rerun-webhook-execution.txt"))).contains(rerunExecutionKey);
    assertThat(Files.readString(outputDir.resolve("rerun-action-audit.txt"))).contains("ACKNOWLEDGE");
  }

  @DisplayName("실행 근거 수집 초안 파일이 재시도 실행 근거 파일을 남긴다")
  @Test
  void collectRetryEvidence() throws Exception {
    Path outputDir = Files.createDirectory(tempDir.resolve("collect-retry-output"));
    Path h2Path = tempDir.resolve("collect-retry-db");
    String derivedExecutionKey = "EXECUTION:MANUAL_RERUN:collect-retry-derived";
    String sourceExecutionKey = "EXECUTION:MANUAL_RERUN:collect-retry-source";

    createRetryEvidence(h2Path, sourceExecutionKey, derivedExecutionKey);
    Process appProcess = startNamedSleepProcess(TEST_APP_PROCESS_NAME, outputDir.resolve("app.log"));
    Files.writeString(outputDir.resolve("app.pid"), Long.toString(appProcess.pid()), StandardCharsets.UTF_8);

    int exitCode = runCollectEvidenceScript(outputDir, "retry", "jdbc:h2:file:" + h2Path + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE", null, derivedExecutionKey);

    String collectLog = readFileIfExists(outputDir.resolve("collect-evidence.log"));

    assertThat(exitCode)
        .withFailMessage(collectLog)
        .isZero();
    assertThat(Files.readString(outputDir.resolve("retry-webhook-execution.txt"))).contains(derivedExecutionKey);
    assertThat(Files.readString(outputDir.resolve("retry-agent-execution-log.txt"))).contains("review-generated");
  }

  @DisplayName("실행 근거 수집 초안 파일이 앱 종료 미확인 종료 코드 40을 남긴다")
  @Test
  void stopCollectEvidenceWhenAppIsStillRunning() throws Exception {
    Path outputDir = Files.createDirectory(tempDir.resolve("collect-app-running"));
    Path h2Path = tempDir.resolve("collect-app-running-db");
    String rerunExecutionKey = "EXECUTION:MANUAL_RERUN:collect-app-running";

    createRerunEvidence(h2Path, rerunExecutionKey);
    Process appProcess = startNamedSleepProcess(TEST_APP_PROCESS_NAME, outputDir.resolve("app.log"));

    try {
      int exitCode = runCollectEvidenceScript(outputDir, "rerun", "jdbc:h2:file:" + h2Path + ";MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE", rerunExecutionKey, null);

      assertThat(exitCode).isEqualTo(40);
      assertThat(Files.readString(outputDir.resolve("collect-evidence.log"))).contains("앱 종료 미확인");
    } finally {
      appProcess.destroyForcibly();
      appProcess.waitFor(5, TimeUnit.SECONDS);
    }
  }

  @DisplayName("실행 근거 수집 초안 파일이 H2 조회 실패 종료 코드 41을 남긴다")
  @Test
  void stopCollectEvidenceWhenH2QueryFails() throws Exception {
    Path outputDir = Files.createDirectory(tempDir.resolve("collect-query-fail"));
    String rerunExecutionKey = "EXECUTION:MANUAL_RERUN:collect-query-fail";

    int exitCode = runCollectEvidenceScript(outputDir, "rerun", "jdbc:h2:file:" + tempDir.resolve("missing-db") + ";MODE=PostgreSQL;DB_CLOSE_ON_EXIT=FALSE", rerunExecutionKey, null);

    assertThat(exitCode).isEqualTo(41);
    assertThat(Files.readString(outputDir.resolve("collect-evidence.log"))).contains("실행 근거 조회 실패");
  }

  @DisplayName("실행 근거 수집 초안 파일이 다른 H2 프로세스가 있어도 잠금 시그니처가 없으면 종료 코드 41을 남긴다")
  @Test
  void stopCollectEvidenceWhenOtherH2ProcessExistsWithoutLockSignature() throws Exception {
    Path outputDir = Files.createDirectory(tempDir.resolve("collect-non-lock-with-h2-process"));
    String rerunExecutionKey = "EXECUTION:MANUAL_RERUN:collect-non-lock-with-h2-process";
    Process h2ShellProcess = startNamedSleepProcess(TEST_H2_PROCESS_NAME, outputDir.resolve("h2-shell.log"));

    try {
      int exitCode = runCollectEvidenceScript(outputDir, "rerun", "jdbc:h2:file:" + tempDir.resolve("missing-non-lock-db") + ";MODE=PostgreSQL;DB_CLOSE_ON_EXIT=FALSE", rerunExecutionKey, null);

      assertThat(exitCode).isEqualTo(41);
      assertThat(Files.readString(outputDir.resolve("collect-evidence.log"))).contains("실행 근거 조회 실패");
      assertThat(Files.readString(outputDir.resolve("collect-evidence.log"))).doesNotContain("H2 잠금 의심");
    } finally {
      h2ShellProcess.destroyForcibly();
      h2ShellProcess.waitFor(5, TimeUnit.SECONDS);
    }
  }

  @DisplayName("실행 근거 수집 초안 파일이 H2 잠금 의심 종료 코드 42를 남긴다")
  @Test
  void stopCollectEvidenceWhenH2CliSeemsLocked() throws Exception {
    Path outputDir = Files.createDirectory(tempDir.resolve("collect-lock"));
    String rerunExecutionKey = "EXECUTION:MANUAL_RERUN:collect-lock";
    Path h2Path = tempDir.resolve("collect-lock-db");
    createRerunEvidence(h2Path, rerunExecutionKey);
    Process h2ShellProcess = startH2LockHolderProcess(h2Path, outputDir.resolve("h2-shell.log"));

    try {
      TimeUnit.SECONDS.sleep(1);
      int exitCode = runCollectEvidenceScript(outputDir, "rerun", "jdbc:h2:file:" + h2Path + ";MODE=PostgreSQL;DB_CLOSE_ON_EXIT=FALSE", rerunExecutionKey, null);

      assertThat(exitCode).isEqualTo(42);
      assertThat(Files.readString(outputDir.resolve("collect-evidence.log"))).contains("H2 잠금 의심");
    } finally {
      h2ShellProcess.destroyForcibly();
      h2ShellProcess.waitFor(5, TimeUnit.SECONDS);
    }
  }

  private int runRerunScript(Path outputDir, int port, String rerunExecutionKey, String actionBody)
      throws Exception {
    return runRerunScript(outputDir, port, rerunExecutionKey, actionBody, "exec -a GradleMain sleep 20");
  }

  private int runRerunScript(
      Path outputDir, int port, String rerunExecutionKey, String actionBody, String appStartCommand)
      throws Exception {
    Map<String, String> environment = new HashMap<>();
    environment.put("OUTPUT_DIR", outputDir.toString());
    environment.put("APP_PORT", Integer.toString(port));
    environment.put("BASE_URL", "http://127.0.0.1:" + port);
    environment.put("RERUN_EXECUTION_KEY", rerunExecutionKey);
    environment.put("RERUN_ACTION_BODY", actionBody);
    environment.put("APP_START_CMD", appStartCommand);
    environment.put("APP_START_TIMEOUT_SECONDS", "2");
    return runScript("run-rerun.sh", outputDir, environment);
  }

  private int runRetryScript(Path outputDir, int port, String sourceExecutionKey, String requestBody)
      throws Exception {
    return runRetryScript(outputDir, port, sourceExecutionKey, requestBody, "exec -a GradleMain sleep 20");
  }

  private int runRetryScript(
      Path outputDir, int port, String sourceExecutionKey, String requestBody, String appStartCommand)
      throws Exception {
    Map<String, String> environment = new HashMap<>();
    environment.put("OUTPUT_DIR", outputDir.toString());
    environment.put("APP_PORT", Integer.toString(port));
    environment.put("BASE_URL", "http://127.0.0.1:" + port);
    environment.put("RETRY_SOURCE_EXECUTION_KEY", sourceExecutionKey);
    environment.put("RETRY_REQUEST_BODY", requestBody);
    environment.put("APP_START_CMD", appStartCommand);
    environment.put("APP_START_TIMEOUT_SECONDS", "2");
    return runScript("run-retry.sh", outputDir, environment);
  }

  private int runCollectEvidenceScript(
      Path outputDir,
      String evidenceMode,
      String jdbcUrl,
      String rerunExecutionKey,
      String retryDerivedExecutionKey)
      throws Exception {
    Map<String, String> environment = new HashMap<>();
    environment.put("OUTPUT_DIR", outputDir.toString());
    environment.put("JDBC_URL", jdbcUrl);
    environment.put("H2_JAR", resolveH2JarPath().toString());
    environment.put("EVIDENCE_MODE", evidenceMode);
    environment.put("APP_PROCESS_PATTERN", TEST_APP_PROCESS_NAME);
    environment.put("H2_PROCESS_PATTERN", TEST_H2_PROCESS_NAME);
    if (rerunExecutionKey != null) {
      environment.put("RERUN_EXECUTION_KEY", rerunExecutionKey);
    }
    if (retryDerivedExecutionKey != null) {
      environment.put("RETRY_DERIVED_EXECUTION_KEY", retryDerivedExecutionKey);
    }
    return runScript("collect-evidence.sh", outputDir, environment);
  }

  private int runScript(String scriptName, Path outputDir, Map<String, String> environment) throws Exception {
    ProcessBuilder builder =
        new ProcessBuilder("bash", "scripts/manual-rerun-response/" + scriptName)
            .directory(REPOSITORY_PATH.toFile())
            .redirectErrorStream(true)
            .redirectOutput(outputDir.resolve(scriptName + ".process.log").toFile());
    builder.environment().putAll(environment);

    Process process = builder.start();
    boolean finished = process.waitFor(30, TimeUnit.SECONDS);
    assertThat(finished).isTrue();
    return process.exitValue();
  }

  private void destroyAppFromPidFile(Path outputDir) throws Exception {
    Path pidFile = outputDir.resolve("app.pid");
    if (!Files.exists(pidFile)) {
      return;
    }
    String pidValue = Files.readString(pidFile).trim();
    if (pidValue.isEmpty()) {
      return;
    }
    ProcessHandle.of(Long.parseLong(pidValue)).ifPresent(processHandle -> {
      processHandle.destroyForcibly();
      try {
        processHandle.onExit().get(5, TimeUnit.SECONDS);
      } catch (Exception ignored) {
      }
    });
  }

  private StartedServer startServer(int port, Map<String, Deque<ResponsePlan>> routes) throws Exception {
    HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", port), 0);
    server.createContext("/", exchange -> handleExchange(exchange, routes));
    server.start();
    return new StartedServer(server);
  }

  private void handleExchange(HttpExchange exchange, Map<String, Deque<ResponsePlan>> routes)
      throws IOException {
    String routeKey = exchange.getRequestMethod() + " " + exchange.getRequestURI().getPath();
    Deque<ResponsePlan> plans = routes.get(routeKey);
    ResponsePlan responsePlan = resolveResponsePlan(plans);
    byte[] body = responsePlan.body().getBytes(StandardCharsets.UTF_8);

    exchange.getResponseHeaders().add("Content-Type", "application/json");
    exchange.sendResponseHeaders(responsePlan.status(), body.length);
    try (var responseBody = exchange.getResponseBody()) {
      responseBody.write(body);
    }
  }

  private ResponsePlan resolveResponsePlan(Deque<ResponsePlan> plans) {
    if (plans == null || plans.isEmpty()) {
      return new ResponsePlan(404, "{\"error\":\"not found\"}");
    }
    if (plans.size() == 1) {
      return plans.peekFirst();
    }
    return plans.pollFirst();
  }

  private void addRoute(Map<String, Deque<ResponsePlan>> routes, String key, ResponsePlan... responsePlans) {
    Deque<ResponsePlan> plans = new ArrayDeque<>();
    for (ResponsePlan responsePlan : responsePlans) {
      plans.addLast(responsePlan);
    }
    routes.put(key, plans);
  }

  private ResponsePlan jsonResponse(String body) {
    return new ResponsePlan(200, body);
  }

  private ResponsePlan errorResponse() {
    return new ResponsePlan(500, "{\"error\":\"failed\"}");
  }

  private Process startH2LockHolderProcess(Path h2Path, Path logFile) throws Exception {
    Path sourceFile = tempDir.resolve("H2LockHolder.java");
    Files.writeString(
        sourceFile,
        "import java.sql.Connection;\n"
            + "import java.sql.DriverManager;\n"
            + "public class H2LockHolder {\n"
            + "  public static void main(String[] args) throws Exception {\n"
            + "    try (Connection connection = DriverManager.getConnection(args[0], \"sa\", \"\")) {\n"
            + "      Thread.sleep(20000L);\n"
            + "    }\n"
            + "  }\n"
            + "}\n",
        StandardCharsets.UTF_8);

    String jdbcUrl = "jdbc:h2:file:" + h2Path + ";MODE=PostgreSQL;DB_CLOSE_ON_EXIT=FALSE";
    String command =
        "exec -a "
            + TEST_H2_PROCESS_NAME
            + " java --class-path "
            + resolveH2JarPath()
            + " "
            + sourceFile
            + " '"
            + jdbcUrl
            + "'";

    return new ProcessBuilder("bash", "-lc", command)
        .redirectErrorStream(true)
        .redirectOutput(logFile.toFile())
        .start();
  }

  private Process startNamedSleepProcess(String processName, Path logFile) throws IOException {
    return new ProcessBuilder("bash", "-lc", "exec -a " + processName + " sleep 20")
        .redirectErrorStream(true)
        .redirectOutput(logFile.toFile())
        .start();
  }

  private void createRerunEvidence(Path h2Path, String rerunExecutionKey) throws Exception {
    try (Connection connection = openConnection(h2Path); Statement statement = connection.createStatement()) {
      statement.execute("DROP TABLE IF EXISTS WEBHOOK_EXECUTION");
      statement.execute("DROP TABLE IF EXISTS MANUAL_RERUN_CONTROL_ACTION_AUDIT");
      statement.execute("CREATE TABLE WEBHOOK_EXECUTION (execution_key VARCHAR(255), status VARCHAR(32), error_code VARCHAR(128), failure_disposition VARCHAR(64), execution_start_type VARCHAR(64), execution_control_mode VARCHAR(32), write_performed BOOLEAN, retry_source_execution_key VARCHAR(255))");
      statement.execute("CREATE TABLE MANUAL_RERUN_CONTROL_ACTION_AUDIT (id INT PRIMARY KEY, execution_key VARCHAR(255), action VARCHAR(64), action_status VARCHAR(64), note VARCHAR(255), applied_at TIMESTAMP)");
      statement.execute("INSERT INTO WEBHOOK_EXECUTION (execution_key, status, error_code, failure_disposition, execution_start_type, execution_control_mode, write_performed, retry_source_execution_key) VALUES ('" + rerunExecutionKey + "', 'FAILED', 'GITHUB_APP_CONFIGURATION_MISSING', 'MANUAL_ACTION_REQUIRED', 'MANUAL_RERUN', 'DRY_RUN', FALSE, NULL)");
      statement.execute("INSERT INTO MANUAL_RERUN_CONTROL_ACTION_AUDIT (id, execution_key, action, action_status, note, applied_at) VALUES (1, '" + rerunExecutionKey + "', 'ACKNOWLEDGE', 'APPLIED', '운영자 확인 완료', TIMESTAMP '" + LocalDateTime.of(2026, 4, 13, 10, 0, 0) + "')");
    }
  }

  private void createRetryEvidence(Path h2Path, String sourceExecutionKey, String derivedExecutionKey)
      throws Exception {
    try (Connection connection = openConnection(h2Path); Statement statement = connection.createStatement()) {
      statement.execute("DROP TABLE IF EXISTS WEBHOOK_EXECUTION");
      statement.execute("DROP TABLE IF EXISTS AGENT_EXECUTION_LOG");
      statement.execute("CREATE TABLE WEBHOOK_EXECUTION (execution_key VARCHAR(255), retry_source_execution_key VARCHAR(255), status VARCHAR(32), error_code VARCHAR(128), failure_disposition VARCHAR(64), execution_start_type VARCHAR(64), execution_control_mode VARCHAR(32), write_performed BOOLEAN)");
      statement.execute("CREATE TABLE AGENT_EXECUTION_LOG (id INT PRIMARY KEY, execution_key VARCHAR(255), retry_source_execution_key VARCHAR(255), step_name VARCHAR(64), status VARCHAR(32), error_code VARCHAR(128), failure_disposition VARCHAR(64))");
      statement.execute("INSERT INTO WEBHOOK_EXECUTION (execution_key, retry_source_execution_key, status, error_code, failure_disposition, execution_start_type, execution_control_mode, write_performed) VALUES ('" + derivedExecutionKey + "', '" + sourceExecutionKey + "', 'FAILED', 'GITHUB_APP_CONFIGURATION_MISSING', 'MANUAL_ACTION_REQUIRED', 'MANUAL_RERUN', 'DRY_RUN', FALSE)");
      statement.execute("INSERT INTO AGENT_EXECUTION_LOG (id, execution_key, retry_source_execution_key, step_name, status, error_code, failure_disposition) VALUES (1, '" + derivedExecutionKey + "', '" + sourceExecutionKey + "', 'review-generated', 'FAILED', 'GITHUB_APP_CONFIGURATION_MISSING', 'MANUAL_ACTION_REQUIRED')");
    }
  }

  private Connection openConnection(Path h2Path) throws Exception {
    return DriverManager.getConnection(
        "jdbc:h2:file:" + h2Path + ";MODE=PostgreSQL;DB_CLOSE_ON_EXIT=FALSE",
        "sa",
        "");
  }

  private int findFreePort() throws IOException {
    try (ServerSocket socket = new ServerSocket(0)) {
      return socket.getLocalPort();
    }
  }

  private String readFileIfExists(Path path) {
    try {
      if (!Files.exists(path)) {
        return "";
      }
      return Files.readString(path);
    } catch (IOException exception) {
      return exception.getMessage();
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

  private record ResponsePlan(int status, String body) {}

  private record StartedServer(HttpServer server) implements AutoCloseable {
    @Override
    public void close() {
      server.stop(0);
    }
  }
}
