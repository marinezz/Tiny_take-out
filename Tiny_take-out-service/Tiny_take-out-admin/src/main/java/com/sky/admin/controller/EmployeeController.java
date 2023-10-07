package com.sky.admin.controller;

import com.sky.admin.service.EmployeeService;
import com.sky.common.constant.JwtClaimsConstant;
import com.sky.common.properties.JwtProperties;
import com.sky.common.result.PageResult;
import com.sky.common.result.Result;
import com.sky.common.utils.JwtUtil;
import com.sky.model.admin.entity.Employee;
import com.sky.model.admin.dto.EmployeeDTO;
import com.sky.model.admin.dto.EmployeeLoginDTO;
import com.sky.model.admin.dto.EmployeePageQueryDTO;
import com.sky.model.admin.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工相关接口")
public class EmployeeController {
    /**
     * spring默认扫描该包及其子包中的类
     * 比如这里就是在com.sky.admin   JwtProperties不在这里面，所以不能自动注入
     * 两种解决方式：
     * 1 自动配置扫描路径为@ComponentScan(basePackages = "com.sky")
     *   这样就可以扫描到，即使不在同一个模块中
     * 2 将需要扫描的类加入到spring.factory中，会自动配置扫描并注册
     */
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 登录
     *
     * @param employeeLoginDTO
     * @return
     */
    @PostMapping("/login")
    @ApiOperation("员工登录")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    /**
     * 退出
     *
     * @return
     */
    @PostMapping("/logout")
    @ApiOperation("员工退出")
    public Result<String> logout() {
        return Result.success();
    }

    /**
     * 新增员工
     * @param employeeDTO
     * @return
     */
    @PostMapping
    @ApiOperation("新增员工")
    public Result save(@RequestBody EmployeeDTO employeeDTO){
        log.info("新增员工：{}",employeeDTO);
        employeeService.save(employeeDTO);

        return Result.success();
    }

    /**
     * 员工分页查询
     * @param employeePageQueryDTO
     * @return
     */
    @GetMapping("/page")
    @ApiOperation("员工分页查询")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO){
        log.info("员工分页查询，参数为：{}",employeePageQueryDTO);
        PageResult pageResult = employeeService.pageQuery(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    /**
     * 启用禁用员工账号
     * status是路径参数，需要加入注解@PathVariable
     * @param status
     * @param id
     * @return
     */
    @PostMapping("/status/{status}")
    @ApiOperation("启用禁用员工账号")
    public Result startOrStop(@PathVariable Integer status,Long id){
        log.info("启用禁用员工账号：{},{}",status,id);
        employeeService.startOrStop(status,id);
        return Result.success();
    }

    /**
     * 根据Id查询员工
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("根据ID查询员工")
    public Result<Employee> getById(@PathVariable Long id){
        log.info("修改员工Id：{}",id);
        Employee employee = employeeService.getById(id);
        return Result.success(employee);
    }

    /**
     * 修改员工信息
     * @param employeeDTO
     * @return
     */
    @PutMapping
    @ApiOperation("修改员工信息")
    public Result update(@RequestBody EmployeeDTO employeeDTO){
        log.info("修改员工信息：{}",employeeDTO);
        employeeService.update(employeeDTO);
        return Result.success();
    }
}
