package com.sky.dish.feign;

import com.sky.apis.dish.IDishClient;
import com.sky.dish.service.DishService;
import com.sky.model.dish.entity.Dish;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class DishClient implements IDishClient {

    @Autowired
    private DishService dishService;

    /**
     * 根据id获取菜品
     * @param id
     * @return
     */
    @Override
    @GetMapping("/feign/dish/{id}")
    public Dish getById(@PathVariable Long id) {
        return dishService.getById(id);
    }

    /**
     * 获取菜品数量
     * @param map
     * @return
     */
    @Override
    @PostMapping("/feign/dish/count")
    public Integer countByMap(Map map) {
        return dishService.countByMap(map);
    }
}
