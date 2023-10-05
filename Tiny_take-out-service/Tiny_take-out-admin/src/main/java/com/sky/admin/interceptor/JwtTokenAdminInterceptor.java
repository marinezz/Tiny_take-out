package com.sky.admin.interceptor;


import com.sky.common.constant.JwtClaimsConstant;
import com.sky.common.context.BaseContext;
import com.sky.common.properties.JwtProperties;
import com.sky.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenAdminInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }
        String empId = request.getHeader("userId");
        if(empId != null){
            //存入到当前线程中
            Long Id = Long.valueOf(empId);
            BaseContext.setCurrentId(Id);
            log.info("当前员工id：{}", Id);
        }
        return true;

    }
}
