package com.sky.user.service;


import com.sky.model.user.dto.UserLoginDTO;
import com.sky.model.user.entirty.User;

public interface UserService {
    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    User wxLogin(UserLoginDTO userLoginDTO);

    User getById(Long id);
}
