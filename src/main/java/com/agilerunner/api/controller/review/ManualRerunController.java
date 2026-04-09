package com.agilerunner.api.controller.review;

import com.agilerunner.api.controller.review.request.ManualRerunRequest;
import com.agilerunner.api.controller.review.request.ManualRerunExecutionListRequest;
import com.agilerunner.api.controller.review.request.ManualRerunControlActionRequest;
import com.agilerunner.api.controller.review.request.ManualRerunRetryRequest;
import com.agilerunner.api.controller.review.response.ManualRerunControlActionResponse;
import com.agilerunner.api.controller.review.response.ManualRerunExecutionListResponse;
import com.agilerunner.api.controller.review.response.ManualRerunQueryNotFoundResponse;
import com.agilerunner.api.controller.review.response.ManualRerunQueryResponse;
import com.agilerunner.api.controller.review.response.ManualRerunResponse;
import com.agilerunner.api.controller.review.response.ManualRerunRetryConflictResponse;
import com.agilerunner.api.controller.review.response.ManualRerunRetryNotFoundResponse;
import com.agilerunner.api.controller.review.response.ManualRerunRetryResponse;
import com.agilerunner.api.service.review.ManualRerunExecutionListService;
import com.agilerunner.api.service.review.ManualRerunControlActionService;
import com.agilerunner.api.service.review.ManualRerunQueryService;
import com.agilerunner.api.service.review.ManualRerunService;
import com.agilerunner.api.service.review.ManualRerunRetryService;
import com.agilerunner.api.service.review.response.ManualRerunControlActionServiceResponse;
import com.agilerunner.api.service.review.response.ManualRerunExecutionListServiceResponse;
import com.agilerunner.api.service.review.request.ManualRerunQueryServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunQueryServiceResponse;
import com.agilerunner.api.service.review.response.ManualRerunServiceResponse;
import com.agilerunner.api.service.review.response.ManualRerunRetryServiceResponse;
import com.agilerunner.domain.exception.ManualRerunQueryNotFoundException;
import com.agilerunner.domain.exception.ManualRerunRetryConflictException;
import com.agilerunner.domain.exception.ManualRerunRetryNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews/rerun")
public class ManualRerunController {

    private final ManualRerunService manualRerunService;
    private final ManualRerunExecutionListService manualRerunExecutionListService;
    private final ManualRerunControlActionService manualRerunControlActionService;
    private final ManualRerunQueryService manualRerunQueryService;
    private final ManualRerunRetryService manualRerunRetryService;

    @PostMapping
    public ResponseEntity<ManualRerunResponse> rerun(@RequestBody ManualRerunRequest request) {
        ManualRerunServiceResponse response = manualRerunService.rerun(request.toServiceRequest());
        return ResponseEntity.ok(ManualRerunResponse.from(response));
    }

    @GetMapping("/executions")
    public ResponseEntity<ManualRerunExecutionListResponse> listExecutions(ManualRerunExecutionListRequest request) {
        ManualRerunExecutionListServiceResponse response = manualRerunExecutionListService.list(request.toServiceRequest());
        return ResponseEntity.ok(ManualRerunExecutionListResponse.from(response));
    }

    @PostMapping("/{executionKey}/actions")
    public ResponseEntity<ManualRerunControlActionResponse> executeAction(@PathVariable String executionKey,
                                                                          @RequestBody ManualRerunControlActionRequest request) {
        ManualRerunControlActionServiceResponse response = manualRerunControlActionService.execute(
                request.toServiceRequest(executionKey)
        );
        return ResponseEntity.ok(ManualRerunControlActionResponse.from(response));
    }

    @GetMapping("/{executionKey}")
    public ResponseEntity<ManualRerunQueryResponse> getRerunResult(@PathVariable String executionKey) {
        ManualRerunQueryServiceResponse response = manualRerunQueryService.find(
                ManualRerunQueryServiceRequest.of(executionKey)
        );
        return ResponseEntity.ok(ManualRerunQueryResponse.from(response));
    }

    @PostMapping("/{executionKey}/retry")
    public ResponseEntity<ManualRerunRetryResponse> retry(@PathVariable String executionKey,
                                                          @RequestBody ManualRerunRetryRequest request) {
        ManualRerunRetryServiceResponse response = manualRerunRetryService.retry(request.toServiceRequest(executionKey));
        return ResponseEntity.ok(ManualRerunRetryResponse.from(response));
    }

    @ExceptionHandler(ManualRerunQueryNotFoundException.class)
    public ResponseEntity<ManualRerunQueryNotFoundResponse> handleManualRerunQueryNotFound(
            ManualRerunQueryNotFoundException exception
    ) {
        return ResponseEntity.status(404).body(
                ManualRerunQueryNotFoundResponse.of(exception.getExecutionKey(), exception.getMessage())
        );
    }

    @ExceptionHandler(ManualRerunRetryNotFoundException.class)
    public ResponseEntity<ManualRerunRetryNotFoundResponse> handleManualRerunRetryNotFound(
            ManualRerunRetryNotFoundException exception
    ) {
        return ResponseEntity.status(404).body(
                ManualRerunRetryNotFoundResponse.of(exception.getExecutionKey(), exception.getMessage())
        );
    }

    @ExceptionHandler(ManualRerunRetryConflictException.class)
    public ResponseEntity<ManualRerunRetryConflictResponse> handleManualRerunRetryConflict(
            ManualRerunRetryConflictException exception
    ) {
        return ResponseEntity.status(409).body(
                ManualRerunRetryConflictResponse.of(
                        exception.getExecutionKey(),
                        exception.getFailureDisposition(),
                        exception.getMessage()
                )
        );
    }
}
