package org.trt.micro.config;

import com.hujingli.micro.common.constant.AuthConstant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.config.annotation.configurers.ClientDetailsServiceConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configuration.AuthorizationServerConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableAuthorizationServer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerEndpointsConfigurer;
import org.springframework.security.oauth2.config.annotation.web.configurers.AuthorizationServerSecurityConfigurer;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;
import org.springframework.security.oauth2.provider.token.TokenEnhancerChain;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import org.springframework.security.oauth2.provider.token.store.KeyStoreKeyFactory;
import org.trt.micro.auth.DaoUserDetailsService;
import org.trt.micro.jwt.JwtTokenEnhancer;

import java.security.KeyPair;
import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="exphuhong@163.com">胡红</a>
 * @Date 2021 年 04 月 15 日
 * @Description oauth2配置文件
 */
@Configuration
@EnableAuthorizationServer
public class Oauth2ServerConfig extends AuthorizationServerConfigurerAdapter {

    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtTokenEnhancer tokenEnhancer;
    @Autowired
    private DaoUserDetailsService userDetailsService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Override
    public void configure(AuthorizationServerSecurityConfigurer authorizationServerSecurityConfigurer) throws Exception {
        authorizationServerSecurityConfigurer.allowFormAuthenticationForClients();
    }

    @Override
    public void configure(ClientDetailsServiceConfigurer clientDetailsServiceConfigurer) throws Exception {
        clientDetailsServiceConfigurer.inMemory()
                .withClient(AuthConstant.ADMIN_CLIENT_ID)
                .secret(passwordEncoder.encode(AuthConstant.KEY_PAIR_PWD))
                .authorizedGrantTypes("password", "refresh_token")
                .scopes("all")
                .accessTokenValiditySeconds(3600 * 24)
                .refreshTokenValiditySeconds(3600 * 24 * 7)
                .and()
                .withClient(AuthConstant.PORTAL_CLIENT_ID)
                .secret(passwordEncoder.encode(AuthConstant.KEY_PAIR_PWD))
                .scopes("all")
                .accessTokenValiditySeconds(3600 * 24)
                .refreshTokenValiditySeconds(3600 * 24 * 7)
        ;

    }

    @Override
    public void configure(AuthorizationServerEndpointsConfigurer endpoints) throws Exception {
        TokenEnhancerChain chain = new TokenEnhancerChain();
        List<TokenEnhancer> enhancers = new ArrayList<>();
        enhancers.add(tokenEnhancer);
        enhancers.add(jwtAccessTokenConverter());
        chain.setTokenEnhancers(enhancers);
        endpoints.authenticationManager(authenticationManager)
                .userDetailsService(userDetailsService)
                .accessTokenConverter(jwtAccessTokenConverter())
                .tokenEnhancer(chain);
    }

    @Bean
    public JwtAccessTokenConverter jwtAccessTokenConverter() {
        JwtAccessTokenConverter converter = new JwtAccessTokenConverter();
        // 从classpath 目录下 获取密钥对
        KeyPair keyPair = new KeyStoreKeyFactory(
                new ClassPathResource("jwt.jks"), AuthConstant.KEY_PAIR_PWD.toCharArray())
                .getKeyPair("jwt", AuthConstant.KEY_PAIR_PWD.toCharArray());
        converter.setKeyPair(keyPair);
        return converter;
    }
}
