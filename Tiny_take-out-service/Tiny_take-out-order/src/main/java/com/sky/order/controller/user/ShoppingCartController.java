package com.sky.order.controller.user;



import com.sky.common.result.Result;
import com.sky.model.user.dto.ShoppingCartDTO;
import com.sky.model.user.entirty.ShoppingCart;
import com.sky.order.service.ShoppingCartService;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user/shoppingCart")
@Slf4j
@ApiOperation("C端购物车接口")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * 添加购物车
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/add")
    @ApiOperation("添加购物车")
    public Result add(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("添加购物车，信息为：{}",shoppingCartDTO);
        shoppingCartService.addShoppingCart(shoppingCartDTO);
        return Result.success();
    }

    /**
     * 查看购物车
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("查询购物车信息接口")
    private Result<List<ShoppingCart>> list(){
        List<ShoppingCart> list = shoppingCartService.showShoppingCart();
        return Result.success(list);
    }

    /**
     * 清空购物车
     * @return
     */
    @DeleteMapping("/clean")
    @ApiOperation("清空购物车接口")
    public Result clean(){
        shoppingCartService.cleanShoppingCart();
        return Result.success();
    }

    /**
     * 删除购物车
     * @param shoppingCartDTO
     * @return
     */
    @PostMapping("/sub")
    @ApiOperation("删除购物车中一个商品接口")
    public Result delete(@RequestBody ShoppingCartDTO shoppingCartDTO){
        log.info("删除菜品，信息为：{}",shoppingCartDTO);
        shoppingCartService.delete(shoppingCartDTO);
        return Result.success();
    }
}
