package com.xuecheng.ucenter.service.Impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.xuecheng.ucenter.mapper.XcMenuMapper;
import com.xuecheng.ucenter.mapper.XcUserMapper;
import com.xuecheng.ucenter.model.dto.AuthParamsDto;
import com.xuecheng.ucenter.model.dto.XcUserExt;
import com.xuecheng.ucenter.model.po.XcMenu;
import com.xuecheng.ucenter.model.po.XcUser;
import com.xuecheng.ucenter.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.context.ApplicationContext;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

//当查询用户的时候，实现了UserDetailsService接口的这个方法，就会执行，从数据库查询用户
@Component
@Slf4j
public class UserServiceImpl implements UserDetailsService {

    @Autowired
    XcUserMapper xcUserMapper;

    @Autowired
    XcMenuMapper xcMenuMapper;

    @Autowired
    ApplicationContext applicationContext;

    //登陆时这里传入的请求认证参数就是AuthParamsDto，而不是用户名
    @Override
    public UserDetails loadUserByUsername(String s){
        //将传入的json转成AuthParamsDto对象
        AuthParamsDto authParamsDto=null;
        try {
            authParamsDto = JSON.parseObject(s, AuthParamsDto.class);
        }catch (Exception e){
            throw new RuntimeException("请求参数不符合认证要求");
        }

        //认证类型
        String authType = authParamsDto.getAuthType();
        //根据请求类型从spring中取出指定的bean
        String beanName=authType+"_authService";
        AuthService authService = applicationContext.getBean(beanName, AuthService.class);
        //调用相应的认证方法进行认证
        XcUserExt execute = authService.execute(authParamsDto);

        //封装XcUserExt为UserDetails
        //sercurity根据返回的UserDetails生成令牌，所以在这里对用户加权限
        UserDetails userDetails = getUserDetails(execute);

        return userDetails;
    }
    /**
     * 根据 XcUserExt 对象构造一个 UserDetails 对象
     *
     * @param userExt userExt 对象-用户信息
     * @return {@link UserDetails}
     */
    public UserDetails getUserDetails(XcUserExt userExt){
        String password = userExt.getPassword();
        //如果查到，就封装成UserDetails返回
        //对用户加权限
        String[] authorities={"test"};
        List<XcMenu> xcMenus = xcMenuMapper.selectPermissionByUserId(userExt.getId());
        ArrayList<String> permissions = new ArrayList<>();
        if(xcMenus.size()>0){
            xcMenus.stream().forEach(item->{
                permissions.add(item.getCode());
            });
            authorities= permissions.toArray(new String[0]);
        }

        //原来只有username信息不够，需要拓展
        //将user对象信息转成json形式，到时候用的时候直接转就行了
        userExt.setPassword(null);
        String userJson = JSON.toJSONString(userExt);
        UserDetails userDetails = User.withUsername(userJson).password(password).authorities(authorities).build();
        return userDetails;
    }
}
