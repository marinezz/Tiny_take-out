package com.sky.dish.feign;

import com.sky.apis.dish.IDishClient;
import com.sky.dish.service.DishService;
import com.sky.model.dish.entity.Dish;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DishClient implements IDishClient {

    @Autowired
    private DishService dishService;

    @Override
    @GetMapping("/feign/dish/{id}")
    public Dish getById(@PathVariable Long id) {
        return dishService.getById(id);
    }
}
