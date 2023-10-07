package com.sky.dish.service;


import com.sky.common.result.PageResult;
import com.sky.model.dish.dto.DishDTO;
import com.sky.model.dish.dto.DishPageQueryDTO;
import com.sky.model.dish.entity.Dish;
import com.sky.model.dish.vo.DishVO;

import java.util.List;
import java.util.Map;

public interface DishService {
    /**
     * 新增菜品和口味
     * @param dishDTO
     */
    void saveWithFlavor(DishDTO dishDTO);

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO);

    /**
     * 菜品的批量删除
     * @param ids
     */
    void deleteBacth(List<Long> ids);

    /**
     * 根据id查询菜品和对应的口味
     * @param id
     * @return
     */
    DishVO getByIdWithFlavor(Long id);

    /**
     * 修改菜品及其口味
     * @param dishDTO
     */
    void updateWithFlavor(DishDTO dishDTO);

    /**
     * 起售停售菜品
     * @param status
     * @param id
     */
    void startOrStop(Integer status, Long id);

    /**
     * 根据分类id查询菜品
     * @param id
     * @return
     */
    List<Dish> list(Long id);

    /**
     * 查询菜品以及口味数据
     * @param dish
     * @return
     */
    List<DishVO> listWithFlavor(Dish dish);

    /**
     * 根据id获取菜品
     * @param id
     * @return
     */
    Dish getById(Long id);

    /**
     * 获取菜品数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
