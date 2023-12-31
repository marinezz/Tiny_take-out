package com.sky.dish.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;

import com.sky.common.constant.MessageConstant;
import com.sky.common.constant.StatusConstant;
import com.sky.common.exception.DeletionNotAllowedException;
import com.sky.common.exception.SetmealEnableFailedException;
import com.sky.common.result.PageResult;
import com.sky.dish.mapper.DishMapper;
import com.sky.dish.mapper.SetmealDishMapper;
import com.sky.dish.mapper.SetmealMapper;
import com.sky.dish.service.SetmealService;
import com.sky.model.dish.dto.SetmealDTO;
import com.sky.model.dish.dto.SetmealPageQueryDTO;
import com.sky.model.dish.entity.Dish;
import com.sky.model.dish.entity.Setmeal;
import com.sky.model.dish.entity.SetmealDish;
import com.sky.model.dish.vo.DishItemVO;
import com.sky.model.dish.vo.SetmealVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
public class SetmealServiceImpl implements SetmealService {
    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private SetmealDishMapper setmealDishMapper;

    @Autowired
    private DishMapper dishMapper;

    /**
     * 新增套餐
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void save(SetmealDTO setmealDTO) {
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        // 新增套餐
        setmealMapper.insert(setmeal);

        // 获取套餐关系
        Long setmealId = setmeal.getId();

        // 套餐菜品关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealId);
        });

        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 分页查询菜品
     * @param setmealPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(SetmealPageQueryDTO setmealPageQueryDTO) {
        PageHelper.startPage(setmealPageQueryDTO.getPage(),setmealPageQueryDTO.getPageSize());
        Page<SetmealVO> page = setmealMapper.pageQuery(setmealPageQueryDTO);
        long total = page.getTotal();
        return new PageResult(total,page);
    }

    /**
     * 套餐的批量删除
     * @param ids
     */
    @Override
    @Transactional
    public void deleteBatch(List<Long> ids) {
        // 判断套餐能否被删除，起售中不能删除
        for (Long id : ids) {
            Setmeal setmeal = setmealMapper.getById(id);
            if(setmeal.getStatus() == StatusConstant.ENABLE){
                throw new DeletionNotAllowedException(MessageConstant.SETMEAL_ON_SALE);
            }
        }

        // 批量删除套餐
        setmealMapper.deletebyIds(ids);

        // 批量删除 套餐 菜品 关系
        setmealDishMapper.deleteBySetmealIds(ids);
    }

    /**
     * 修改套餐信息
     * @param setmealDTO
     */
    @Override
    @Transactional
    public void updateWithDish(SetmealDTO setmealDTO) {
        // 修改套餐信息
        Setmeal setmeal = new Setmeal();
        BeanUtils.copyProperties(setmealDTO,setmeal);
        setmealMapper.update(setmeal);

        // 删除原始的套餐 菜品 关系
        setmealDishMapper.deleteBySetmealId(setmealDTO.getId());

        // 新增套餐 菜品 关系
        List<SetmealDish> setmealDishes = setmealDTO.getSetmealDishes();
        setmealDishes.forEach(setmealDish -> {
            setmealDish.setSetmealId(setmealDTO.getId());
        });

        setmealDishMapper.insertBatch(setmealDishes);
    }

    /**
     * 根据菜品id查询套餐以及菜品
     * @param id
     * @return
     */
    @Override
    public SetmealVO getByIdWithDish(Long id) {
        // 查询套餐
        Setmeal setmeal = setmealMapper.getById(id);

        // 查询菜品
        List<SetmealDish> dishList = setmealDishMapper.getBysetmealId(id);

        SetmealVO setmealVO = new SetmealVO();
        BeanUtils.copyProperties(setmeal,setmealVO);
        setmealVO.setSetmealDishes(dishList);

        return setmealVO;
    }

    /**
     * 起售停售套餐
     * @param status,id
     */
    @Override
    public void startOrStop(Integer status,Long id) {
        // 起售套餐得时候，判断套餐中没有停售得菜品才能成功
        if(status == StatusConstant.ENABLE){
            List<Dish> dishList = dishMapper.getBySetmealId(id);
            if(dishList != null && dishList.size() != 0){
                dishList.forEach(dish -> {
                    if(dish.getStatus() == StatusConstant.DISABLE){
                        throw new SetmealEnableFailedException(MessageConstant.SETMEAL_ENABLE_FAILED);
                    }
                });
            }
        }
        Setmeal setmeal = Setmeal.builder()
                .id(id)
                .status(status)
                .build();

        setmealMapper.update(setmeal);
    }

    /**
     * 条件查询
     * @param setmeal
     * @return
     */
    public List<Setmeal> list(Setmeal setmeal) {
        List<Setmeal> list = setmealMapper.list(setmeal);
        return list;
    }

    /**
     * 根据id查询菜品选项
     * @param id
     * @return
     */
    public List<DishItemVO> getDishItemById(Long id) {
        return setmealMapper.getDishItemBySetmealId(id);
    }

    /**
     * 根据id获取套餐
     * @param id
     * @return
     */
    @Override
    public Setmeal getById(Long id) {
        return setmealMapper.getById(id);
    }

    /**
     * 获取套餐数量
     * @param map
     * @return
     */
    @Override
    public Integer countByMap(Map map) {
        return setmealMapper.countByMap(map);
    }
}
