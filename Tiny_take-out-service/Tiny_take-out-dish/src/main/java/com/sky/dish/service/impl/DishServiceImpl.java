package com.sky.dish.service.impl;


import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import com.sky.common.constant.MessageConstant;
import com.sky.common.constant.StatusConstant;
import com.sky.common.exception.DeletionNotAllowedException;
import com.sky.common.result.PageResult;
import com.sky.dish.mapper.DishFlavorMapper;
import com.sky.dish.mapper.DishMapper;
import com.sky.dish.mapper.SetmealDishMapper;
import com.sky.dish.service.DishService;
import com.sky.model.dish.dto.DishDTO;
import com.sky.model.dish.dto.DishPageQueryDTO;
import com.sky.model.dish.entity.Dish;
import com.sky.model.dish.entity.DishFlavor;
import com.sky.model.dish.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
public class DishServiceImpl implements DishService {
    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private DishFlavorMapper dishFlavorMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    /**
     * 新增菜品和口味
     * 设计多个表的操作，需要用到注解，要在启动类上加入注解方式的事务管理
     * @param dishDTO
     */
    @Override
    @Transactional
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        // 向菜品表插入一条数据,只用传dish对象
        dishMapper.insert(dish);

        // 获取insert生成得id值
        Long dishId = dish.getId();

        // 向口味表插入多条数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0){
            // 赋值
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishId);
            });
            // 批量插入
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(),dishPageQueryDTO.getPageSize());
        Page<DishVO> page = dishMapper.pageQuery(dishPageQueryDTO);
        long total = page.getTotal();
        return new PageResult(total,page);
    }

    /**
     * 菜品的批量删除
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBacth(List<Long> ids) {
        // 判断菜品是否能删除  -- 起售中不能删除
        for(Long id : ids){
            Dish dish = dishMapper.getById(id);
            if(dish.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        // 判断菜品是否能删除  -- 被套餐关联不能删除
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if(setmealIds != null && setmealIds.size() >0){
            throw  new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

//        // 删除菜品表中的菜品数量
//        for (Long id : ids) {
//            dishMapper.deleteById(id);
//            // 删除菜品关联的口味数据,不管有没有数据都进行删除，就不用再查询一遍
//            dishFlavorMapper.deleteByDishId(id);
//        }

        // 代码优化 ： 批量删除
        // sql：delete from dish where id in (?,?,?)
        // 删除菜品表中的菜品数量
        dishMapper.deleteByIds(ids);
        dishFlavorMapper.deleteByDishIds(ids);

    }

    /**
     * 根据id查询菜品和对应的口味
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        // 查询菜品数据
        Dish dish = dishMapper.getById(id);

        // 根据菜品id查询口味数据
        List<DishFlavor> dishFlavors = dishFlavorMapper.getByDishId(id);

        // 封装到Vo
        DishVO dishVO = new DishVO();
        BeanUtils.copyProperties(dish,dishVO);
        dishVO.setFlavors(dishFlavors);

        return dishVO;
    }

    /**
     * 修改菜品及其口味
     * @param dishDTO
     */
    @Override
    public void updateWithFlavor(DishDTO dishDTO) {
        // 修改菜品表
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO,dish);
        dishMapper.update(dish);

        // 修改口味表，先删除原有的口味数据，然后重新插入口味数据
        dishFlavorMapper.deleteByDishId(dishDTO.getId());
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if(flavors != null && flavors.size() > 0){
            // dishId赋值
            flavors.forEach(dishFlavor -> {
                dishFlavor.setDishId(dishDTO.getId());
            });
            // 批量插入
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 起售停售菜品
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        Dish dish = Dish.builder().
                id(id).
                status(status).build();

        dishMapper.update(dish);
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @Override
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }

    /**
     * 根据id获取菜品
     * @param id
     * @return
     */
    @Override
    public Dish getById(Long id) {
        return dishMapper.getById(id);
    }

    /**
     * 获取菜品数量
     * @param map
     * @return
     */
    @Override
    public Integer countByMap(Map map) {
        return dishMapper.countByMap(map);
    }
}
