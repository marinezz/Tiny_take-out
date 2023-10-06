package com.sky.apis.user;

import com.sky.model.user.entirty.AddressBook;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "take-out-user",path = "/feign")
public interface IAddressBookClient {

    @GetMapping("/addressBook/{id}")
    AddressBook getById(@PathVariable Long id);
}
