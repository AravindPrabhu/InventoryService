package com.edutrain.busbooking.inventory.model;

import java.util.Date;

import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.stereotype.Component;

@Component
@Document("InventoryDtls")
public class InventoryModel {
	
	
	private String busNo;
	private String availableSeats;
	private Date lastUpdtDate;
	
	public String getBusNo() {
		return busNo;
	}
	public void setBusNo(String busNo) {
		this.busNo = busNo;
	}
	public String getAvailableSeats() {
		return availableSeats;
	}
	public void setAvailableSeats(String availableSeats) {
		this.availableSeats = availableSeats;
	}
	public Date getLastUpdtDate() {
		return lastUpdtDate;
	}
	public void setLastUpdtDate(Date lastUpdtDate) {
		this.lastUpdtDate = lastUpdtDate;
	}

	
	
	
	

}
