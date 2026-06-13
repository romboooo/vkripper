package org.example.camunda.worker;

import lombok.RequiredArgsConstructor;
import org.example.camunda.externalTask.CamundaExternalTask;
import org.example.dto.response.ShoppingCartResponse;
import org.example.service.ShoppingCartServiceImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.example.camunda.externalTask.CamundaExternalTaskVariables.integerVariable;
import static org.example.camunda.externalTask.CamundaExternalTaskVariables.longVariable;

@Component
@RequiredArgsConstructor
public class CartProcessWorker {
    private final CamundaWorkerProperties properties;
    private final BusinessWorkerSupport workerSupport;
    private final ShoppingCartServiceImpl shoppingCartService;

    @Scheduled(fixedDelayString = "${app.camunda.worker.poll-delay:5000}")
    public void poll() {
        if (!properties.isEnabled()) {
            return;
        }

        workerSupport.pollTopic(properties.getValidateCartAddTopic(), "cartAddStatus", this::validateCartAdd);
        workerSupport.pollTopic(properties.getExecuteCartAddTopic(), "cartAddStatus", this::executeCartAdd);
        workerSupport.pollTopic(properties.getValidateCartUpdateTopic(), "cartUpdateStatus", this::validateCartUpdate);
        workerSupport.pollTopic(properties.getExecuteCartUpdateTopic(), "cartUpdateStatus", this::executeCartUpdate);
        workerSupport.pollTopic(properties.getValidateCartRemoveTopic(), "cartRemoveStatus", this::validateCartRemove);
        workerSupport.pollTopic(properties.getExecuteCartRemoveTopic(), "cartRemoveStatus", this::executeCartRemove);
    }

    private Map<String, Object> validateCartAdd(CamundaExternalTask task) {
        shoppingCartService.validateCartAddForProcess(userId(task), productId(task));
        return workerSupport.ok("cartAddStatus");
    }

    private Map<String, Object> executeCartAdd(CamundaExternalTask task) {
        ShoppingCartResponse response = shoppingCartService.addToShoppingCartForProcess(userId(task), productId(task));
        Map<String, Object> variables = workerSupport.ok("cartAddStatus");
        variables.put("cartItemId", response.getId());
        return variables;
    }

    private Map<String, Object> validateCartUpdate(CamundaExternalTask task) {
        shoppingCartService.validateCartUpdateForProcess(userId(task), productId(task), amount(task));
        return workerSupport.ok("cartUpdateStatus");
    }

    private Map<String, Object> executeCartUpdate(CamundaExternalTask task) {
        ShoppingCartResponse response = shoppingCartService.updateProductAmountForProcess(userId(task), productId(task), amount(task));
        Map<String, Object> variables = workerSupport.ok("cartUpdateStatus");
        variables.put("cartItemId", response.getId());
        return variables;
    }

    private Map<String, Object> validateCartRemove(CamundaExternalTask task) {
        shoppingCartService.validateCartRemoveForProcess(userId(task), productId(task));
        return workerSupport.ok("cartRemoveStatus");
    }

    private Map<String, Object> executeCartRemove(CamundaExternalTask task) {
        shoppingCartService.deleteFromShoppingCartForProcess(userId(task), productId(task));
        return workerSupport.ok("cartRemoveStatus");
    }

    private Long userId(CamundaExternalTask task) {
        return longVariable(task, "userId");
    }

    private Long productId(CamundaExternalTask task) {
        return longVariable(task, "productId");
    }

    private Integer amount(CamundaExternalTask task) {
        return integerVariable(task, "amount");
    }
}
