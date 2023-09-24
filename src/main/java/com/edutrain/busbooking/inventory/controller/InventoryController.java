package com.edutrain.busbooking.inventory.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.edutrain.busbooking.inventory.model.InventoryModel;
import com.edutrain.busbooking.inventory.model.InventoryModelWrapper;
import com.edutrain.busbooking.inventory.repository.InventoryRepository;

@RestController
@RequestMapping("/inventory")
public class InventoryController {

	@Autowired
	InventoryModelWrapper inventoryModelWrapper;

	@Autowired
	InventoryModel inventoryModel;

	@Autowired
	private final InventoryRepository inventoryRepository;

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

		InventoryModelList.forEach((inventoryModel) -> {
			stringRouteList.add("BusNo: " + inventoryModel.getBusNo() + ",availableSeats: "
					+ inventoryModel.getAvailableSeats() + ", lastUpdtDate: " + inventoryModel.getLastUpdtDate());
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
			String InventoryModelStr = "BusNo: " + inventoryModel.getBusNo() + ",availableSeats: "
					+ inventoryModel.getAvailableSeats() + ", lastUpdtDate: " + inventoryModel.getLastUpdtDate();

			return InventoryModelStr;

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

}
