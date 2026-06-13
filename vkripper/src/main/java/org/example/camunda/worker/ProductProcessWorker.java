package org.example.camunda.worker;

import lombok.RequiredArgsConstructor;
import org.example.camunda.externalTask.CamundaExternalTask;
import org.example.dto.request.ProductRequest;
import org.example.dto.response.ProductResponse;
import org.example.entity.ProductGroup;
import org.example.service.ProductServiceImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.example.camunda.externalTask.CamundaExternalTaskVariables.bigDecimalVariable;
import static org.example.camunda.externalTask.CamundaExternalTaskVariables.booleanVariable;
import static org.example.camunda.externalTask.CamundaExternalTaskVariables.longVariable;
import static org.example.camunda.externalTask.CamundaExternalTaskVariables.stringVariable;

@Component
@RequiredArgsConstructor
public class ProductProcessWorker {
    private final CamundaWorkerProperties properties;
    private final BusinessWorkerSupport workerSupport;
    private final ProductServiceImpl productService;

    @Scheduled(fixedDelayString = "${app.camunda.worker.poll-delay:5000}")
    public void poll() {
        if (!properties.isEnabled()) {
            return;
        }

        workerSupport.pollTopic(properties.getValidateProductCreateTopic(), "productCreateStatus", this::validateProductCreate);
        workerSupport.pollTopic(properties.getExecuteProductCreateTopic(), "productCreateStatus", this::executeProductCreate);
        workerSupport.pollTopic(properties.getValidateProductDeleteTopic(), "productDeleteStatus", this::validateProductDelete);
        workerSupport.pollTopic(properties.getExecuteProductDeleteTopic(), "productDeleteStatus", this::executeProductDelete);
    }

    private Map<String, Object> validateProductCreate(CamundaExternalTask task) {
        productService.validateProductCreateForProcess(role(task), productRequest(task));
        return workerSupport.ok("productCreateStatus");
    }

    private Map<String, Object> executeProductCreate(CamundaExternalTask task) {
        ProductResponse response = productService.createProductForProcess(userId(task), role(task), productRequest(task));
        Map<String, Object> variables = workerSupport.ok("productCreateStatus");
        variables.put("productId", response.getId());
        return variables;
    }

    private Map<String, Object> validateProductDelete(CamundaExternalTask task) {
        productService.validateProductDeleteForProcess(userId(task), role(task), productId(task));
        return workerSupport.ok("productDeleteStatus");
    }

    private Map<String, Object> executeProductDelete(CamundaExternalTask task) {
        productService.deleteProductForProcess(userId(task), role(task), productId(task));
        return workerSupport.ok("productDeleteStatus");
    }

    private Long userId(CamundaExternalTask task) {
        return longVariable(task, "userId");
    }

    private Long productId(CamundaExternalTask task) {
        return longVariable(task, "productId");
    }

    private String role(CamundaExternalTask task) {
        return stringVariable(task, "role");
    }

    private ProductRequest productRequest(CamundaExternalTask task) {
        ProductRequest request = new ProductRequest();
        request.setName(stringVariable(task, "name"));
        request.setPrice(bigDecimalVariable(task, "price"));
        request.setAvailable(booleanVariable(task, "available"));
        request.setProductGroup(ProductGroup.valueOf(stringVariable(task, "productGroup")));
        return request;
    }
}
