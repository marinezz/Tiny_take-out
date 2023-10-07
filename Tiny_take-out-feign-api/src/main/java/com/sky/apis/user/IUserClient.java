package com.sky.apis.user;

import com.sky.model.user.entirty.User;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "take-out-user",path = "/feign")
public interface IUserClient {
    /**
     * 根据id获取用户
     * @param id
     * @return
     */
    @GetMapping("/user/{id}")
    User getById(@PathVariable Long id);

    /**
     * 根据条件，统计用户
     * @param map
     * @return
     */
    @PostMapping("/user/count")
    Integer countByMap(@RequestBody Map map);
}
