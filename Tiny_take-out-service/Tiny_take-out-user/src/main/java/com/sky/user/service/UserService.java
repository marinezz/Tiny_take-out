package com.sky.user.service;


import com.sky.model.user.dto.UserLoginDTO;
import com.sky.model.user.entirty.User;

import java.util.Map;

public interface UserService {
    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    User wxLogin(UserLoginDTO userLoginDTO);

    /**
     * 根据id查询用户
     * @param id
     * @return
     */
    User getById(Long id);

    /**
     * 统计用户数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
