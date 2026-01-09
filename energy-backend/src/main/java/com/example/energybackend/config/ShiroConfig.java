package com.example.energybackend.config;

import com.example.energybackend.realm.JwtRealm;
import com.example.energybackend.filter.JwtFilter;
import org.apache.shiro.mgt.DefaultSessionStorageEvaluator;
import org.apache.shiro.mgt.DefaultSubjectDAO;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

@Configuration
public class ShiroConfig {

    @Autowired
    private JwtFilter jwtFilter; // 直接从Spring容器注入JwtFilter

    @Autowired
    private JwtRealm jwtRealm; // 直接从Spring容器注入JwtRealm

    // 注意：这里删除了 @Bean public JwtRealm jwtRealm() 方法
    // 因为JwtRealm已经标记为@Component，由Spring管理

    // 2. 配置SecurityManager
    @Bean
    public DefaultWebSecurityManager securityManager() { // 去掉参数
        DefaultWebSecurityManager securityManager = new DefaultWebSecurityManager();
        securityManager.setRealm(jwtRealm); // 使用注入的jwtRealm

        // 禁用Session（JWT无状态）
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator sessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        sessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(sessionStorageEvaluator);
        securityManager.setSubjectDAO(subjectDAO);

        return securityManager;
    }

    // 3. 配置ShiroFilter
    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(DefaultWebSecurityManager securityManager) {
        ShiroFilterFactoryBean filterFactory = new ShiroFilterFactoryBean();
        filterFactory.setSecurityManager(securityManager);

        // 注册自定义JWT过滤器 - 使用从Spring容器注入的jwtFilter
        Map<String, Filter> filters = new HashMap<>();
        filters.put("jwt", jwtFilter); // 使用注入的jwtFilter，而不是new JwtFilter()
        filterFactory.setFilters(filters);

        // 配置过滤规则：anon=匿名访问，jwt=需要JWT认证
        // 使用LinkedHashMap保持顺序
        Map<String, String> filterChainDefinitionMap = new LinkedHashMap<>();

        // 匿名访问接口（必须放在前面）
        filterChainDefinitionMap.put("/auth/login", "anon");
        filterChainDefinitionMap.put("/test/jwt/**", "anon");
        filterChainDefinitionMap.put("/doc.html", "anon");
        filterChainDefinitionMap.put("/webjars/**", "anon");
        filterChainDefinitionMap.put("/swagger-resources/**", "anon");
        filterChainDefinitionMap.put("/v2/api-docs", "anon");
        filterChainDefinitionMap.put("/swagger-ui.html", "anon");

        // 所有其他接口需要JWT认证（必须放在最后）
        filterChainDefinitionMap.put("/**", "jwt");

        filterFactory.setFilterChainDefinitionMap(filterChainDefinitionMap);

        return filterFactory;
    }
}
