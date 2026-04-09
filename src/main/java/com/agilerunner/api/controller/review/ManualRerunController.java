package com.agilerunner.api.controller.review;

import com.agilerunner.api.controller.review.request.ManualRerunRequest;
import com.agilerunner.api.controller.review.response.ManualRerunQueryNotFoundResponse;
import com.agilerunner.api.controller.review.response.ManualRerunQueryResponse;
import com.agilerunner.api.controller.review.response.ManualRerunResponse;
import com.agilerunner.api.service.review.ManualRerunQueryService;
import com.agilerunner.api.service.review.ManualRerunService;
import com.agilerunner.api.service.review.request.ManualRerunQueryServiceRequest;
import com.agilerunner.api.service.review.response.ManualRerunQueryServiceResponse;
import com.agilerunner.api.service.review.response.ManualRerunServiceResponse;
import com.agilerunner.domain.exception.ManualRerunQueryNotFoundException;
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
    private final ManualRerunQueryService manualRerunQueryService;

    @PostMapping
    public ResponseEntity<ManualRerunResponse> rerun(@RequestBody ManualRerunRequest request) {
        ManualRerunServiceResponse response = manualRerunService.rerun(request.toServiceRequest());
        return ResponseEntity.ok(ManualRerunResponse.from(response));
    }

    @GetMapping("/{executionKey}")
    public ResponseEntity<ManualRerunQueryResponse> getRerunResult(@PathVariable String executionKey) {
        ManualRerunQueryServiceResponse response = manualRerunQueryService.find(
                ManualRerunQueryServiceRequest.of(executionKey)
        );
        return ResponseEntity.ok(ManualRerunQueryResponse.from(response));
    }

    @ExceptionHandler(ManualRerunQueryNotFoundException.class)
    public ResponseEntity<ManualRerunQueryNotFoundResponse> handleManualRerunQueryNotFound(
            ManualRerunQueryNotFoundException exception
    ) {
        return ResponseEntity.status(404).body(
                ManualRerunQueryNotFoundResponse.of(exception.getExecutionKey(), exception.getMessage())
        );
    }
}
