package com.sky.order.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.apis.user.IAddressBookClient;
import com.sky.apis.user.IUserClient;
import com.sky.common.constant.MessageConstant;
import com.sky.common.context.BaseContext;
import com.sky.common.exception.AddressBookBusinessException;
import com.sky.common.exception.OrderBusinessException;
import com.sky.common.exception.ShoppingCartBusinessException;
import com.sky.common.result.PageResult;
import com.sky.common.utils.HttpClientUtil;
import com.sky.common.utils.WeChatPayUtil;
import com.sky.model.order.vo.OrderStatisticsVO;
import com.sky.order.websocket.WebSocketServer;
import com.sky.model.order.dto.*;
import com.sky.model.order.entity.OrderDetail;
import com.sky.model.order.entity.Orders;
import com.sky.model.order.vo.OrderPaymentVO;
import com.sky.model.order.vo.OrderSubmitVO;
import com.sky.model.order.vo.OrderVO;
import com.sky.model.user.entirty.AddressBook;
import com.sky.model.user.entirty.ShoppingCart;
import com.sky.model.user.entirty.User;
import com.sky.order.mapper.OrderDetailMapper;
import com.sky.order.mapper.OrderMapper;
import com.sky.order.mapper.ShoppingCartMapper;
import com.sky.order.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private IAddressBookClient addressBookClient;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private IUserClient userClient;

    @Autowired
    private WeChatPayUtil weChatPayUtil;

    @Autowired
    private WebSocketServer webSocketServer;

    @Value("${sky.shop.address}")
    private String shopAddress;

    @Value("${sky.baidu.ak}")
    private String ak;
    /**
     * 用户下单
     * @param ordersSubmitDTO
     * @return
     */
    @Override
    @Transactional
    public OrderSubmitVO submitOrder(OrdersSubmitDTO ordersSubmitDTO) {
        // 处理各种异常 （地址为空 ）（购物车数据为空）
        AddressBook addressBook = addressBookClient.getById(ordersSubmitDTO.getAddressBookId());
        if(addressBook == null){
            // 地址为空
            throw new AddressBookBusinessException(MessageConstant.ADDRESS_BOOK_IS_NULL);
        }

        // 检查位置信息
        checkOutOfRange(addressBook.getCityName() + addressBook.getDistrictName() + addressBook.getDetail());

        ShoppingCart shoppingCart = new ShoppingCart();
        shoppingCart.setUserId(BaseContext.getCurrentId());
        List<ShoppingCart> shoppingCartList = shoppingCartMapper.list(shoppingCart);
        if (shoppingCartList == null || shoppingCartList.size() == 0){
            // 购物车为空
            throw new ShoppingCartBusinessException(MessageConstant.SHOPPING_CART_IS_NULL);
        }

        // 向订单表中插入一条数据
        Orders orders = new Orders();
        BeanUtils.copyProperties(ordersSubmitDTO,orders);
        orders.setOrderTime(LocalDateTime.now());  // 下单时间
        orders.setPayStatus(Orders.UN_PAID);  // 支付状态
        orders.setStatus(Orders.PENDING_PAYMENT);  // 订单状态
        orders.setNumber(String.valueOf(System.currentTimeMillis()));  // 设置订单号 时间戳
        orders.setPhone(addressBook.getPhone());  // 手机号
        orders.setConsignee(addressBook.getConsignee());  // 收货人
        orders.setUserId(BaseContext.getCurrentId());
        orders.setAddress(addressBook.getDetail());
        orderMapper.insert(orders);

        // 向订单明细表插入n条数据
        List<OrderDetail> orderDetailList = new ArrayList<>();
        for (ShoppingCart cart : shoppingCartList) {
            OrderDetail orderDetail = new OrderDetail();
            BeanUtils.copyProperties(cart,orderDetail);
            orderDetail.setOrderId(orders.getId());
            orderDetailList.add(orderDetail);
        }
        orderDetailMapper.insertBatch(orderDetailList);

        // 清空购物车
        shoppingCartMapper.deleteByUserId(BaseContext.getCurrentId());

        // 封装VO
        OrderSubmitVO orderSubmitVO = OrderSubmitVO.builder()
                .id(orders.getId())
                .orderTime(orders.getOrderTime())
                .orderNumber(orders.getNumber())
                .orderAmount(orders.getAmount())
                .build();

        return orderSubmitVO;
    }

    /**
     * 订单支付
     *
     * @param ordersPaymentDTO
     * @return
     */
    public OrderPaymentVO payment(OrdersPaymentDTO ordersPaymentDTO) throws Exception {
        // 当前登录用户id
        Long userId = BaseContext.getCurrentId();
        User user = userClient.getById(userId);

        //调用微信支付接口，生成预支付交易单
//        JSONObject jsonObject = weChatPayUtil.pay(
//                ordersPaymentDTO.getOrderNumber(), //商户订单号
//                new BigDecimal(0.01), //支付金额，单位 元
//                "苍穹外卖订单", //商品描述
//                user.getOpenid() //微信用户的openid
//        );
        JSONObject jsonObject = new JSONObject();

        if (jsonObject.getString("code") != null && jsonObject.getString("code").equals("ORDERPAID")) {
            throw new OrderBusinessException("该订单已支付");
        }

        OrderPaymentVO vo = jsonObject.toJavaObject(OrderPaymentVO.class);
        vo.setPackageStr(jsonObject.getString("package"));

        // todo 跳过微信支付直接修改订单状态
        paySuccess(ordersPaymentDTO.getOrderNumber());
        return vo;
    }

    /**
     * 支付成功，修改订单状态
     *
     * @param outTradeNo
     */
    public void paySuccess(String outTradeNo) {

        // 根据订单号查询订单
        Orders ordersDB = orderMapper.getByNumber(outTradeNo);

        // 根据订单id更新订单的状态、支付方式、支付状态、结账时间
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.TO_BE_CONFIRMED)
                .payStatus(Orders.PAID)
                .checkoutTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);

        Map map = new HashMap();
        map.put("type",1);
        map.put("orderId",ordersDB.getId());
        map.put("content","订单号:"+outTradeNo);

        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }

    /**
     * 用户端分页查询
     * @param pageNum
     * @param pageSize
     * @param status
     * @return
     */
    @Override
    public PageResult pageQuery4User(int pageNum, int pageSize, Integer status) {
        // 分页查询
        PageHelper.startPage(pageNum,pageSize);
        OrdersPageQueryDTO ordersPageQueryDTO = new OrdersPageQueryDTO();
        ordersPageQueryDTO.setUserId(BaseContext.getCurrentId());
        ordersPageQueryDTO.setStatus(status);

        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        // 构建返回数据
        List<OrderVO> list = new ArrayList<>();
        if(page != null && page.getTotal() > 0){
            for (Orders orders : page) {
                Long ordersId = orders.getId();

                // 查询订单明细
                List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(ordersId);
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                orderVO.setOrderDetailList(orderDetails);

                list.add(orderVO);
            }
        }

        return new PageResult(page.getTotal(),list);
    }

    /**
     * 根据订单id获取订单详情
     * @param id
     * @return
     */
    @Override
    public OrderVO details(Long id) {
        Orders orders = orderMapper.getById(id);
        List<OrderDetail> orderDetails = orderDetailMapper.getByOrderId(id);

        OrderVO orderVO = new OrderVO();
        BeanUtils.copyProperties(orders,orderVO);
        orderVO.setOrderDetailList(orderDetails);

        return orderVO;
    }

    /**
     * 用户根据订单删除订单
     * @param id
     * @throws Exception
     */
    @Override
    public void usercancelById(Long id) throws Exception {
        // 查询订单是否存在
        Orders ordersDB = orderMapper.getById(id);
        if(ordersDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 查看订单状态,1待付款 2待接单 3已接单 4派送中 5已完成 6已取消
        if(ordersDB.getStatus() > 2){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 订单处于等待接单状态，退款
        Orders orders = new Orders();
        orders.setId(ordersDB.getId());
        if(ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            //调用微信支付退款接口
//            weChatPayUtil.refund(
//                    ordersDB.getNumber(), //商户订单号
//                    ordersDB.getNumber(), //商户退款单号
//                    new BigDecimal(0.01),//退款金额，单位 元
//                    new BigDecimal(0.01));//原订单金额

            // 支付状态修改为 退款
            orders.setPayStatus(Orders.REFUND);
        }

        // 更新订单状态
        orders.setStatus(Orders.CANCELLED);
        orders.setCancelReason("用户取消");
        orders.setCancelTime(LocalDateTime.now());

        orderMapper.update(orders);
    }

    /**
     * 再来一单
     * @param id
     */
    @Override
    public void repetition(Long id) {
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(id);
        List<ShoppingCart> shoppingCartList = orderDetailList.stream().map(x -> {
            ShoppingCart shoppingCart = new ShoppingCart();
            BeanUtils.copyProperties(x, shoppingCart);
            shoppingCart.setUserId(BaseContext.getCurrentId());
            shoppingCart.setCreateTime(LocalDateTime.now());
            return shoppingCart;
        }).collect(Collectors.toList());

        shoppingCartMapper.insertBatch(shoppingCartList);
    }

    /**
     * 订单搜索
     * @param ordersPageQueryDTO
     * @return
     */
    @Override
    public PageResult conditionSearch(OrdersPageQueryDTO ordersPageQueryDTO) {
        PageHelper.startPage(ordersPageQueryDTO.getPage(),ordersPageQueryDTO.getPageSize());
        Page<Orders> page = orderMapper.pageQuery(ordersPageQueryDTO);

        List<OrderVO> orderVoList = getOrderVoList(page);
        return new PageResult(page.getTotal(),orderVoList);
    }

    /**
     * 统计各个状态订单 待派单 派送中 待接单
     * @return
     */
    @Override
    public OrderStatisticsVO statistics() {
        Integer toBeConfirmed = orderMapper.countStatus(Orders.TO_BE_CONFIRMED);
        Integer confirmed = orderMapper.countStatus(Orders.CONFIRMED);
        Integer deleveryInProgress = orderMapper.countStatus(Orders.DELIVERY_IN_PROGRESS);

        OrderStatisticsVO orderStatisticsVO = new OrderStatisticsVO();
        orderStatisticsVO.setToBeConfirmed(toBeConfirmed);
        orderStatisticsVO.setConfirmed(confirmed);
        orderStatisticsVO.setDeliveryInProgress(deleveryInProgress);
        return orderStatisticsVO;
    }

    @Override
    public void confirm(OrdersConfirmDTO ordersConfirmDTO) {
        Orders orders = Orders.builder()
                .id(ordersConfirmDTO.getId())
                .status(Orders.CONFIRMED)
                .build();
        orderMapper.update(orders);
    }

    /**
     * 拒单
     * @param ordersRejectionDTO
     */
    @Override
    public void reject(OrdersRejectionDTO ordersRejectionDTO) {
        // 查询订单状态
        Orders ordersDB = orderMapper.getById(ordersRejectionDTO.getId());

        // 只有待接单状态才可以取消
        if(ordersDB == null || !ordersDB.getStatus().equals(Orders.TO_BE_CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        // 如果支付状态为已付款
        if(ordersDB.getPayStatus() == Orders.PAID){
            //用户已支付，需要退款
//            String refund = weChatPayUtil.refund(
//                    ordersDB.getNumber(),
//                    ordersDB.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
//            log.info("申请退款：{}", refund);
            log.info("退款成功");
        }

        // 修改订单状态
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .payStatus(Orders.REFUND)
                .status(Orders.CANCELLED)
                .rejectionReason(ordersRejectionDTO.getRejectionReason())
                .cancelTime(LocalDateTime.now())
                .build();
        orderMapper.update(orders);
    }

    /**
     * 商家取消订单
     * @param ordersCancelDTO
     */
    @Override
    public void cancel(OrdersCancelDTO ordersCancelDTO) {
        Orders ordersDB = orderMapper.getById(ordersCancelDTO.getId());

        if(ordersDB == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        // 已支付状态，退款
        if(ordersDB.getPayStatus() == Orders.PAID){
            //用户已支付，需要退款
//            String refund = weChatPayUtil.refund(
//                    ordersDB.getNumber(),
//                    ordersDB.getNumber(),
//                    new BigDecimal(0.01),
//                    new BigDecimal(0.01));
//            log.info("申请退款：{}", refund);
            log.info("退款成功");
        }

        // 更新订单状态
        Orders orders = Orders.builder()
                .id(ordersDB.getId())
                .status(Orders.CANCELLED)
                .payStatus(Orders.REFUND)
                .cancelReason(ordersCancelDTO.getCancelReason())
                .cancelTime(LocalDateTime.now())
                .build();

        orderMapper.update(orders);
    }

    /**
     * 派送订单
     * @param id
     */
    @Override
    public void delivery(Long id) {
        Orders ordersDB = orderMapper.getById(id);

        if(ordersDB == null || !ordersDB.getStatus().equals(Orders.CONFIRMED)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.DELIVERY_IN_PROGRESS)
                .build();

        orderMapper.update(orders);
    }

    /**
     * 完成订单
     * @param id
     */
    @Override
    public void complete(Long id) {
        Orders ordersDB = orderMapper.getById(id);

        if(ordersDB == null || !ordersDB.getStatus().equals(Orders.DELIVERY_IN_PROGRESS)){
            throw new OrderBusinessException(MessageConstant.ORDER_STATUS_ERROR);
        }

        Orders orders = Orders.builder()
                .id(id)
                .status(Orders.COMPLETED)
                .build();

        orderMapper.update(orders);
    }

    /**
     * 客户催单
     * @param id
     */
    @Override
    public void reminder(Long id) {
        Orders orders = orderMapper.getById(id);

        // 校验订单
        if(orders == null){
            throw new OrderBusinessException(MessageConstant.ORDER_NOT_FOUND);
        }

        Map map = new HashMap();
        map.put("type",2);
        map.put("orderId",orders.getId());
        map.put("content","订单号:"+orders.getNumber());

        String jsonString = JSON.toJSONString(map);
        webSocketServer.sendToAllClient(jsonString);
    }

    /**
     * 获取指定时间营业额
     * @param map
     * @return
     */
    @Override
    public Double sumByMap(Map map) {
        return orderMapper.sumByMap(map);
    }

    /**
     * 获取指定时间内的排名前10
     * @param beginTime
     * @param endTime
     * @return
     */
    @Override
    public List<GoodsSalesDTO> getSalesTop10(LocalDateTime beginTime, LocalDateTime endTime) {
        return orderMapper.getSalesTop10(beginTime,endTime);
    }

    /**
     * 获取订单数量
     * @param map
     * @return
     */
    @Override
    public Integer countByMap(Map map) {
        return orderMapper.countByMap(map);
    }

    /**
     * 封装Vo，包含订单菜品信息，字符串形式
     * @param page
     * @return
     */
    private List<OrderVO> getOrderVoList(Page<Orders> page) {
        List<OrderVO> orderVOList = new ArrayList<>();

        List<Orders> ordersList = page.getResult();
        if(!CollectionUtils.isEmpty(ordersList)){
            for (Orders orders : ordersList) {
                OrderVO orderVO = new OrderVO();
                BeanUtils.copyProperties(orders,orderVO);
                // 获取订单菜品字符串
                String orderDishes = getOrderDishesStr(orders);

                orderVO.setOrderDishes(orderDishes);
                orderVOList.add(orderVO);
            }
        }

        return orderVOList;
    }

    /**
     * 根据订单id获取菜品信息字符串
     * @param orders
     * @return
     */
    private String getOrderDishesStr(Orders orders) {
        List<OrderDetail> orderDetailList = orderDetailMapper.getByOrderId(orders.getId());
        List<String> orderDishList = orderDetailList.stream().map(x -> {
            String orderDish = x.getName() + "*" + x.getNumber() + ";";
            return orderDish;
        }).collect(Collectors.toList());

        return String.join("",orderDishList);
    }

    /**
     * 检查客户的收货地址是否超出范围
     * 获取位置编码：https://lbsyun.baidu.com/faq/api?title=webapi/guide/webservice-geocoding-base
     * 路线规划：https://lbsyun.baidu.com/faq/api?title=webapi/guide/webservice-lwrouteplanapi/dirve
     * @param address
     */
    private void checkOutOfRange(String address){
        // 商家位置编码
        Map map = new HashMap<>();
        map.put("address",shopAddress);
        map.put("output","json");
        map.put("ak",ak);
        String shopCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);
        JSONObject jsonObject = JSON.parseObject(shopCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("店铺地址解析失败");
        }
        JSONObject location = jsonObject.getJSONObject("result").getJSONObject("location");
        // 获取经纬度
        String lat = location.getString("lat");
        String lng = location.getString("lng");
        // 店铺坐标
        String shopLngLat = lat + "," + lng;

        // 获取用户的位置编码
        map.put("address",address);
        String userCoordinate = HttpClientUtil.doGet("https://api.map.baidu.com/geocoding/v3", map);
        jsonObject = JSON.parseObject(userCoordinate);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("店铺地址解析失败");
        }
        location = jsonObject.getJSONObject("result").getJSONObject("location");
        // 获取经纬度
        lat = location.getString("lat");
        lng = location.getString("lng");
        // 店铺坐标
        String userLngLat = lat + "," + lng;

        // 路线规划
        map.put("origin",shopLngLat);
        map.put("destination",userLngLat);
        map.put("steps_info","0");

        String json = HttpClientUtil.doGet("https://api.map.baidu.com/directionlite/v1/driving", map);
        jsonObject = JSON.parseObject(json);
        if(!jsonObject.getString("status").equals("0")){
            throw new OrderBusinessException("路线规划失败");
        }

        JSONObject result = jsonObject.getJSONObject("result");
        JSONArray jsonArray = (JSONArray) result.get("routes");
        Integer distance = (Integer) ((JSONObject) jsonArray.get(0)).get("distance");

        if(distance > 5000){
            //配送距离超过5000米
            throw new OrderBusinessException("超出配送范围");
        }
    }
}
