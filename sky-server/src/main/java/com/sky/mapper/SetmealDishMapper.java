package com.sky.mapper;


import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface SetmealDishMapper {

    /**
     * 根据菜品ids 查询 套餐ids
     * select setmeal_id from setmeal_dish where dish_id in (1,2,3,4)
     * @param dishIds
     * @return
     */
    List<Long> getSetmealIdsByDishIds(List<Long> dishIds);
}
