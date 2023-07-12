package com.trodix.duckcloud.security;

import com.trodix.duckcloud.security.utils.CasbinFilePolicyLoader;
import com.trodix.duckcloud.security.utils.ConfigResourceLoader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.casbin.adapter.JDBCAdapter;
import org.casbin.jcasbin.main.Enforcer;
import org.casbin.jcasbin.model.Model;
import org.casbin.jcasbin.persist.Adapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class CasbinConfig {

    private final DataSource dataSource;

    private final ConfigResourceLoader configResourceLoader;

    @Value("${casbin.model}")
    private String casbinModel;

    @Value("${casbin.policy}")
    private String casbinPolicy;

    @Bean
    public Adapter casbinDatabaseAdapter() throws Exception {
        return new JDBCAdapter(dataSource);
    }

    @Bean
    public Enforcer casbinEnforcer() throws Exception {

        String casbinModelConfig = configResourceLoader.readConfig(casbinModel);

        Model model = new Model();
        model.loadModelFromText(casbinModelConfig);

        Enforcer enforcer = new Enforcer(model, casbinDatabaseAdapter());
        initDefaultPolicies(enforcer);

        return enforcer;
    }

    private void initDefaultPolicies(Enforcer enforcer) throws IOException {

        String casbinPolicyConfig = configResourceLoader.readConfig(casbinPolicy);
        CasbinFilePolicyLoader loader = new CasbinFilePolicyLoader();

        CasbinFilePolicyLoader.ConfigRules configRules = loader.parseRules(casbinPolicyConfig);
        loader.persistLocalPolicy(configRules, enforcer);

    }

}
