# TradeSim - 投资交易模拟器

## 项目简介
TradeSim是一个投资交易模拟平台，提供股票、加密货币等多种投资品种的模拟交易，帮助用户学习投资知识。

## 技术栈
- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security + JWT
- Spring Scheduler (行情模拟)
- H2 Database
- Maven

## 快速启动
1. `cd tradesim`
2. `mvn clean install`
3. `mvn spring-boot:run`
4. 访问 http://localhost:8082/swagger-ui.html

## 主要功能
1. 用户管理：注册、登录、初始虚拟资金
2. 市场行情：实时行情模拟、K线数据
3. 交易功能：买入、卖出、市价单、限价单
4. 持仓管理：持仓查询、盈亏统计
5. 投资组合：多组合管理、收益分析
6. 数据分析：收益曲线、风险指标
7. 策略回测：历史数据回测

## 初始数据
- 新用户初始资金：1,000,000元
- 预置证券：AAPL, TSLA, GOOGL, MSFT, AMZN, BTC, ETH, BNB
- 行情更新：每60秒自动更新

## 注意
这是一个演示项目，所有数据均为模拟，不代表真实市场。
