package com.hashicorp.vaultstaticsecrets;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
class VaultRefresher {
    private final Log log = LogFactory.getLog(getClass());

    private final ContextRefresher contextRefresher;

    VaultRefresher(ContextRefresher contextRefresher) {
        this.contextRefresher = contextRefresher;
    }

    @Scheduled(initialDelayString="${secrets.refresh-interval-ms}",
            fixedDelayString = "${secrets.refresh-interval-ms}")
    void refresher() {
        contextRefresher.refresh();
        log.info("refresh key-value secret");
    }
}
