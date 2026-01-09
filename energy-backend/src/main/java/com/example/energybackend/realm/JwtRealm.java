package com.example.energybackend.realm;

import com.example.energybackend.entity.User;
import com.example.energybackend.service.UserService;
import com.example.energybackend.util.JWTUtil;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component // 添加@Component注解
public class JwtRealm extends AuthorizingRealm {
    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private UserService userService;

    // 支持JWT Token
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    // 授权（获取用户角色/权限）
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
        String username = (String) principals.getPrimaryPrincipal();
        User user = userService.getUserByUsername(username);
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();
        info.addRole(user.getRole()); // 添加角色
        return info;
    }

    // 认证（校验JWT Token有效性）
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
        String jwtToken = (String) token.getCredentials();

        // 1. 从Token中获取用户名
        String username = jwtUtil.getUsernameFromToken(jwtToken);
        if (username == null) {
            throw new UnknownAccountException("Token无效");
        }

        // 2. 查询用户
        User user = userService.getUserByUsername(username);
        if (user == null) {
            throw new UnknownAccountException("用户不存在");
        }

        // 3. 校验Token是否过期
        if (jwtUtil.isTokenExpired(jwtToken)) {
            throw new ExpiredCredentialsException("Token已过期，请重新登录");
        }

        // 4. 返回认证信息
        return new SimpleAuthenticationInfo(username, jwtToken, getName());
    }

    // 自定义JWT Token类
    public static class JwtToken implements AuthenticationToken {
        private final String token;

        public JwtToken(String token) {
            this.token = token;
        }

        @Override
        public Object getPrincipal() {
            return token;
        }

        @Override
        public Object getCredentials() {
            return token;
        }
    }
}
