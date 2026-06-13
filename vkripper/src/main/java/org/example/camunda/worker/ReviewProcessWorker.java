package org.example.camunda.worker;

import lombok.RequiredArgsConstructor;
import org.example.camunda.externalTask.CamundaExternalTask;
import org.example.dto.request.ReviewRequest;
import org.example.dto.response.ReviewResponse;
import org.example.service.ReviewServiceImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.example.camunda.externalTask.CamundaExternalTaskVariables.integerVariable;
import static org.example.camunda.externalTask.CamundaExternalTaskVariables.longVariable;
import static org.example.camunda.externalTask.CamundaExternalTaskVariables.optionalLongVariable;
import static org.example.camunda.externalTask.CamundaExternalTaskVariables.stringVariable;

@Component
@RequiredArgsConstructor
public class ReviewProcessWorker {
    private final CamundaWorkerProperties properties;
    private final BusinessWorkerSupport workerSupport;
    private final ReviewServiceImpl reviewService;

    @Scheduled(fixedDelayString = "${app.camunda.worker.poll-delay:5000}")
    public void poll() {
        if (!properties.isEnabled()) {
            return;
        }

        workerSupport.pollTopic(properties.getValidateReviewCreateTopic(), "reviewCreateStatus", this::validateReviewCreate);
        workerSupport.pollTopic(properties.getExecuteReviewCreateTopic(), "reviewCreateStatus", this::executeReviewCreate);
        workerSupport.pollTopic(properties.getValidateReviewUpdateTopic(), "reviewUpdateStatus", this::validateReviewUpdate);
        workerSupport.pollTopic(properties.getExecuteReviewUpdateTopic(), "reviewUpdateStatus", this::executeReviewUpdate);
        workerSupport.pollTopic(properties.getValidateReviewDeleteTopic(), "reviewDeleteStatus", this::validateReviewDelete);
        workerSupport.pollTopic(properties.getExecuteReviewDeleteTopic(), "reviewDeleteStatus", this::executeReviewDelete);
    }

    private Map<String, Object> validateReviewCreate(CamundaExternalTask task) {
        reviewService.validateReviewCreateForProcess(userId(task), role(task), reviewRequest(task));
        return workerSupport.ok("reviewCreateStatus");
    }

    private Map<String, Object> executeReviewCreate(CamundaExternalTask task) {
        ReviewResponse response = reviewService.createReviewForProcess(userId(task), role(task), reviewRequest(task));
        Map<String, Object> variables = workerSupport.ok("reviewCreateStatus");
        variables.put("reviewId", response.getId());
        return variables;
    }

    private Map<String, Object> validateReviewUpdate(CamundaExternalTask task) {
        reviewService.validateReviewUpdateForProcess(role(task), reviewId(task), reviewRequest(task));
        return workerSupport.ok("reviewUpdateStatus");
    }

    private Map<String, Object> executeReviewUpdate(CamundaExternalTask task) {
        ReviewResponse response = reviewService.updateReviewForProcess(role(task), reviewId(task), reviewRequest(task));
        Map<String, Object> variables = workerSupport.ok("reviewUpdateStatus");
        variables.put("reviewId", response.getId());
        return variables;
    }

    private Map<String, Object> validateReviewDelete(CamundaExternalTask task) {
        reviewService.validateReviewDeleteForProcess(role(task), reviewId(task));
        return workerSupport.ok("reviewDeleteStatus");
    }

    private Map<String, Object> executeReviewDelete(CamundaExternalTask task) {
        reviewService.deleteReviewForProcess(role(task), reviewId(task));
        return workerSupport.ok("reviewDeleteStatus");
    }

    private Long userId(CamundaExternalTask task) {
        return longVariable(task, "userId");
    }

    private Long reviewId(CamundaExternalTask task) {
        return longVariable(task, "reviewId");
    }

    private String role(CamundaExternalTask task) {
        return stringVariable(task, "role");
    }

    private ReviewRequest reviewRequest(CamundaExternalTask task) {
        ReviewRequest request = new ReviewRequest();
        request.setProductId(optionalLongVariable(task, "productId"));
        request.setRating(integerVariable(task, "rating"));
        request.setText(stringVariable(task, "text"));
        return request;
    }
}
