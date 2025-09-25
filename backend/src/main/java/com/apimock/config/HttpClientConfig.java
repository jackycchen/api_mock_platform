package com.apimock.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * HTTP客户端配置
 */
@Configuration
public class HttpClientConfig {

    /**
     * 配置RestTemplate用于代理转发
     */
    @Bean
    public RestTemplate restTemplate() {
        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory();

        // 设置连接超时时间（毫秒）
        factory.setConnectTimeout(5000);

        // 设置读取超时时间（毫秒）
        factory.setReadTimeout(30000);

        return new RestTemplate(factory);
    }
}