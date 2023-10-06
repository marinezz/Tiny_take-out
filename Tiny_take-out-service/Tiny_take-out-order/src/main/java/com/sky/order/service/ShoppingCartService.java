package com.sky.order.service;



import com.sky.model.user.dto.ShoppingCartDTO;
import com.sky.model.user.entirty.ShoppingCart;

import java.util.List;

public interface ShoppingCartService {
    /**
     * 添加购物车
      * @param shoppingCartDTO
     */
    void addShoppingCart(ShoppingCartDTO shoppingCartDTO);

    /**
     * 查询全部购物车
     * @return
     */
    List<ShoppingCart> showShoppingCart();

    /**
     * 清空购物车
     */
    void cleanShoppingCart();

    /**
     * 删除购物车中的一个商品
     * @param shoppingCartDTO
     */
    void delete(ShoppingCartDTO shoppingCartDTO);
}
