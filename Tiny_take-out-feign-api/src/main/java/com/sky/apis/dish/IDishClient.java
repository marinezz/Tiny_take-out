package com.sky.apis.dish;

import com.sky.model.dish.entity.Dish;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "take-out-dish",path="/feign")
public interface IDishClient {

    /**
     * 根据id获取菜品
     * @param id
     * @return
     */
    @GetMapping("/dish/{id}")
    Dish getById(@PathVariable Long id);

    /**
     * 获取菜品数量
     * @param map
     * @return
     */
    @GetMapping("/dish/count")
    Integer countByMap(@RequestBody Map map);
}
