package com.example.demo.service.impl;

import com.example.demo.dto.request.AddressDTO;
import com.example.demo.dto.request.UserRequestDTO;
import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.UserDetailResponse;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.model.AddressEntity;
import com.example.demo.model.UserEntity;
import com.example.demo.repository.UserRepository;
import com.example.demo.repository.custom.SearchRepository;
import com.example.demo.service.UserService;
import com.example.demo.utils.UserStatus;
import com.example.demo.utils.UserType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository repository;
    private final SearchRepository searchRepository;

    @Override
    public long addUser(UserRequestDTO request) {
        UserEntity userEntity = UserEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .email(request.getEmail())
                .status(request.getStatus())
                .type(UserType.valueOf(request.getUserType().toUpperCase()))
                .username(request.getUsername())
                .password(request.getPassword())
                .gender(request.getGender())
                .build();
        Set<AddressEntity> addresses = convertToAddress(request.getAddresses());
        addresses.forEach(a -> a.setUser(userEntity));
        userEntity.setAddresses(addresses);
        repository.save(userEntity);
        log.info("User has been saved! {}", userEntity.getId());
        return userEntity.getId();
    }

    @Override
    public void updateUser(long userId, UserRequestDTO request) {
        UserEntity userEntity = UserEntity.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateOfBirth(request.getDateOfBirth())
                .email(request.getEmail())
                .status(request.getStatus())
                .type(UserType.valueOf(request.getUserType().toUpperCase()))
                .username(request.getUsername())
                .password(request.getPassword())
                .gender(request.getGender())
                .phone(request.getPhone())
                .build();
        userEntity.setId(userId);
        Set<AddressEntity> addresses = convertToAddress(request.getAddresses());
        addresses.forEach(a -> a.setUser(userEntity));
        userEntity.setAddresses(addresses);
        repository.save(userEntity);
    }

    @Override
    public void changeStatus(long userId, UserStatus userStatus) {
        UserEntity userEntity = repository.findById(userId).get();
        userEntity.setStatus(userStatus);
        repository.save(userEntity);
    }


    @Transactional
    @Override
    public void deleteUser(long userId) {
        repository.deleteById(userId);
    }

    @Override
    public UserDetailResponse getUserById(long userId) throws ResourceNotFoundException {
        UserEntity userEntity = repository.findById(userId).orElseThrow(() -> new ResourceNotFoundException("user not found!"));
        return UserDetailResponse.builder()
                .id(userEntity.getId())
                .firstName(userEntity.getFirstName())
                .lastName(userEntity.getLastName())
                .phone(userEntity.getPhone())
                .email(userEntity.getEmail())
                .build();
    }

    @Override
    public PageResponse<?> getAllUsersWithSortBy(int pageNo, int pageSize, String sortBy) {
        int page = 0;
        if(pageNo > 0){
            page = pageNo - 1;
        }
        List<Sort.Order> sorts = new ArrayList<>();
        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                if (matcher.group(3).equalsIgnoreCase("asc")) {
                    sorts.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                } else {
                    sorts.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                }
            }
        }
        Pageable pageable = PageRequest.of(page, pageSize, Sort.by(sorts));
        Page<UserEntity> userPages = repository.findAll(pageable);
        List<UserDetailResponse> list = repository.findAll(pageable).stream()
                .map(a -> UserDetailResponse.builder()
                        .id(a.getId())
                        .email(a.getEmail())
                        .phone(a.getPhone())
                        .lastName(a.getLastName())
                        .firstName(a.getFirstName())
                        .build())
                .toList();
        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .contents(list)
                .totalPages(userPages.getTotalPages())
                .totalElements(userPages.getTotalElements())
                .build();
    }

    @Override
    public PageResponse<?> getAllUsersWithMultipleSortBy(int pageNo, int pageSize, String... sortBy) {
        List<Sort.Order> orders = new ArrayList<>();
        if (sortBy != null) {

            for (String sort : sortBy) {
                if (StringUtils.hasLength(sort)) {
                    Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
                    Matcher matcher = pattern.matcher(sort);
                    if (matcher.find()) {
                        if (matcher.group(3).equalsIgnoreCase("asc")) {
                            orders.add(new Sort.Order(Sort.Direction.ASC, matcher.group(1)));
                        } else {
                            orders.add(new Sort.Order(Sort.Direction.DESC, matcher.group(1)));
                        }
                    }
                }
            }
        }
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by(orders));
        Page<UserEntity> userPages = repository.findAll(pageable);
        List<UserDetailResponse> list = userPages.stream()
                .map(a -> UserDetailResponse.builder()
                        .id(a.getId())
                        .email(a.getEmail())
                        .phone(a.getPhone())
                        .firstName(a.getFirstName())
                        .lastName(a.getLastName())
                        .build()
                ).toList();
        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .contents(list)
                .totalPages(userPages.getTotalPages())
                .totalElements(userPages.getTotalElements())
                .build();
    }

    @Override
    public PageResponse<?> getAllUsersWithSortByAndSearch(int pageNo,int pageSize,String sortBy,String search) {
        return searchRepository.getAllUsersSortByAndSearch(pageNo, pageSize, sortBy, search);
    }

    @Override
    public PageResponse<?> advanceSearchByCriteria(int pageNo, int pageSize, String sortBy, String... search) {
        return searchRepository.advanceSearchUser(pageNo, pageSize, sortBy, search);
    }

    @Override
    public PageResponse<?> advanceSearchJoinByCriteria(int pageNo, int pageSize, String sortBy, String address, String... search) {
        return searchRepository.advanceSearchJoinByCriteria(pageNo, pageSize, sortBy, address, search);
    }


//    @Override
//    public int addUser(UserRequestDTO userRequestDTO) throws ResourceNotFoundException {
//        if(!userRequestDTO.getFirstName().equals("Dat")){
//            throw new ResourceNotFoundException("First name is invalid");
//        }
//        return 0;
//    }

    private Set<AddressEntity> convertToAddress(Set<AddressDTO> addresses) {
        Set<AddressEntity> result = new HashSet<>();
        addresses.forEach(a ->
                result.add(AddressEntity.builder()
                        .appartmentNumber(a.getApartmentNumber())
                        .street(a.getStreet())
                        .streetNumber(a.getStreetNumber())
                        .country(a.getCountry())
                        .building(a.getBuilding())
                        .floor(a.getFloor())
                        .city(a.getCity())
                        .addressType(a.getAddressType())
                        .build())
        );
        return result;
    }
}
