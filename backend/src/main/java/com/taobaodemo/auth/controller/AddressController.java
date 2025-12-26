package com.taobaodemo.auth.controller;

import com.taobaodemo.auth.entity.Address;
import com.taobaodemo.auth.entity.User;
import com.taobaodemo.auth.service.AddressService;
import com.taobaodemo.common.util.ResponseUtil;
import com.taobaodemo.common.util.SessionUtil;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {
    @Autowired
    private AddressService addressService;

    @GetMapping
    public Object getAddresses(HttpSession session) {
        User user = SessionUtil.getCurrentUser(session);
        if (user == null) {
            return ResponseUtil.notLoggedIn();
        }
        List<Address> addresses = addressService.getAddressesByUserId(user.getUserId());
        return ResponseUtil.success(addresses);
    }

    @GetMapping("/{id}")
    public Object getAddressById(@PathVariable Integer id, HttpSession session) {
        User user = SessionUtil.getCurrentUser(session);
        if (user == null) {
            return ResponseUtil.notLoggedIn();
        }
        Address address = addressService.getAddressById(id).orElse(null);
        if (address == null) {
            return ResponseUtil.error("地址不存在");
        }
        if (!address.getUserId().equals(user.getUserId())) {
            return ResponseUtil.error("无权访问此地址");
        }
        return ResponseUtil.success(address);
    }

    @PostMapping
    public Object addAddress(@RequestBody Address address, HttpSession session) {
        User user = SessionUtil.getCurrentUser(session);
        if (user == null) {
            return ResponseUtil.notLoggedIn();
        }
        address.setUserId(user.getUserId());
        Address savedAddress = addressService.addAddress(address);
        return ResponseUtil.success(savedAddress, "地址添加成功");
    }

    @PutMapping("/{id}")
    public Object updateAddress(@PathVariable Integer id, @RequestBody Address address, HttpSession session) {
        User user = SessionUtil.getCurrentUser(session);
        if (user == null) {
            return ResponseUtil.notLoggedIn();
        }
        address.setAddressId(id);
        address.setUserId(user.getUserId());
        try {
            Address updatedAddress = addressService.updateAddress(address);
            return ResponseUtil.success(updatedAddress, "地址更新成功");
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public Object deleteAddress(@PathVariable Integer id, HttpSession session) {
        User user = SessionUtil.getCurrentUser(session);
        if (user == null) {
            return ResponseUtil.notLoggedIn();
        }
        try {
            addressService.deleteAddress(id, user.getUserId());
            return ResponseUtil.success(null, "地址删除成功");
        } catch (IllegalArgumentException e) {
            return ResponseUtil.error(e.getMessage());
        }
    }
}

