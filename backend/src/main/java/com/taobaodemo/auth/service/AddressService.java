package com.taobaodemo.auth.service;

import com.taobaodemo.auth.entity.Address;
import com.taobaodemo.auth.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class AddressService {
    @Autowired
    private AddressRepository addressRepository;

    public List<Address> getAddressesByUserId(Integer userId) {
        // 直接按数据库默认顺序返回全部地址
        return addressRepository.findByUserId(userId);
    }

    public Optional<Address> getAddressById(Integer addressId) {
        return addressRepository.findById(addressId);
    }

    @Transactional
    public Address addAddress(Address address) {
        // 最简单的逻辑：直接保存前端传来的地址信息
        return addressRepository.save(address);
    }

    @Transactional
    public Address updateAddress(Address address) {
        Address existing = addressRepository.findById(address.getAddressId()).orElse(null);
        if (existing == null) {
            throw new IllegalArgumentException("地址不存在");
        }
        
        // 验证地址属于当前用户
        if (!existing.getUserId().equals(address.getUserId())) {
            throw new IllegalArgumentException("无权修改此地址");
        }

        existing.setReceiverName(address.getReceiverName());
        existing.setReceiverPhone(address.getReceiverPhone());
        existing.setProvince(address.getProvince());
        existing.setCity(address.getCity());
        existing.setDistrict(address.getDistrict());
        existing.setDetailAddress(address.getDetailAddress());
        return addressRepository.save(existing);
    }

    @Transactional
    public void deleteAddress(Integer addressId, Integer userId) {
        Address address = addressRepository.findById(addressId).orElse(null);
        if (address == null) {
            throw new IllegalArgumentException("地址不存在");
        }
        
        if (!address.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权删除此地址");
        }
        
        addressRepository.delete(address);
    }

}
