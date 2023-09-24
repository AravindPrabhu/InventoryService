 package com.edutrain.busbooking.inventory;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

import com.edutrain.busbooking.inventory.repository.InventoryRepository;

@SpringBootApplication
@EnableMongoRepositories(basePackageClasses = InventoryRepository.class)
@EnableEurekaClient
@ComponentScan(basePackages = "com.edutrain.busbooking.inventory.controller,com.edutrain.busbooking.inventory.model,com.edutrain.busbooking.inventory.repository")
public class InventoryServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(InventoryServiceApplication.class, args);
	}

}
