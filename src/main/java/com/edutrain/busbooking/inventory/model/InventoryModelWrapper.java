package com.edutrain.busbooking.inventory.model;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;
import org.springframework.data.annotation.Id;

@Component
@Document("InventoryDtls")
public class InventoryModelWrapper {
	
	@Id
	private String busNo;
	private InventoryModel inventoryModel;
	
	
	public String getBusNo() {
		return busNo;
	}
	public void setBusNo(String busNo) {
		this.busNo = busNo;
	}
	public InventoryModel getInventoryModel() {
		return inventoryModel;
	}
	public void setInventoryModel(InventoryModel inventoryModel) {
		this.inventoryModel = inventoryModel;
	}	

}
