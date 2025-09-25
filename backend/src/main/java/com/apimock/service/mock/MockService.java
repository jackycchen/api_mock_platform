package com.apimock.service.mock;

import com.apimock.entity.api.ApiDefinition;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

/**
 * Mock数据生成服务
 */
@Service
public class MockService {

    private static final Logger logger = LoggerFactory.getLogger(MockService.class);

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * 根据API ID生成Mock响应
     */
    public String generateMockResponse(Long apiId) {
        // 这里是简化的Mock数据生成
        // 在实际实现中，应该根据API定义生成相应的Mock数据

        Map<String, Object> mockData = new HashMap<>();
        mockData.put("code", 200);
        mockData.put("message", "success");
        mockData.put("data", generateSampleData());
        mockData.put("timestamp", LocalDateTime.now().toString());

        try {
            return objectMapper.writeValueAsString(mockData);
        } catch (Exception e) {
            logger.error("Failed to generate mock response for API ID: {}", apiId, e);
            return "{\"code\": 500, \"message\": \"Mock generation error\", \"timestamp\": \"" +
                   LocalDateTime.now() + "\"}";
        }
    }

    /**
     * 根据API定义生成Mock响应
     */
    public String generateMockResponse(ApiDefinition apiDefinition) {
        try {
            // 根据API定义生成更精确的Mock数据
            Map<String, Object> mockData = new HashMap<>();
            mockData.put("code", 200);
            mockData.put("message", "success");

            // 这里可以根据API的响应定义生成更准确的Mock数据
            // 目前使用简化的示例数据
            mockData.put("data", generateDataByApiDefinition(apiDefinition));
            mockData.put("timestamp", LocalDateTime.now().toString());

            return objectMapper.writeValueAsString(mockData);
        } catch (Exception e) {
            logger.error("Failed to generate mock response for API: {} {}",
                apiDefinition.getMethod(), apiDefinition.getPath(), e);
            return generateErrorResponse("Mock generation error: " + e.getMessage());
        }
    }

    /**
     * 生成示例数据
     */
    private Object generateSampleData() {
        Map<String, Object> data = new HashMap<>();
        data.put("id", new Random().nextInt(1000) + 1);
        data.put("name", "Mock User " + (new Random().nextInt(100) + 1));
        data.put("email", "user" + new Random().nextInt(1000) + "@example.com");
        data.put("phone", "138" + String.format("%08d", new Random().nextInt(100000000)));
        data.put("status", "active");
        data.put("createTime", LocalDateTime.now().toString());
        data.put("updateTime", LocalDateTime.now().toString());

        return data;
    }

    /**
     * 根据API定义生成数据
     */
    private Object generateDataByApiDefinition(ApiDefinition apiDefinition) {
        // 根据API的method和path生成不同的Mock数据
        String method = apiDefinition.getMethod().toUpperCase();
        String path = apiDefinition.getPath().toLowerCase();

        Map<String, Object> data = new HashMap<>();

        if (path.contains("user")) {
            return generateUserMockData();
        } else if (path.contains("order")) {
            return generateOrderMockData();
        } else if (path.contains("product")) {
            return generateProductMockData();
        } else if (method.equals("POST") && path.contains("login")) {
            return generateLoginMockData();
        } else if (method.equals("GET") && path.contains("list")) {
            return generateListMockData();
        }

        // 默认返回通用数据
        return generateSampleData();
    }

    private Object generateUserMockData() {
        Map<String, Object> user = new HashMap<>();
        user.put("id", new Random().nextInt(1000) + 1);
        user.put("username", "mockuser" + new Random().nextInt(100));
        user.put("nickname", "Mock User");
        user.put("email", "mock@example.com");
        user.put("phone", "13800138000");
        user.put("avatar", "https://example.com/avatar.jpg");
        user.put("status", 1);
        user.put("createTime", LocalDateTime.now().toString());
        return user;
    }

    private Object generateOrderMockData() {
        Map<String, Object> order = new HashMap<>();
        order.put("id", "ORD" + System.currentTimeMillis());
        order.put("userId", new Random().nextInt(1000) + 1);
        order.put("amount", new Random().nextDouble() * 1000);
        order.put("status", "pending");
        order.put("createTime", LocalDateTime.now().toString());

        List<Map<String, Object>> items = new ArrayList<>();
        Map<String, Object> item = new HashMap<>();
        item.put("productId", new Random().nextInt(100) + 1);
        item.put("productName", "Mock Product");
        item.put("quantity", new Random().nextInt(5) + 1);
        item.put("price", new Random().nextDouble() * 100);
        items.add(item);

        order.put("items", items);
        return order;
    }

    private Object generateProductMockData() {
        Map<String, Object> product = new HashMap<>();
        product.put("id", new Random().nextInt(1000) + 1);
        product.put("name", "Mock Product " + new Random().nextInt(100));
        product.put("description", "This is a mock product description");
        product.put("price", new Random().nextDouble() * 1000);
        product.put("stock", new Random().nextInt(100));
        product.put("category", "Electronics");
        product.put("status", "active");
        product.put("createTime", LocalDateTime.now().toString());
        return product;
    }

    private Object generateLoginMockData() {
        Map<String, Object> loginData = new HashMap<>();
        loginData.put("token", "mock_jwt_token_" + System.currentTimeMillis());
        loginData.put("refreshToken", "mock_refresh_token_" + System.currentTimeMillis());
        loginData.put("expireTime", System.currentTimeMillis() + 7200000); // 2小时后过期

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", 1);
        userInfo.put("username", "mockuser");
        userInfo.put("nickname", "Mock User");
        userInfo.put("role", "user");

        loginData.put("userInfo", userInfo);
        return loginData;
    }

    private Object generateListMockData() {
        List<Object> list = new ArrayList<>();
        int count = new Random().nextInt(10) + 5; // 5-15条记录

        for (int i = 0; i < count; i++) {
            list.add(generateSampleData());
        }

        Map<String, Object> pageData = new HashMap<>();
        pageData.put("list", list);
        pageData.put("total", count + new Random().nextInt(100));
        pageData.put("page", 1);
        pageData.put("pageSize", count);

        return pageData;
    }

    /**
     * 生成错误响应
     */
    private String generateErrorResponse(String errorMessage) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("code", 500);
        errorData.put("message", errorMessage);
        errorData.put("data", null);
        errorData.put("timestamp", LocalDateTime.now().toString());

        try {
            return objectMapper.writeValueAsString(errorData);
        } catch (Exception e) {
            logger.error("Failed to generate error response", e);
            return "{\"code\": 500, \"message\": \"Internal server error\", \"timestamp\": \"" +
                   LocalDateTime.now() + "\"}";
        }
    }
}