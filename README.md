# iot-mqtt-bridge
**feature:**

- [x] 解析简单mqtt消息存到mysql
- [x] 解析复杂消息
- [ ] 解析简单mqtt消息存到kafka
- [ ] 批处理存储数据
- [ ] 自定义各种handler



假如有一个这种结构的消息，要存储到t_device_info表里

| 列名     | device_id | msg_code   | msg_value      | serial num |
| -------- | --------- | ---------- | -------------- | ---------- |
| 数据类型 | short     | int        | float          | int        |
| 原值     | 3,-23     | 0,0,-44,49 | 66,-10,-26,102 | 0,0,4,-46  |
| 实际值   | 1001      | 10000      | 123.45         | 4321       |

可以如下配置：

```sql
DROP TABLE IF EXISTS t_device_info;
CREATE TABLE t_device_info (
  id int AUTO_INCREMENT,
  device_id smallint NOT NULL,
  msg_code int,
  msg_value int,
  time_stamp datetime,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
```



```json
[
  {
    "name": "deviceInfo",
    "tableName": "t_device_info",
    "topic": "device_info_topic",
    "clientId": "device_info_client",
    "cleanSession": true,
    "qos": 1,
    "storeType": "mysql",
    "fields": [{
        "name": "deviceId",
        "type": "short",
        "index": 1,
        "lenght": 2
    },
    {
        "name": "msgCode",
        "type": "int",
        "index": 2,
        "lenght": 4
    },
    {
        "name": "msgValue",
        "type": "float",
        "index": 3,
        "lenght": 4
    },
    {
        "name": "serialNum",
        "type": "int",
        "index": 4,
        "lenght": 4
    }]
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
        - deviceInfo


```

cd release/bin

./server 启动项目

发送消息后，最终数据就会被自动保存到数据库内
