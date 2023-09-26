package com.edutrain.busbooking.inventory.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edutrain.busbooking.inventory.model.BookPayment;
import com.edutrain.busbooking.inventory.model.InventoryModel;
import com.edutrain.busbooking.inventory.model.InventoryModelWrapper;
import com.edutrain.busbooking.inventory.repository.InventoryRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

	@Autowired
	InventoryModelWrapper inventoryModelWrapper;

	@Autowired
	InventoryModel inventoryModel;

	@Autowired
	BookPayment bookPayment;

	@Autowired
	private final InventoryRepository inventoryRepository;

	@Autowired
	private JmsMessagingTemplate jmsMessagingTemplate;

	public InventoryController(InventoryRepository inventoryRepository) {
		this.inventoryRepository = inventoryRepository;
	}

	@GetMapping("/getallinventory")
	public List<String> getAllInventory() {

		List<InventoryModel> InventoryModelList = new ArrayList<InventoryModel>();
		List<String> stringRouteList = new ArrayList<String>();

		inventoryRepository.findAll().forEach((inventoryModelWrapper) -> {
			InventoryModelList.add(inventoryModelWrapper.getInventoryModel());
		});

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();

		InventoryModelList.forEach((inventoryModel) -> {

			String jsonString = null;

			try {
				jsonString = ow.writeValueAsString(inventoryModel);

			} catch (JsonProcessingException e) {

				e.printStackTrace();

			}

			stringRouteList.add(jsonString);
		});

		return stringRouteList;

	}

	@PostMapping("/addinventory")
	public String addInventoryModel(@RequestBody InventoryModel inventoryModel) {

		String busNumber = inventoryModel.getBusNo();
		System.out.println("BusNumber in addbusroute is " + busNumber);

		// InventoryModelWrapper inventoryModelWrapper= new InventoryModelWrapper();
		inventoryModelWrapper.setBusNo(busNumber);
		inventoryModelWrapper.setInventoryModel(inventoryModel);

		try {
			InventoryModelWrapper retValue = inventoryRepository.save(inventoryModelWrapper);

			if (retValue != null) {
				return "InventoryModel Added successfully";
			} else {
				return "There is an error in adding InventoryModel";
			}
		} catch (Exception e) {

			return "There is an error in adding InventoryModel";
		}

	}

	@GetMapping("/getinventory/{BusNo}")
	public String getInventoryModel(@PathVariable String BusNo) {

		Optional<InventoryModelWrapper> inventoryModelWrapperRetValue = inventoryRepository.findById(BusNo);

		if (inventoryModelWrapperRetValue.isPresent()) {

			inventoryModelWrapper = inventoryModelWrapperRetValue.get();
			inventoryModel = inventoryModelWrapper.getInventoryModel();

			/*
			 * String InventoryModelStr = "BusNo: " + inventoryModel.getBusNo() +
			 * ",availableSeats: " + inventoryModel.getAvailableSeats() + ", lastUpdtDate: "
			 * + inventoryModel.getLastUpdtDate();
			 */

			ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
			String jsonString;
			try {
				jsonString = ow.writeValueAsString(inventoryModel);
				return jsonString;
			} catch (JsonProcessingException e) {

				e.printStackTrace();
				return "Exception occured";
			}

		} else {

			return "Route Not found";
		}

	}

	@DeleteMapping("/deleteinventory/{BusNo}")
	public String deleteInventoryModel(@PathVariable String BusNo) {
		try {
			inventoryRepository.deleteById(BusNo);
			return "Route Deleted successfully";
		} catch (Exception e) {
			return "Error while deletion";
		}

	}

	@PutMapping("/updateinventory")
	public String updateInventoryModel(@RequestBody InventoryModel inventoryModel) {

		String busNumber = inventoryModel.getBusNo();
		System.out.println("BusNumber in addbusroute is " + busNumber);
		String RetValue = getInventoryModel(busNumber);

		if (RetValue.equalsIgnoreCase("Route Not found")) {
			return "Route Not found,Please enter valid route";
		} else {

			// InventoryModelWrapper inventoryModelWrapper= new InventoryModelWrapper();
			inventoryModelWrapper.setBusNo(busNumber);
			inventoryModelWrapper.setInventoryModel(inventoryModel);

			try {
				InventoryModelWrapper retValue = inventoryRepository.save(inventoryModelWrapper);

				if (retValue != null) {
					return "InventoryModel Updated successfully";
				} else {
					return "There is an error in updating InventoryModel";
				}
			} catch (Exception e) {

				return "There is an error in updating  InventoryModel";
			}
		}

	}

	@JmsListener(destination = "PaymentToInventory")
	public String ReceiveBookingAndProcessPayment(Object obj) {

		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		bookPayment = (BookPayment) obj;

		inventoryModel.setAvailableSeats(String.valueOf(
				Integer.parseInt(inventoryModel.getAvailableSeats()) - Integer.parseInt(bookPayment.getNoOfSeats())));
		inventoryModel.setBusNo(bookPayment.getBusNo());
		
		updateInventoryModel(inventoryModel);

		System.out.println("Message Received" + obj);

		
		SendMessageToBookingService(bookPayment);

		return null;

	}

	private String SendMessageToBookingService(BookPayment bookPayment) {
		// TODO Auto-generated method stub
		jmsMessagingTemplate.convertAndSend("InventoryToBooking", bookPayment);

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
		String jsonString;
		try {
			jsonString = ow.writeValueAsString(bookPayment);
			return jsonString;
		} catch (JsonProcessingException e) {

			e.printStackTrace();
			return "Exception occured";
		}

	}

}
