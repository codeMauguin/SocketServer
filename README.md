# 目录

* [WebServer-Socket](#webserver-socket)
  * [socket模式](#socket模式)
  * [API](#api)
  * [任务清单](#任务清单)
  * [问题](#问题)
  * [最近添加](#最近添加)

# WebServer-Socket

## socket模式

- [x] BIO
- [x] NIO

## API

- `Servlet`
- `WebHttpServerFactory`
- `HttpRequest`
- `HttpResponse`
- `WebServer`
- `Filter`
- `Controller`


## 任务清单

- [ ] 识别文件上传请求头，并将body转为byte[]存储；
- [x] 实现NIO模式
- [ ] 完善PUT DELETE 等处理方式
- [ ] 实现文件下载
- [x] 重构代码风格
- [x] 启动参数读取
- [x] 完善参数解析器(50%)(需要增加大量类型转换适配器，知识暂时不够，等时间充足，去了解double的转换）
- [ ] 表单格式解析器

## 问题
- [ ] 消息内容正确转换
- [ ] 性能损耗不了解
- [ ] 配置

## 最近添加
- 类型解析器
- JSON解析器
