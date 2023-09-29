package com.edutrain.busbooking.inventory.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsMessagingTemplate;
import org.springframework.jms.core.JmsTemplate;
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
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	private JmsTemplate jmsTemplate;
	
	private static final Logger LOGGER = LogManager.getLogger(InventoryController.class);

	public InventoryController(InventoryRepository inventoryRepository) {
		this.inventoryRepository = inventoryRepository;
	}

	@GetMapping("/getallinventory")
	public List<String> getAllInventory() {
		LOGGER.info("In getAllInventory");
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
				LOGGER.error("Exception occured in getAllInventory"+e.getMessage());
				e.printStackTrace();

			}

			stringRouteList.add(jsonString);
		});

		return stringRouteList;

	}

	@PostMapping("/addinventory")
	public String addInventoryModel(@RequestBody InventoryModel inventoryModel) {
		LOGGER.info("In addInventoryModel");
		String busNumber = inventoryModel.getBusNo();
		LOGGER.debug("BusNumber in addbusroute is " + busNumber);

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
			LOGGER.error("Exception occured in addInventory"+e.getMessage());
			return "There is an error in adding InventoryModel";
		}

	}

	@GetMapping("/getinventory/{BusNo}")
	public Object getInventoryModel(@PathVariable String BusNo) {

		LOGGER.info("In getInventoryModel");
		Optional<InventoryModelWrapper> inventoryModelWrapperRetValue = inventoryRepository.findById(BusNo);

		if (inventoryModelWrapperRetValue.isPresent()) {

			inventoryModelWrapper = inventoryModelWrapperRetValue.get();
			inventoryModel = inventoryModelWrapper.getInventoryModel();

			return inventoryModel;

		} else {

			return new String("Route Not found");
		}

	}

	@DeleteMapping("/deleteinventory/{BusNo}")
	public String deleteInventoryModel(@PathVariable String BusNo) {
		LOGGER.info("In deleteInventoryModel");
		try {
			inventoryRepository.deleteById(BusNo);
			return "Route Deleted successfully";
		} catch (Exception e) {
			LOGGER.error("Exception occured in deleteInventoryModel"+e.getMessage());
			return "Error while deletion";
		}

	}

	@PutMapping("/updateinventory")
	public String updateInventoryModel(@RequestBody InventoryModel inventoryModel) {
		LOGGER.info("In updateInventoryModel");
		String busNumber = inventoryModel.getBusNo();
		LOGGER.debug("BusNumber in addbusroute is " + busNumber);
		Object retValue = getInventoryModel(busNumber);

		if (retValue.getClass().equals(String.class)) {

			if (retValue.toString().equalsIgnoreCase("Route Not found")) {
				return "Route Not found,Please enter valid route";
			}

		} else {
			inventoryModel = (InventoryModel) retValue;
		}

		// InventoryModelWrapper inventoryModelWrapper= new InventoryModelWrapper();
		inventoryModelWrapper.setBusNo(busNumber);
		inventoryModelWrapper.setInventoryModel(inventoryModel);

		try {
			InventoryModelWrapper retValue1 = inventoryRepository.save(inventoryModelWrapper);

			if (retValue1 != null) {
				return "InventoryModel Updated successfully";
			} else {
				return "There is an error in updating InventoryModel";
			}
		} catch (Exception e) {
			LOGGER.error("Exception occured in updateInventoryModel"+e.getMessage());
			return "There is an error in updating  InventoryModel";
		}
	}

	

	@JmsListener(destination = "PaymentToInventory")
	public String ReceivePaymentAndSendInventory(String bookPaymentStr) {
		LOGGER.info("In ReceiveBookingAndProcessPayment");
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			LOGGER.error("Exception occured in ReceivePaymentAndSendInventory"+e.getMessage());
			e.printStackTrace();
		}

		LOGGER.debug("Message Received in ReceivePaymentAndSendInventory " + bookPaymentStr);
		try {
			bookPayment =  new ObjectMapper().readValue(bookPaymentStr, BookPayment.class);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		LOGGER.debug("bookPayment object  Received" + bookPayment);

		inventoryModel.setAvailableSeats(String.valueOf(
				Integer.parseInt(inventoryModel.getAvailableSeats()) - Integer.parseInt(bookPayment.getNoOfSeats())));
		inventoryModel.setBusNo(bookPayment.getBusNo());

		updateInventoryModel(inventoryModel);	

		SendMessageToBookingService(bookPayment);

		return null;

	}

	private void SendMessageToBookingService(BookPayment bookPayment) {
		// TODO Auto-generated method stub
		LOGGER.info("In SendMessageToBookingService");		
		String bookPaymentStr = null;
		try {
			ObjectWriter objWriter = new ObjectMapper().writer().withDefaultPrettyPrinter();
			bookPaymentStr = objWriter.writeValueAsString(bookPayment);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			LOGGER.error("Exception occured in SendMessageToBookingService"+e.getMessage());
			e.printStackTrace();
		}

		LOGGER.debug(" BookPayment  " + bookPaymentStr);
		
		jmsTemplate.convertAndSend("InventoryToBooking", bookPaymentStr);

	}

}
