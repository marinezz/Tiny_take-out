package com.sky.apis.user;

import com.sky.model.user.entirty.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "take-out-user",path = "/feign")
public interface IUserClient {
    @GetMapping("/feign/user/{id}")
    User getById(@PathVariable Long id);
}
