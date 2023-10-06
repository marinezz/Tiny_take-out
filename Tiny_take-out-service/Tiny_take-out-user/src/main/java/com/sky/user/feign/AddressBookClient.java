package com.sky.user.feign;

import com.sky.apis.user.IAddressBookClient;
import com.sky.model.user.entirty.AddressBook;
import com.sky.user.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AddressBookClient implements IAddressBookClient {

    @Autowired
    private AddressBookService addressBookService;
    @Override
    @GetMapping("/feign/addressBook/{id}")
    public AddressBook getById(@PathVariable Long id) {
        return addressBookService.getById(id);
    }
}
