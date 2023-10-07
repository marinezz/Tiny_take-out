package com.sky.dish.feign;

import com.sky.apis.dish.ISetmealClient;
import com.sky.dish.service.SetmealService;
import com.sky.model.dish.entity.Setmeal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class SetmealClient implements ISetmealClient {
    @Autowired
    private SetmealService setmealService;

    /**
     * 根据id获取套餐
     * @param id
     * @return
     */
    @Override
    @GetMapping("/feign/setmeal/{id}")
    public Setmeal getById(@PathVariable Long id) {
        return setmealService.getById(id);
    }

    /**
     * 获取套餐数量
     * @param map
     * @return
     */
    @Override
    public Integer countByMap(Map map) {
        return setmealService.countByMap(map);
    }
}
