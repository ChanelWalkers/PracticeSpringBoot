package com.example.demo.controller;

import com.example.demo.configuration.Translator;
import com.example.demo.dto.request.UserRequestDTO;
import com.example.demo.dto.response.ResponseData;
import com.example.demo.dto.response.ResponseError;
import com.example.demo.dto.response.UserDetailResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.service.UserService;
import com.example.demo.utils.UserStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@Validated
@Tag(name = "User Controller")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @Operation
    @PostMapping(value = "/")
    public ResponseData<Long> addUser(@Valid @RequestBody UserRequestDTO userRequestDTO) {
        try {
            long userId = userService.addUser(userRequestDTO);
            return new ResponseData<>(HttpStatus.CREATED.value(), Translator.toLocale("user.add.success"), userId);
        } catch (Exception ex) {
            log.error("Error: {}", ex.getMessage(), ex.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "create user fail");
        }
    }

    @PutMapping("/{id}")
    public ResponseData<UserRequestDTO> updateUser(@PathVariable("id") @Min(1) int userId, @RequestBody UserRequestDTO userRequestDTO) {
        try {
            userService.updateUser(userId, userRequestDTO);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "user has been updated", userRequestDTO);
        } catch (Exception ex) {
            log.error("Error: {}", ex.getMessage(), ex.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), "update failed");
        }
    }

    @PatchMapping("/{userId}")
    public ResponseData<?> updateUserStatus(@PathVariable("userId") int userId, @RequestParam UserStatus status) {
        try {
            userService.changeStatus(userId, status);
            return new ResponseData<>(HttpStatus.ACCEPTED.value(), "user updated successfully!");
        } catch (Exception ex) {
            log.error("Error: {}", ex.getMessage(), ex.getCause());
            return new ResponseError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "update failed");
        }
    }

    @DeleteMapping("/{userId}")
    public ResponseData<?> deleteUser(@PathVariable("userId") long userId) {
        try {
            userService.deleteUser(userId);
            return new ResponseData<>(HttpStatus.NO_CONTENT.value(), "user has been deleted");
        } catch (Exception ex) {
            log.error("Error: {}", ex.getMessage(), ex.getCause());
            return new ResponseError(HttpStatus.INTERNAL_SERVER_ERROR.value(), "update failed");
        }
    }

    @GetMapping("/{userId}")
    public ResponseData<UserDetailResponse> getUserById(@PathVariable("userId") long userId) {
        try {
            UserDetailResponse response = userService.getUserById(userId);
            return new ResponseData(HttpStatus.OK.value(), "user", response);
        } catch (ResourceNotFoundException ex) {
            log.error("Error: {}", ex.getMessage(), ex.getCause());
            return new ResponseError(HttpStatus.BAD_REQUEST.value(), ex.getMessage());
        }
    }

    @GetMapping("/list")
    public ResponseData<Object> getAllUsers(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                            @Min(10) @RequestParam(defaultValue = "20", required = false) int pageSize,
                                            @RequestParam(required = false) String sortBy) {
        return new ResponseData<>(HttpStatus.OK.value(), "list of users", userService.getAllUsersWithSortBy(pageNo, pageSize, sortBy));
    }

    @GetMapping("/all")
    public ResponseData<Object> getAllUsersByMultipleColumns(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                                             @Min(10) @RequestParam(defaultValue = "20", required = false) int pageSize,
                                                             @RequestParam(required = false) String... sortBy) {
        return new ResponseData<>(HttpStatus.OK.value(), "list of users", userService.getAllUsersWithMultipleSortBy(pageNo, pageSize, sortBy));
    }

    @GetMapping("/all-search")
    public ResponseData<Object> getAllUsersSortByAndSearch(@RequestParam(defaultValue = "0", required = false) int pageNo,
                                                           @Min(10) @RequestParam(defaultValue = "20", required = false) int pageSize,
                                                           @RequestParam(required = false) String sortBy,
                                                           @RequestParam(required = false) String search) {
        return new ResponseData<>(HttpStatus.OK.value(), "list of users", userService.getAllUsersWithSortByAndSearch(pageNo, pageSize, sortBy, search));
    }

    @GetMapping("/advance-search-specification")
    public ResponseData<?> advanceSearch(Pageable pageable,
                                         @RequestParam(required = false) String[] user,
                                         @RequestParam(required = false) String[] address) {
        return new ResponseData<>(HttpStatus.OK.value(), "list of users", userService.advanceSearchSpecification(pageable,user,address));
    }
}
