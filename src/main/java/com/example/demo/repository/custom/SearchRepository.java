package com.example.demo.repository.custom;

import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.UserDetailResponse;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Repository
@Slf4j
public class SearchRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public PageResponse<?> getAllUsersSortByAndSearch(int pageNo, int pageSize, String sortBy, String search) {
        StringBuilder query = new StringBuilder("Select new com.example.demo.dto.response.UserDetailResponse(u.id,u.firstName,u.lastName,u.phone,u.email) from User u where 1=1 ");

        Pageable pageable = PageRequest.of(pageNo, pageSize);
        if (StringUtils.hasLength(search)) {
            query.append(" and lower(u.firstName) like lower(?1) ");
            query.append(" or lower(u.lastName) like lower(?2) ");
            query.append(" or lower(u.email) like lower(?3) ");
        }

        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                query.append(String.format("order by u.%s %s", matcher.group(1), matcher.group(3)));
            }
        }

        Query selectQuery = entityManager.createQuery(query.toString());

        selectQuery.setFirstResult(pageNo);
        selectQuery.setMaxResults(pageSize);
        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(.*)");
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                if (matcher.group(3).equalsIgnoreCase("asc")) {
                    pageable = PageRequest.of(pageNo, pageSize, Sort.by(new Sort.Order(Sort.Direction.ASC,matcher.group(3))));
                } else {
                    pageable = PageRequest.of(pageNo, pageSize, Sort.by(new Sort.Order(Sort.Direction.DESC,matcher.group(3))));
                }
            }
        }

        if (StringUtils.hasLength(search)) {
            selectQuery.setParameter(1, String.format("%%%s%%", search));
            selectQuery.setParameter(2, String.format("%%%s%%", search));
            selectQuery.setParameter(3, String.format("%%%s%%", search));
        }



        Long totalElements = totalElementWithSearch(search);

        List<Object> list = selectQuery.getResultList();
        Page<?> userPage = new PageImpl<Object>(list, pageable, totalElements);

        return PageResponse.builder()
                .pageNo(userPage.getNumber())
                .pageSize(userPage.getSize())
                .contents(userPage.stream().toList())
                .totalPages(userPage.getTotalPages())
                .totalElements(userPage.getTotalElements())
                .build();
    }

    private Long totalElementWithSearch(String search) {
        StringBuilder query = new StringBuilder("Select COUNT(*) from User u ");

        if (StringUtils.hasLength(search)) {
            query.append(" where lower(u.firstName) like lower(?1) ");
            query.append(" or lower(u.lastName) like lower(?2) ");
            query.append(" or lower(u.email) like lower(?3) ");
        }
        Query selectQuery = entityManager.createQuery(query.toString());

        if (StringUtils.hasLength(search)) {
            selectQuery.setParameter(1, String.format("%%%s%%", search));
            selectQuery.setParameter(2, String.format("%%%s%%", search));
            selectQuery.setParameter(3, String.format("%%%s%%", search));
        }
        return (Long) selectQuery.getSingleResult();
    }
}
