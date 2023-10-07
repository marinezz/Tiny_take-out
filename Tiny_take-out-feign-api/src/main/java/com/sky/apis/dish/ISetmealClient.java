package com.sky.apis.dish;

import com.sky.model.dish.entity.Setmeal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;
import java.util.Set;

@FeignClient(name = "take-out-dish",path="/feign")
public interface ISetmealClient {

    /**
     * 根据id获取套餐
     * @param id
     * @return
     */
    @GetMapping("/setmeal/{id}")
    Setmeal getById(@PathVariable Long id);

    /**
     * 获取套餐数量
     * @param map
     * @return
     */
    @PostMapping("/setmeal/count")
    Integer countByMap(@RequestBody Map map);
}
