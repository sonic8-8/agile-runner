package com.agilerunner.config;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;

import javax.sql.DataSource;

@Configuration
public class AgentRuntimeDataSourceConfig {

    @Bean
    @ConditionalOnProperty(prefix = "agile-runner.agent-runtime", name = "enabled", havingValue = "true")
    @ConfigurationProperties(prefix = "agile-runner.agent-runtime.datasource")
    public DataSourceProperties agentRuntimeDataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @ConditionalOnProperty(prefix = "agile-runner.agent-runtime", name = "enabled", havingValue = "true")
    public DataSource agentRuntimeDataSource(
            @Qualifier("agentRuntimeDataSourceProperties") DataSourceProperties agentRuntimeDataSourceProperties
    ) {
        return agentRuntimeDataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "agile-runner.agent-runtime", name = "enabled", havingValue = "true")
    public NamedParameterJdbcTemplate agentRuntimeNamedParameterJdbcTemplate(
            @Qualifier("agentRuntimeDataSource") DataSource agentRuntimeDataSource
    ) {
        return new NamedParameterJdbcTemplate(agentRuntimeDataSource);
    }

    @Bean
    @ConditionalOnProperty(prefix = "agile-runner.agent-runtime", name = "enabled", havingValue = "true")
    public DataSourceInitializer agentRuntimeSchemaInitializer(
            @Qualifier("agentRuntimeDataSource") DataSource agentRuntimeDataSource
    ) {
        ResourceDatabasePopulator populator = new ResourceDatabasePopulator(
                new ClassPathResource("agent-runtime/schema.sql")
        );

        DataSourceInitializer dataSourceInitializer = new DataSourceInitializer();
        dataSourceInitializer.setDataSource(agentRuntimeDataSource);
        dataSourceInitializer.setDatabasePopulator(populator);
        return dataSourceInitializer;
    }
}
