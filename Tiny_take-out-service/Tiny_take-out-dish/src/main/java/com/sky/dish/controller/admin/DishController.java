package com.sky.dish.controller.admin;


import com.sky.common.result.PageResult;
import com.sky.common.result.Result;
import com.sky.dish.service.DishService;
import com.sky.model.dish.dto.DishDTO;
import com.sky.model.dish.dto.DishPageQueryDTO;
import com.sky.model.dish.entity.Dish;
import com.sky.model.dish.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * 菜品管理
 */
@RestController
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
@Slf4j
public class DishController {
    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 新增菜品
     * @return
     */
    @PostMapping
    @ApiOperation("新增菜品")
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品：{}",dishDTO);
        dishService.saveWithFlavor(dishDTO);

        // 清理缓存
        String key = "dish_" + dishDTO.getCategoryId();
        cleanCache(key);
        return Result.success();
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("分页查询菜品")
    public Result<PageResult> page(DishPageQueryDTO dishPageQueryDTO){
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 菜品的批量删除
     * @RequestParam 将参数解析到list中
     * @param ids
     * @return
     */
    @DeleteMapping
    @ApiOperation("菜品的批量删除")
    public Result delete(@RequestParam List<Long> ids){
        log.info("菜品的批量删除：{}",ids);
        dishService.deleteBacth(ids);

        // 清除缓存,如果要删除单个还要从新查库，所以直接删除
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 根据id查询菜品
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据id查询菜品")
    public Result<DishVO> getById(@PathVariable Long id){
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    /**
     * 修改菜品信息
     * @param dishDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改菜品接口")
    public Result update(@RequestBody DishDTO dishDTO){
        log.info("修改菜品：{}",dishDTO);
        dishService.updateWithFlavor(dishDTO);

        // 清除缓存,如果要删除单个还要从新查库，所以直接删除
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 起售、停售菜品
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("起售停售菜品接口")
    public Result startOrStop(@PathVariable Integer status,Long id){
        log.info("起售停售菜品：{}，{}",status,id);
        dishService.startOrStop(status,id);

        // 清除缓存,如果要删除单个还要从新查库，所以直接删除
        cleanCache("dish_*");
        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        log.info("查询id：{}",categoryId);
        List<Dish> list= dishService.list(categoryId);
        return Result.success(list);
    }

    /**
     * 删除缓存
     * redis不能以通配符的方式删除缓存，所以先以通配符查出数据再删除
     * @param pattern
     */
    private void cleanCache(String pattern){
        Set keys = redisTemplate.keys(pattern);
        redisTemplate.delete(keys);
    }
}
