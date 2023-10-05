package com.sky.admin.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.admin.mapper.EmployeeMapper;
import com.sky.admin.service.EmployeeService;
import com.sky.common.constant.MessageConstant;
import com.sky.common.constant.PasswordConstant;
import com.sky.common.constant.StatusConstant;
import com.sky.common.context.BaseContext;
import com.sky.common.exception.AccountLockedException;
import com.sky.common.exception.AccountNotFoundException;
import com.sky.common.exception.PasswordErrorException;
import com.sky.common.result.PageResult;
import com.sky.model.admin.dto.EmployeeDTO;
import com.sky.model.admin.dto.EmployeeLoginDTO;
import com.sky.model.admin.dto.EmployeePageQueryDTO;
import com.sky.model.admin.entity.Employee;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class EmployeeServiceImpl implements EmployeeService {

    @Autowired
    private EmployeeMapper employeeMapper;

    /**
     * 员工登录
     *
     * @param employeeLoginDTO
     * @return
     */
    public Employee login(EmployeeLoginDTO employeeLoginDTO) {
        String username = employeeLoginDTO.getUsername();
        String password = employeeLoginDTO.getPassword();

        //1、根据用户名查询数据库中的数据
        Employee employee = employeeMapper.getByUsername(username);

        //2、处理各种异常情况（用户名不存在、密码不对、账号被锁定）
        if (employee == null) {
            //账号不存在
            throw new AccountNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        //密码比对
        // md5 加密处理
        password = DigestUtils.md5DigestAsHex(password.getBytes());
        if (!password.equals(employee.getPassword())) {
            //密码错误
            throw new PasswordErrorException(MessageConstant.PASSWORD_ERROR);
        }

        if (employee.getStatus() == StatusConstant.DISABLE) {
            //账号被锁定
            throw new AccountLockedException(MessageConstant.ACCOUNT_LOCKED);
        }

        //3、返回实体对象
        return employee;
    }

    /**
     * 新增员工
     * @param employeeDTO
     */
    @Override
    public void save(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();

        // 对象属性拷贝,前面拷到后面目标
        BeanUtils.copyProperties(employeeDTO,employee);

        // 设置账号的装填，1 正常 0 锁定  默认为 1 ，所以可以不设置
        employee.setStatus(StatusConstant.ENABLE);

        // 设置密码，默认密码123456 加密
        employee.setPassword(DigestUtils.md5DigestAsHex(PasswordConstant.DEFAULT_PASSWORD.getBytes()));

        // 统一赋值 去掉
//        //设置创建时间
//        employee.setCreateTime(LocalDateTime.now());
//
//        // 设置更新时间
//        employee.setUpdateTime(LocalDateTime.now());
//
//        // 设置当前记录创建人id和修改人id
//        employee.setCreateUser(BaseContext.getCurrentId());
//        employee.setUpdateUser(BaseContext.getCurrentId());

        // 写入数据库
        employeeMapper.insert(employee);
    }

    /**
     * 分页查询员工
     * @param employeePageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(EmployeePageQueryDTO employeePageQueryDTO) {
        // 分页查询  通过PageHelper框架
        PageHelper.startPage(employeePageQueryDTO.getPage(),employeePageQueryDTO.getPageSize());
        // PageHelper要求 必须返回Page对象
        Page<Employee> page = employeeMapper.pageQuery(employeePageQueryDTO);

        long total = page.getTotal();
        List<Employee> result = page.getResult();
        return new PageResult(total,result);
    }

    /**
     * 启用禁用员工账号
     * @param status
     * @param id
     */
    @Override
    public void startOrStop(Integer status, Long id) {
        // update employee set status = ? where id = ?
//        Employee employee = new Employee();
//        employee.setStatus(status);
//        employee.setId(id);

        // 另一种写法
        Employee employee = Employee.builder()
                .status(status)
                .id(id)
                .build();

        employeeMapper.updata(employee);
    }

    /**
     * 根据id查询员工
     * @param id
     * @return
     */
    @Override
    public Employee getById(Long id) {
        Employee employee = employeeMapper.getById(id);
        // 处理密码，不传输给前端
        employee.setPassword("****");
        return employee;
    }

    /**
     * 修改员工信息
     * @param employeeDTO
     */
    @Override
    public void update(EmployeeDTO employeeDTO) {
        Employee employee = new Employee();
        BeanUtils.copyProperties(employeeDTO,employee);
        employee.setUpdateUser(BaseContext.getCurrentId());
        employee.setUpdateTime(LocalDateTime.now());
        employeeMapper.updata(employee);
    }

}
