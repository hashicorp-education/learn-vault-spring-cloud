package com.hashicorp.vaultgraalvm;

import org.aopalliance.intercept.MethodInterceptor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.aop.SpringProxy;
import org.springframework.aop.framework.Advised;
import org.springframework.aop.framework.ProxyFactoryBean;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.cloud.endpoint.event.RefreshEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportRuntimeHints;
import org.springframework.core.DecoratingProxy;
import org.springframework.vault.core.lease.SecretLeaseContainer;
import org.springframework.vault.core.lease.domain.RequestedSecret;
import org.springframework.vault.core.lease.event.SecretLeaseCreatedEvent;
import org.springframework.vault.core.lease.event.SecretLeaseExpiredEvent;

import javax.sql.DataSource;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@Configuration
@ImportRuntimeHints(RefreshableDataSourceVaultConfiguration.RefreshableDataSourceHints.class)
public class RefreshableDataSourceVaultConfiguration {
    private final Log log = LogFactory.getLog(getClass());

    private final ApplicationEventPublisher publisher;

    RefreshableDataSourceVaultConfiguration(
            @Value("${spring.cloud.vault.database.role}") String databaseRole,
            @Value("${spring.cloud.vault.database.backend}") String databaseBackend,
            DataSourceProperties properties,
            SecretLeaseContainer leaseContainer,
            ApplicationEventPublisher publisher) {
        this.publisher = publisher;

        var vaultCredsPath = String.format(
                "%s/creds/%s", databaseBackend, databaseRole);
        leaseContainer.addLeaseListener(event -> {
            if (vaultCredsPath.equals(event.getSource().getPath())) {
                if (event instanceof SecretLeaseExpiredEvent &&
                        event.getSource().getMode() == RequestedSecret.Mode.RENEW) {
                    log.info("expire lease, rotate database credentials");
                    leaseContainer.requestRotatingSecret(vaultCredsPath);
                } else if (event instanceof SecretLeaseCreatedEvent secretLeaseCreatedEvent
                        && event.getSource().getMode() == RequestedSecret.Mode.ROTATE) {

                    String username = (String) secretLeaseCreatedEvent.getSecrets()
                            .get("username");
                    String password = (String) secretLeaseCreatedEvent.getSecrets()
                            .get("password");

                    log.info("update database properties : " + username + "," + password);
                    properties.setUsername(username);
                    properties.setPassword(password);

                    refresh();
                }
            }
        });
    }

    @Bean
    DataSource dataSource(DataSourceProperties properties) {
        var rebuild = (Function<DataSourceProperties, DataSource>) dataSourceProperties -> {
            log.info("build data source: " + properties.getUsername() + "," + properties.getPassword());

            return DataSourceBuilder
                    .create()
                    .url(properties.getUrl())
                    .username(properties.getUsername())
                    .password(properties.getPassword())
                    .build();

        };

        var delegate = new AtomicReference<>(rebuild.apply(properties));

        var pfb = new ProxyFactoryBean();
        pfb.addInterface(DataSource.class);
        pfb.addInterface(RefreshedEventListener.class);
        pfb.addAdvice((MethodInterceptor) invocation -> {
            var methodName = invocation.getMethod().getName();
            if (methodName.equals("onApplicationEvent")) {
                delegate.set(rebuild.apply(properties));
                return null;
            }
            return invocation.getMethod()
                    .invoke(delegate.get(), invocation.getArguments());
        });
        return (DataSource) pfb.getObject();
    }


    static class RefreshableDataSourceHints implements RuntimeHintsRegistrar {

        @Override
        public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
            hints.proxies().registerJdkProxy(DataSource.class, RefreshedEventListener.class,
                    SpringProxy.class, Advised.class, DecoratingProxy.class);
        }
    }

    interface RefreshedEventListener extends ApplicationListener<RefreshEvent> {
    }

    private void refresh() {
        this.publisher.publishEvent(new RefreshEvent(this, null,
                "refresh database connection with new Vault credentials"));
    }
}

