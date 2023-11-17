<h1 align = "center">简易</h1>

# 1 项目介绍

外卖

## 1.1 目录介绍





# 2 技术总结

### 2.1 接口测试工具 -- Swagger



### 2.2 缓存 -- Redis



### 2.3 代码实现HTTP请求 -- HttpClient



### 2.4 定时任务 -- Spring Task



### 2.5 网络协议 -- WebSocket



### 2.6 可视化图标库 -- Apache ECharts



### 2.7 处理Office -- Apache POI



# 3 改进

## 2.1 微服务架构

user（地址，购物车）   -- 管理员    菜品     订单服务（不需要两边登录就可以完成的接口，其余都是远程调用）     定时任务服务 

比如下单 就是user 远程调用订单 

查看订单服务 就是直接调用订单服务



这里将整个项目分为了五个微服务（后续可能改动），分别为用户服务、管理员服务、菜品服务、订单服务、任务调度服务。

* 用户服务：C端有关的接口，包括用户登录、用户地址、用户订单等
* 管理员服务：员工相关服务，包括员工管理、店铺状态等
* 菜品服务：菜品、套餐相关服务，包括分类、菜品、套餐、口味等
* 订单服务：订单相关服务，订单的相关逻辑
* 任务调度服务：订单相关服务

### 2.1.1 目录结构改变

**原本的结构：**

```bash
 ├─ take-out       //maven父工程，统一管理依赖版本，聚合其他子模块
    ├─sky-common   // 子模块，存放工具类
    ├─sky-pojo     // 子模块，存放实体类
    └─sky-server   // 子模块，后端服务，存放配置文件
```

**微服务结构：**

对比改造前的结构，多了**网关模块**和**feign对外接口模块**

```bash
├─ take-out 						//maven父工程，统一管理依赖版本，聚合其他子模块
    ├─Tiny_take-out-common          // 子模块，存放工具类
    ├─Tiny_take-out-feign-api		// 子模块，feign对外接口
    ├─Tiny_take-out-getway			// 子模块，管理网关工程
    ├─Tiny_take-out-model     		// 子模块，存放实体类
    └─Tiny_take-out-service			// 子模块，后端服务
```

**后端服务：**

对于`Tiny_take-out-service`下存放五个微服务模块

```bash
├─Tiny_take-out-admin		// 管理员服务
├─Tiny_take-out-dish		// 菜品服务
├─Tiny_take-out-order		// 订单服务
├─Tiny_take-out-schedule	// 任务调度服务
└─Tiny_take-out-user		// 用户服务
```





## 2.2 优化缓存

优化：   销量排名数据 放入缓存中

## 2.3 优化逻辑



外卖项目定时任务逻辑  每次都是全表查询。

## 2.4 集成新的技术

## 2.5 项目测试



## 2.6 项目上线





# 

1 分库分表

1-1 水平分表，把一个表水平分成多个表，把经常查的放在一个表上，没那么经常差的放在一个表上。（比如：把文章表的基本信息放在第一个表上（经常读），把文章内容放在一个表上（大文本），也可以是多个表）

![image-20230926095609190](D:\Project\java_project\sky_take-out\README.assets\image-20230926095609190.png)

2 主键改为雪花算法 - - 分布式ID

3 目录结构

4 单元测试，在接口上ctrl + shift + T 自动创建测试

5 feign服务降级

6 异步调用  （文章审核） Springboot集成异步线程调用

![image-20231114192913407](D:\Project\java_project\sky_take-out\README.assets\image-20231114192913407.png)

7 DFA有穷自动机 --- 敏感词检测，   ---- 用不到 ==

8 集成seata解决分布式事务 （各个微服务下操作数据库）-- 课程没有讲

9 延时队列  定时上架  菜品   定时发布文章（审核应该什么时候）  小计 没有想清楚

10 

![image-20230927095506116](D:\Project\java_project\sky_take-out\README.assets\image-20230927095506116.png)

11 定时任务改成   --   延时任务（思考一下有必要没有）

dalayQueue   消息队列   使用redis的zset  

12  使用redis 延时任务  mysql持久化  zset 和 list结合使用    还有版本号（运用到了乐观锁）

13 分布式锁（只有一台设备使用了分布式锁）

14 kafka   消息队列   --- 上下架 （解耦服务），比如用管理端要远程调用菜品去下架菜品，这时候，可以用feign远程调用，也可以kafka解耦服务

15 菜品的搜索功能 吗，搜索的历史记录，联想词语查询
