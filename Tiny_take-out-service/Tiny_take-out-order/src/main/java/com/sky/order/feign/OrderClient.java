package com.sky.order.feign;

import com.sky.apis.order.IOrderClient;
import com.sky.model.order.dto.GoodsSalesDTO;
import com.sky.order.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
public class OrderClient implements IOrderClient {
    @Autowired
    private OrderService orderService;

    /**
     * 获取指定时间的营业额
     * @param map
     * @return
     */
    @Override
    @PostMapping("/feign/order/sumTurnover")
    public Double sumByMap(@RequestBody Map map) {
        return orderService.sumByMap(map);
    }

    /**
     * 获取指定时间内的排名前10
     * @param beginTime
     * @param endTime
     * @return
     */
    @Override
    @GetMapping("/feign/order/top10")
    public List<GoodsSalesDTO> getSalesTop10(@RequestParam("beginTime")LocalDateTime beginTime, @RequestParam("endTime") LocalDateTime endTime) {
        return orderService.getSalesTop10(beginTime,endTime);
    }

    /**
     * 获取订单数量
     * @param map
     * @return
     */
    @Override
    @PostMapping("/feign/order/count")
    public Integer countByMap(@RequestBody Map map) {
        return orderService.countByMap(map);
    }
}
