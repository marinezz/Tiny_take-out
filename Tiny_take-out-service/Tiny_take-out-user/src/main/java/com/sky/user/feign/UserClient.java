package com.sky.user.feign;

import com.sky.apis.user.IUserClient;
import com.sky.model.user.entirty.User;
import com.sky.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserClient implements IUserClient {

    @Autowired
    private UserService userService;

    @Override
    @GetMapping("/feign/user/{id}")
    public User getById(@PathVariable Long id) {
        return userService.getById(id);
    }
}
