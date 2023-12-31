package com.sky.dish.controller.user;


import com.sky.common.constant.StatusConstant;
import com.sky.common.result.Result;
import com.sky.dish.service.DishService;
import com.sky.model.dish.entity.Dish;
import com.sky.model.dish.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController("userDishController")
@RequestMapping("/user/dish")
@Slf4j
@Api(tags = "C端-菜品浏览接口")
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;
    /**
     * 根据分类id查询菜品
     *
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<DishVO>> list(Long categoryId) {
        // 查询redis中是否存在
        String key = "dish_" + categoryId;

        // 如果存在直接返回菜品数据
        List<DishVO> list = (List<DishVO>)redisTemplate.opsForValue().get(key);
        if (list != null && list.size() > 0){
            return Result.success(list);
        }

        // 如果不存在，查询数据库，将查询到的结果放入redis中
        Dish dish = new Dish();
        dish.setCategoryId(categoryId);
        dish.setStatus(StatusConstant.ENABLE);//查询起售中的菜品

        list = dishService.listWithFlavor(dish);
        redisTemplate.opsForValue().set(key,list);

        return Result.success(list);
    }

}
