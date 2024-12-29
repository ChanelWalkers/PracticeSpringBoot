package com.example.demo.repository.custom;

import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.UserDetailResponse;
import com.example.demo.model.AddressEntity;
import com.example.demo.model.UserEntity;
import com.example.demo.repository.specification.SpecSearchCriteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.criteria.*;
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
                    pageable = PageRequest.of(pageNo, pageSize, Sort.by(new Sort.Order(Sort.Direction.ASC, matcher.group(3))));
                } else {
                    pageable = PageRequest.of(pageNo, pageSize, Sort.by(new Sort.Order(Sort.Direction.DESC, matcher.group(3))));
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

    public PageResponse<?> getUserJoinedAddress(Pageable pageable, String[] user, String[] address) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> query = builder.createQuery(UserEntity.class);
        Root<UserEntity> userRoot = query.from(UserEntity.class);

        Join<UserEntity, AddressEntity> userAddressJoin = userRoot.join("addresses");

        Pattern pattern = Pattern.compile("(\\w+?)([<:>~!])(.*)(\\p{Punct}?)(.*)(\\p{Punct}?)");
        List<Predicate> userPre = new ArrayList<>();
        List<Predicate> addPre = new ArrayList<>();
        for (String item : user) {
            Matcher matcher = pattern.matcher(item);
            if (matcher.find()) {
                SpecSearchCriteria criteria = new SpecSearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                userPre.add(toPredicate(userRoot, builder, criteria));
            }
        }

        for (String item : address) {
            Matcher matcher = pattern.matcher(item);
            if (matcher.find()) {
                SpecSearchCriteria criteria = new SpecSearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                addPre.add(toPredicate(userAddressJoin, builder, criteria));
            }
        }

        Predicate userPredicateArr = builder.or(userPre.toArray(new Predicate[0]));
        Predicate addPredicateArr = builder.or(addPre.toArray(new Predicate[0]));

        Predicate finalPre = builder.and(userPredicateArr, addPredicateArr);
        query.where(finalPre);

        List<UserEntity> users = entityManager
                .createQuery(query)
                .setFirstResult(pageable.getPageNumber())
                .setMaxResults(pageable.getPageSize())
                .getResultList();

        Long totalElements = countElement(user,address);

//        Page<?> userPage = new PageImpl<Object>(users,pageable,totalElements);

        return PageResponse.builder()
                .pageNo(pageable.getPageNumber())
                .pageSize(pageable.getPageSize())
                .contents(users)
                .totalElements(totalElements)
                .build();
    }

    private Long countElement(String[] user, String[] address) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<UserEntity> userRoot = query.from(UserEntity.class);

        Join<UserEntity, AddressEntity> userAddressJoin = userRoot.join("addresses");

        Pattern pattern = Pattern.compile("(\\w+?)([<:>~!])(.*)(\\p{Punct}?)(.*)(\\p{Punct}?)");
        List<Predicate> userPre = new ArrayList<>();
        List<Predicate> addPre = new ArrayList<>();
        for (String item : user) {
            Matcher matcher = pattern.matcher(item);
            if (matcher.find()) {
                SpecSearchCriteria criteria = new SpecSearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                userPre.add(toPredicate(userRoot, builder, criteria));
            }
        }

        for (String item : address) {
            Matcher matcher = pattern.matcher(item);
            if (matcher.find()) {
                SpecSearchCriteria criteria = new SpecSearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4), matcher.group(5));
                addPre.add(toPredicate(userAddressJoin, builder, criteria));
            }
        }

        Predicate userPredicateArr = builder.or(userPre.toArray(new Predicate[0]));
        Predicate addPredicateArr = builder.or(addPre.toArray(new Predicate[0]));

        Predicate finalPre = builder.and(userPredicateArr, addPredicateArr);
        query.select(builder.count(userRoot));
        query.where(finalPre);

        return entityManager.createQuery(query).getSingleResult();
    }

    private Predicate toPredicate(Root<UserEntity> root, CriteriaBuilder criteriaBuilder, SpecSearchCriteria criteria) {
        return switch (criteria.getOperation()) {
            case EQUALITY -> criteriaBuilder.equal(root.get(criteria.getKey()), criteria.getValue());
            case NEGATION -> criteriaBuilder.notEqual(root.get(criteria.getKey()), criteria.getValue());
            case GREATER_THAN ->
                    criteriaBuilder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LESS_THAN -> criteriaBuilder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LIKE -> criteriaBuilder.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
            case STARTS_WITH -> criteriaBuilder.like(root.get(criteria.getKey()), criteria.getValue().toString() + "%");
            case ENDS_WITH -> criteriaBuilder.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString());
            case CONTAINS -> criteriaBuilder.like(root.get(criteria.getKey()), "%" + criteria.getValue() + "%");
        };
    }


    private Predicate toPredicate(Join<UserEntity, AddressEntity> root, CriteriaBuilder criteriaBuilder, SpecSearchCriteria criteria) {
        return switch (criteria.getOperation()) {
            case EQUALITY -> criteriaBuilder.equal(root.get(criteria.getKey()), criteria.getValue());
            case NEGATION -> criteriaBuilder.notEqual(root.get(criteria.getKey()), criteria.getValue());
            case GREATER_THAN ->
                    criteriaBuilder.greaterThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LESS_THAN -> criteriaBuilder.lessThan(root.get(criteria.getKey()), criteria.getValue().toString());
            case LIKE -> criteriaBuilder.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString() + "%");
            case STARTS_WITH -> criteriaBuilder.like(root.get(criteria.getKey()), criteria.getValue().toString() + "%");
            case ENDS_WITH -> criteriaBuilder.like(root.get(criteria.getKey()), "%" + criteria.getValue().toString());
            case CONTAINS -> criteriaBuilder.like(root.get(criteria.getKey()), "%" + criteria.getValue() + "%");
        };
    }
}
