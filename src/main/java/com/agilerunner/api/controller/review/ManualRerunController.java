package com.agilerunner.api.controller.review;

import com.agilerunner.api.controller.review.request.ManualRerunRequest;
import com.agilerunner.api.controller.review.response.ManualRerunResponse;
import com.agilerunner.api.service.review.ManualRerunService;
import com.agilerunner.api.service.review.response.ManualRerunServiceResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reviews/rerun")
public class ManualRerunController {

    private final ManualRerunService manualRerunService;

    @PostMapping
    public ResponseEntity<ManualRerunResponse> rerun(@RequestBody ManualRerunRequest request) {
        ManualRerunServiceResponse response = manualRerunService.rerun(request.toServiceRequest());
        return ResponseEntity.ok(ManualRerunResponse.from(response));
    }
}
