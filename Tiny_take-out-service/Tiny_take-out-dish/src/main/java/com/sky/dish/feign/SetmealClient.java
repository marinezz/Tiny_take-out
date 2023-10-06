package com.sky.dish.feign;

import com.sky.apis.dish.ISetmealClient;
import com.sky.dish.service.SetmealService;
import com.sky.model.dish.entity.Setmeal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SetmealClient implements ISetmealClient {
    @Autowired
    private SetmealService setmealService;

    @Override
    @GetMapping("/feign/setmeal/{id}")
    public Setmeal getById(@PathVariable Long id) {
        return setmealService.getById(id);
    }
}
