# iot-mqtt-bridge
**feature:**

- [x] parser basic  type data into mysql
- [ ] parser basic  type data into kafka
- [ ] support more complex byte data persistence
- [ ] batch save data
- [ ] custom mqtt handler

you just define a description json file  in model.json, 

and mqtt connect info and  mysql db info in bridge.yml

then run release/bin/server  to start

this tools will automate save mqtt byte data into db.

```json
[
  {
    "name": "user", //"this will become class name” 
    "topic": "user_topic",
    "clientId": "user_client",
    "cleanSession": true,
    "qos": 1,
    "storeType": "mysql",
    "fields": [{
        "name": "id",
        "type": "long", //"this will become field type" 
        "index": 1,
        "lenght": 8, //"how many bytes will be cut into this field" 
        "offset": 2,
        "idType": "auto" //”this field will use db auto gene value, insert into db“
    },
    {
        "name": "loginLength",
        "type": "int",
        "index": 2,
        "lenght": 2,
        "isTransient": true //"this will not saved into db"
    },
    {
        "name": "login",
        "type": "byte[]",
        "index": 3,
        "dependsOn": 2 //"according loginLength value to cut bytes“ 
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

