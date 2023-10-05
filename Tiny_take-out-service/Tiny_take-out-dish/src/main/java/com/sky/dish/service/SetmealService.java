package com.sky.dish.service;



import com.sky.common.result.PageResult;
import com.sky.model.dish.dto.SetmealDTO;
import com.sky.model.dish.dto.SetmealPageQueryDTO;
import com.sky.model.dish.entity.Setmeal;
import com.sky.model.dish.vo.DishItemVO;
import com.sky.model.dish.vo.SetmealVO;

import java.util.List;


public interface SetmealService {
    /**
     * 新增套餐
     * @param setmealDTO
     */
    void save(SetmealDTO setmealDTO);

    /**
     * 分页插叙菜品
     * @param setmealPageQueryDTO
     * @return
     */
    PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO);

    /**
     * 套餐批量删除
     * @param ids
     */
    void deleteBatch(List<Long> ids);

    /**
     * 修改套餐信息
     * @param setmealDTO
     */
    void updateWithDish(SetmealDTO setmealDTO);

    /**
     * 根据id查询套餐信息
     * @param id
     * @return
     */
    SetmealVO getByIdWithDish(Long id);

    /**
     * 起售停售套餐
     * @param status
     */
    void startOrStop(Integer status,Long id);


    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    List<Setmeal> list(Setmeal setmeal);

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    List<DishItemVO> getDishItemById(Long id);
}
