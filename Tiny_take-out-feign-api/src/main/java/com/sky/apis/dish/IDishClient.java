package com.sky.apis.dish;

import com.sky.model.dish.entity.Dish;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "take-out-dish",path="/feign")
public interface IDishClient {

    @GetMapping("/dish/{id}")
    Dish getById(@PathVariable Long id);
}
