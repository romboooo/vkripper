package org.example.camunda.worker;

import lombok.RequiredArgsConstructor;
import org.example.camunda.externalTask.CamundaExternalTask;
import org.example.dto.response.ProductResponse;
import org.example.service.FavoriteServiceImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.example.camunda.externalTask.CamundaExternalTaskVariables.longVariable;

@Component
@RequiredArgsConstructor
public class FavoriteProcessWorker {
    private final CamundaWorkerProperties properties;
    private final BusinessWorkerSupport workerSupport;
    private final FavoriteServiceImpl favoriteService;

    @Scheduled(fixedDelayString = "${app.camunda.worker.poll-delay:5000}")
    public void poll() {
        if (!properties.isEnabled()) {
            return;
        }

        workerSupport.pollTopic(properties.getValidateFavoriteAddTopic(), "favoriteAddStatus", this::validateFavoriteAdd);
        workerSupport.pollTopic(properties.getExecuteFavoriteAddTopic(), "favoriteAddStatus", this::executeFavoriteAdd);
        workerSupport.pollTopic(properties.getValidateFavoriteRemoveTopic(), "favoriteRemoveStatus", this::validateFavoriteRemove);
        workerSupport.pollTopic(properties.getExecuteFavoriteRemoveTopic(), "favoriteRemoveStatus", this::executeFavoriteRemove);
    }

    private Map<String, Object> validateFavoriteAdd(CamundaExternalTask task) {
        favoriteService.validateFavoriteAddForProcess(userId(task), productId(task));
        return workerSupport.ok("favoriteAddStatus");
    }

    private Map<String, Object> executeFavoriteAdd(CamundaExternalTask task) {
        ProductResponse response = favoriteService.addToFavoriteForProcess(userId(task), productId(task));
        Map<String, Object> variables = workerSupport.ok("favoriteAddStatus");
        variables.put("productId", response.getId());
        return variables;
    }

    private Map<String, Object> validateFavoriteRemove(CamundaExternalTask task) {
        favoriteService.validateFavoriteRemoveForProcess(userId(task), productId(task));
        return workerSupport.ok("favoriteRemoveStatus");
    }

    private Map<String, Object> executeFavoriteRemove(CamundaExternalTask task) {
        favoriteService.deleteFromFavoriteForProcess(userId(task), productId(task));
        return workerSupport.ok("favoriteRemoveStatus");
    }

    private Long userId(CamundaExternalTask task) {
        return longVariable(task, "userId");
    }

    private Long productId(CamundaExternalTask task) {
        return longVariable(task, "productId");
    }
}
