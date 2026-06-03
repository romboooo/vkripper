package org.example.quartz;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.service.ShoppingCartService;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class ShoppingCartCleanup implements Job {

    private final ShoppingCartService shoppingCartService;

    @Override
    public void execute(JobExecutionContext context) {
        LocalDateTime threshold = LocalDateTime.now().minusMonths(6);

        int deletedCount = shoppingCartService.deleteOldShoppingCarts(threshold);

        log.info("Deleted {} old shopping carts", deletedCount);
    }
}