package com.edutrain.busbooking.inventory.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.edutrain.busbooking.inventory.model.InventoryModelWrapper;

@Repository
public interface  InventoryRepository extends MongoRepository<InventoryModelWrapper,String>{

}
