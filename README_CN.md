# iot-mqtt-bridge
**feature:**

- [x] 解析简单mqtt消息存到mysql
- [ ] 解析简单mqtt消息存到kafka
- [ ] 解析复杂消息
- [ ] 批处理存储数据
- [ ] 自定义各种handler

你只需要在 model.json 里定义一个描述文件，具体以含义看注释, 

然后配置mqtt ，mysql连接信息在 bridge.yml，

运行 release/bin/server  启动项目 

就可以自动根据描述文件解析mqtt消息并存进数据库内。

```json
[
  {
    "name": "user", //"动态生成的类名" 
    "topic": "user_topic",
    "clientId": "user_client",
    "cleanSession": true,
    "qos": 1,
    "storeType": "mysql",
    "fields": [{
        "name": "id",
        "type": "long", //"存数据库字段的类型" 
        "index": 1,
        "lenght": 8, //"截取多少个字节" 
        "offset": 2,
        "idType": "auto" //"如果是auto就是用数据库的自增，否则是截取到的数据"
    },
    {
        "name": "loginLength",
        "type": "int",
        "index": 2,
        "lenght": 2,
        "isTransient": true //"不持久化到数据库内"
    },
    {
        "name": "login",
        "type": "byte[]",
        "index": 3,
        "dependsOn": 2 //"根据指定index的字段的值作为长度" 
    }

]
```

```yaml
spring:
  datasource:
    driver-class-name: com.mysql.jdbc.Driver
    url: jdbc:mysql://localhost:3306/mqtt_test
    username: 
    password: 
jdbc: 
  template: 
    prefix: t_
    suffix: _test
    insertGetId: false    
logging:
  config: logback.xml
bridge: 
  modelPath: model.json
  mqtts: 
    - url: tcp://localhost:1883
      username: 
      password: 
      keepAlive: 20
      retained: false
      reconnectAttemptsMax: -1
      reconnectDelay: 10
      models: 
        - user


```

