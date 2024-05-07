package com.hashicorp.vaulttransit;

import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.core.VaultTemplate;

class VaultTransit {
    private final VaultOperations vault;
    private final String path;
    private final String key;

    VaultTransit(VaultTransitProperties properties, VaultTemplate vaultTemplate) {
        this.vault = vaultTemplate;
        this.path = properties.getPath();
        this.key = properties.getKey();
    }

    String decrypt(String ccInfo) {
        return vault.opsForTransit(path).decrypt(key, ccInfo);
    }

    String encrypt(String ccInfo) {
        return vault.opsForTransit(path).encrypt(key, ccInfo);
    }
}