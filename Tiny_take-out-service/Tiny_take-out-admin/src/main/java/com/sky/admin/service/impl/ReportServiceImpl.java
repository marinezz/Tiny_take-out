package com.sky.admin.service.impl;

import com.sky.admin.service.ReportService;
import com.sky.admin.service.WorkspaceService;
import com.sky.apis.order.IOrderClient;
import com.sky.apis.user.IUserClient;
import com.sky.model.admin.vo.*;
import com.sky.model.order.dto.GoodsSalesDTO;
import com.sky.model.order.entity.Orders;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
public class ReportServiceImpl implements ReportService {

    @Autowired
    private IOrderClient orderClient;

    @Autowired
    private IUserClient userClient;

    @Autowired
    private WorkspaceService workspaceService;
    /**
     * 统计指定时间内的营业额数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        // 存放begin到end范围的所有时间
        List<LocalDate> dateList = getDateString(begin,end);
        String dates = StringUtils.join(dateList,",");

        // 查询营业额
        List<Double> turnoverList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            // 获取一天的开始和结束时间
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Map map = new HashMap<>();
            map.put("begin",beginTime);
            map.put("end",endTime);
            map.put("status", Orders.COMPLETED);
            Double turnover = orderClient.sumByMap(map);
            // 没有数据的时候为空，因转换为0.0
            turnover = turnover == null?0.0:turnover;
            turnoverList.add(turnover);
        }


        return TurnoverReportVO
                .builder()
                .dateList(dates)
                .turnoverList(StringUtils.join(turnoverList,","))
                .build();
    }

    /**
     * 统计指定时间内的用户数据
     * 用户总量
     * 新增用户数量
     * @param begin
     * @param end
     * @return
     */
    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        // 存放begin到end范围的所有时间
        List<LocalDate> dateList = getDateString(begin,end);

        // 统计每天新增用户和总用户（截止当前的总用户）
        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            // 获取一天的开始和结束时间
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
            Map map = new HashMap<>();
            map.put("end",endTime);
            Integer totalUser = userClient.countByMap(map);
            totalUserList.add(totalUser);
            map.put("begin",beginTime);
            Integer newUser = userClient.countByMap(map);
            newUserList.add(newUser);
        }

        return UserReportVO
                .builder()
                .dateList(StringUtils.join(dateList,","))
                .newUserList(StringUtils.join(newUserList,","))
                .totalUserList(StringUtils.join(totalUserList,","))
                .build();
    }

    /**
     * 统计指定时间的订单数据
     * @param begin
     * @param end
     * @return
     */
    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        // 存放begin到end范围的所有时间
        List<LocalDate> dateList = getDateString(begin,end);

        // 查询每天的有效订单数和订单总数
        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            // 获取一天的开始和结束时间
            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);

            Integer toatalOrderCount = getOrderCount(beginTime, endTime, null);
            Integer validOrderCount = getOrderCount(beginTime, endTime, Orders.COMPLETED);

            orderCountList.add(toatalOrderCount);
            validOrderList.add(validOrderCount);
        }

        // 计算时间区间内的订单总数
        Integer totalOrderCount = orderCountList.stream().reduce(Integer::sum).get();
        Integer totalValidCount = validOrderList.stream().reduce(Integer::sum).get();

        Double orderCompletionRate = 0.0;
        if(totalOrderCount != 0){
            orderCompletionRate = totalValidCount.doubleValue()/totalOrderCount;
        }
        return OrderReportVO
                .builder()
                .dateList(StringUtils.join(dateList,","))
                .orderCountList(StringUtils.join(orderCountList,","))
                .validOrderCountList(StringUtils.join(validOrderList,","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(totalValidCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    /**
     *统计指定时间内的销量排名top10
     * // select od.name,sum(od.number) number from order_detail od,orders o where od.order_id = o.id
     *    and o.status = 5 and o.order_time > 'begin' and o.order_time < 'end'
     *    group by od.name order number desc limit 10
     * @param begin
     * @param end
     * @return
     */
    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<GoodsSalesDTO> goodsSalesDTOList = orderClient.getSalesTop10(beginTime,endTime);

        List<String> names = goodsSalesDTOList.stream().map(GoodsSalesDTO::getName).collect(Collectors.toList());
        List<Integer> numbers = goodsSalesDTOList.stream().map(GoodsSalesDTO::getNumber).collect(Collectors.toList());

        return SalesTop10ReportVO
                .builder()
                .nameList(StringUtils.join(names,","))
                .numberList(StringUtils.join(numbers,","))
                .build();
    }

    /**
     * 导出运行数据报表
     * @param response
     */
    @Override
    public void exportBusinessDate(HttpServletResponse response) {

        // 1 查询数据库，获取营业数据（近30天）

        // 1.1 查询概览数据
        LocalDate dateBegin = LocalDate.now().minusDays(30);
        LocalDate dateEnd = LocalDate.now().minusDays(1);
        BusinessDataVO businessDataVO = workspaceService.getBusinessData(LocalDateTime.of(dateBegin, LocalTime.MIN), LocalDateTime.of(dateEnd, LocalTime.MAX));


        // 2 通过POI将数据写入到excel文件中
        // 基于模板文件创建excel
        InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream("template/运营数据报表模板.xlsx");
        try {
            XSSFWorkbook excel = new XSSFWorkbook(inputStream);
            // 填充数据
            XSSFSheet sheet = excel.getSheet("Sheet1");
            sheet.getRow(1).getCell(1).setCellValue("时间："+dateBegin+"-"+dateEnd);

            XSSFRow row = sheet.getRow(3);
            row.getCell(2).setCellValue(businessDataVO.getTurnover());
            row.getCell(4).setCellValue(businessDataVO.getOrderCompletionRate());
            row.getCell(6).setCellValue(businessDataVO.getNewUsers());
            row = sheet.getRow(4);
            row.getCell(2).setCellValue(businessDataVO.getValidOrderCount());
            row.getCell(4).setCellValue(businessDataVO.getUnitPrice());

            // 1.2 明细数据
            for(int i = 0; i < 30; i++){
                LocalDate date = dateBegin.plusDays(i);
                BusinessDataVO businessData = workspaceService.getBusinessData(LocalDateTime.of(date, LocalTime.MIN), LocalDateTime.of(date, LocalTime.MAX));

                // 2.1 填充数据
                row = sheet.getRow(7 + i);
                row.getCell(1).setCellValue(date.toString());
                row.getCell(2).setCellValue(businessData.getTurnover());
                row.getCell(3).setCellValue(businessData.getValidOrderCount());
                row.getCell(4).setCellValue(businessData.getOrderCompletionRate());
                row.getCell(5).setCellValue(businessData.getUnitPrice());
                row.getCell(6).setCellValue(businessData.getNewUsers());
            }

            // 3 通过输出流，将文件下载到客户端浏览器
            ServletOutputStream outputStream = response.getOutputStream();
            excel.write(outputStream);

            // 关闭资源
            outputStream.flush();
            outputStream.close();
            excel.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * 获取时间范围字符串
     * @param begin
     * @param end
     * @return
     */
    public List<LocalDate>getDateString(LocalDate begin, LocalDate end){
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while(!begin.equals(end)){
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }

    /**
     * 动态获取订单数量
     * @param begin
     * @param end
     * @param status
     * @return
     */
    public Integer getOrderCount(LocalDateTime begin,LocalDateTime end, Integer status){
        Map map = new HashMap<>();
        map.put("end",end);
        map.put("begin",begin);
        map.put("status",status);

        return orderClient.countByMap(map);
    }
}
