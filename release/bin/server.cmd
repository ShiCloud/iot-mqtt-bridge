@echo off
java -Dloader.path="../" -jar  ../iot-mqtt-bridge-1.0.jar --spring.config.location=../config/bridge.yml

