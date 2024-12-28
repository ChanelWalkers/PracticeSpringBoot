package com.example.demo.service;

import com.example.demo.dto.request.UserRequestDTO;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.UserDetailResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.utils.UserStatus;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    //    int addUser(UserRequestDTO userRequestDTO) throws ResourceNotFoundException;
    long addUser(UserRequestDTO request);

    void updateUser(long userId, UserRequestDTO reques);

    void changeStatus(long userId, UserStatus userStatus);

    void deleteUser(long userId);

    UserDetailResponse getUserById(long userId) throws ResourceNotFoundException;

    PageResponse<?> getAllUsersWithSortBy(int pageNo, int pageSize, String sortBy);

    PageResponse<?> getAllUsersWithMultipleSortBy(int pageNo, int pageSize, String... sortBy);

    PageResponse<?> getAllUsersWithSortByAndSearch(int pageNo, int pageSize, String sortBy, String search);

    PageResponse<?> advanceSearchByCriteria(int pageNo,int pageSize,String sortBy,String... search);

    PageResponse<?> advanceSearchJoinByCriteria(int pageNo,int pageSize,String sortBy,String address,String... search);
}
