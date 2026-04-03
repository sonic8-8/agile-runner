package com.agilerunner.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.YamlPropertiesFactoryBean;
import org.springframework.core.io.ClassPathResource;

import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;

class AgentRuntimeConfigurationTest {

    @DisplayName("기본 설정은 local 프로필과 agent runtime 비활성 기본값을 가진다.")
    @Test
    void application_defaultsToLocalProfile() {
        // given
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application.yml"));

        // when
        Properties properties = yaml.getObject();

        // then
        assertThat(properties).isNotNull();
        assertThat(properties.getProperty("spring.profiles.default"))
                .isEqualTo("local");
        assertThat(properties.getProperty("agile-runner.agent-runtime.enabled"))
                .isEqualTo("false");
    }

    @DisplayName("local 프로필은 파일 기반 H2 agent runtime 저장을 활성화한다.")
    @Test
    void localProfile_enablesFileBasedAgentRuntimeStorage() {
        // given
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application-local.yml"));

        // when
        Properties properties = yaml.getObject();

        // then
        assertThat(properties).isNotNull();
        assertThat(properties.getProperty("agile-runner.agent-runtime.enabled"))
                .isEqualTo("true");
        assertThat(properties.getProperty("agile-runner.agent-runtime.datasource.url"))
                .startsWith("jdbc:h2:file:${user.home}/.agile-runner/agent-runtime/agile-runner");
        assertThat(properties.getProperty("agile-runner.agent-runtime.datasource.driver-class-name"))
                .isEqualTo("org.h2.Driver");
        assertThat(properties.getProperty("spring.h2.console.enabled"))
                .isEqualTo("true");
    }

    @DisplayName("prod 프로필은 agent runtime 저장을 비활성화한다.")
    @Test
    void prodProfile_disablesAgentRuntimeStorage() {
        // given
        YamlPropertiesFactoryBean yaml = new YamlPropertiesFactoryBean();
        yaml.setResources(new ClassPathResource("application-prod.yml"));

        // when
        Properties properties = yaml.getObject();

        // then
        assertThat(properties).isNotNull();
        assertThat(properties.getProperty("agile-runner.agent-runtime.enabled"))
                .isEqualTo("false");
    }
}
