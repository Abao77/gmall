package com.abao.gmall.config;

import com.abao.gmall.HttpClientUtil;
import com.alibaba.fastjson.JSON;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {


    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String token = request.getParameter("newToken");


        if(!StringUtils.isEmpty(token)){
            //写到cookie
            CookieUtil.setCookie(request,response,"token",token,WebConst.COOKIE_MAXAGE,false);
        }



        if(StringUtils.isEmpty(token)){
            //从cookie中获取
            token = CookieUtil.getCookieValue(request, "token", false);
        }



        if(!StringUtils.isEmpty(token)){
            //取用户名
            Map userMap = MakeUserMap(token);

            String nickName = (String) userMap.get("nickName");

            request.setAttribute("nickName",nickName);
        }





        HandlerMethod handlerMethod = (HandlerMethod)handler;
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);

        //加LoginRequire注解了
        if(methodAnnotation != null){


            //验证是否登录  http://passport.gmall.com/verify?token=xx&salt=x

            String salt = request.getHeader("X-forwarded-for");

            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&salt=" + salt);

            if("success".equals(result)){//登录了

                //取用户id,放到request域中
                Map userMap = MakeUserMap(token);
                String userId = (String)userMap.get("id");

                request.setAttribute("userId",userId);

            }else{//没有登录


                boolean b = methodAnnotation.autoRedirect();

                if(b){//注解=true 必须登录

                    String originUrl = request.getRequestURL().toString();

                    //重定向到登录页面进行登录
                    response.sendRedirect(WebConst.LOGIN_ADDRESS + "?originUrl=" + URLEncoder.encode(originUrl));

                    return false;
                }

            }

        }

        return true;
    }

    private Map MakeUserMap(String token) {
        //截取token主体信息解码
        String uToken = StringUtils.substringBetween(token, ".");

        Base64UrlCodec base64 = new Base64UrlCodec();

        byte[] decode = base64.decode(uToken);

        String userStr = new String(decode);

        Map userMap = new HashMap();

        if(!StringUtils.isEmpty(userStr)){
            userMap = JSON.parseObject(userStr, Map.class);

        }

        return userMap;
    }



    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {





    }

    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {





    }

}
