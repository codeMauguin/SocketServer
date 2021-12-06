# 目录

* [WebServer-Socket](#webserver-socket)
  * [socket 模式](#socket-模式)
  * [API](#api)
  * [任务清单](#任务清单)
  * [问题](#问题)


# WebServer-Socket

## socket 模式

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
- [ ] 启动参数读取

## 问题
- [ ] 消息内容正确转换
- [ ] 性能损耗不了解
- [ ] 配置
