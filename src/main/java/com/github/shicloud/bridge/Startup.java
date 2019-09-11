package com.github.shicloud.bridge;

import java.io.File;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import com.github.shicloud.bridge.config.BridgeConfig;
import com.github.shicloud.bridge.config.MqttConfig;
import com.github.shicloud.bridge.model.Model;
import com.github.shicloud.bridge.model.ModelParser;
import com.github.shicloud.bridge.mysql.MsgHandler;
import com.github.shicloud.jdbc.templete.JdbcTemplateTool;

@SpringBootApplication(scanBasePackages = "com.github.shicloud")
@EnableAsync
@EnableTransactionManagement(proxyTargetClass = true)
public class Startup implements ApplicationRunner {
	private static final Logger log = LoggerFactory.getLogger(Startup.class);

	@Autowired
	BridgeConfig bridgeConfig;
	@Autowired
	JdbcTemplateTool jtt;

	public static void main(String[] args) {
		new SpringApplicationBuilder().sources(Startup.class).run(args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		String json = FileUtils.readFileToString(new File(bridgeConfig.getModelPath()));
		Map<String, Model> modelMap = ModelParser.loadAll(json);

		for (MqttConfig mqttConfig : bridgeConfig.getMqtts()) {
			for (String modelName : mqttConfig.getModels()) {
				new MsgHandler(modelMap.get(modelName), mqttConfig, jtt);
			}
		}

		log.info("startup success.");
	}
}
