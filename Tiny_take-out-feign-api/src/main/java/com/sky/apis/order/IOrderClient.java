package com.sky.apis.order;

import com.sky.model.order.dto.GoodsSalesDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@FeignClient(name = "take-out-order",path="/feign")
public interface IOrderClient {
    /**
     * 获取指定时间内的营业额
     * @param map
     * @return
     */
    @PostMapping("/order/sumTurnover")
    Double sumByMap(@RequestBody Map map);

    /**
     * 获取指定时间的排名前10
     * @param beginTime
     * @param endTime
     * @return
     */
    @GetMapping("/order/top10")
    List<GoodsSalesDTO> getSalesTop10(@RequestParam("beginTime")LocalDateTime beginTime,@RequestParam("endTime") LocalDateTime endTime);

    /**
     * 获取订单数量
     * @param map
     * @return
     */
    @PostMapping("/order/count")
    Integer countByMap(@RequestBody Map map);


}
