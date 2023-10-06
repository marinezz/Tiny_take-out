package com.sky.apis.dish;

import com.sky.model.dish.entity.Setmeal;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Set;

@FeignClient(name = "take-out-dish",path="/feign")
public interface ISetmealClient {

    @GetMapping("/setmeal/{id}")
    Setmeal getById(@PathVariable Long id);
}
