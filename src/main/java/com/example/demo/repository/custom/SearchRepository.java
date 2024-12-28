package com.example.demo.repository.custom;

import com.example.demo.dto.response.PageResponse;
import com.example.demo.dto.response.UserDetailResponse;
import com.example.demo.model.AddressEntity;
import com.example.demo.model.UserEntity;
import com.example.demo.repository.criteria.SearchCriteria;
import com.example.demo.repository.criteria.UserSearchCriteriaConsumer;
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

    public PageResponse<?> advanceSearchUser(int pageNo, int pageSize, String sortBy, String... search) {
        List<SearchCriteria> criteriaList = new ArrayList<>();
        if (search != null) {
            for (String item : search) {
                Pattern pattern = Pattern.compile("(\\w+?)(>|<|:)(.*)");
                Matcher matcher = pattern.matcher(item);
                if (matcher.find()) {
                    criteriaList.add(new SearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3)));
                }
            }
        }

        List<UserEntity> users = getUsers(criteriaList, pageNo, pageSize, sortBy, search);

        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .contents(users)
//                .to
                .build();
    }


    public PageResponse<?> advanceSearchJoinByCriteria(int pageNo, int pageSize, String sortBy, String address, String... search) {
        List<SearchCriteria> criteriaList = new ArrayList<>();
        if (search != null) {
            for (String item : search) {
                Pattern pattern = Pattern.compile("(\\w+?)(>|<|:)(.*)");
                Matcher matcher = pattern.matcher(item);
                if (matcher.find()) {
                    criteriaList.add(new SearchCriteria(matcher.group(1), matcher.group(2), matcher.group(3)));
                }
            }
        }

        List<UserEntity> users = getUsersByAddress(criteriaList, pageNo, pageSize, sortBy, address, search);
        Long totalElements = getTotalElements(criteriaList,address);
        return PageResponse.builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .totalElements(totalElements)
                .contents(users)
                .build();
    }


    private List<UserEntity> getUsers(List<SearchCriteria> criteriaList, int pageNo, int pageSize, String sortBy, String... search) {
        CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> query = criteriaBuilder.createQuery(UserEntity.class);
        Root<UserEntity> root = query.from(UserEntity.class);

        Predicate predicate = criteriaBuilder.conjunction();
        UserSearchCriteriaConsumer consumer = new UserSearchCriteriaConsumer(criteriaBuilder, predicate, root);

        criteriaList.forEach(consumer);
        predicate = consumer.getPredicate();

        query.where(predicate);

        if (StringUtils.hasLength(sortBy)) {
            Pattern pattern = Pattern.compile("(\\w+?)(:)(asc|desc)");
            Matcher matcher = pattern.matcher(sortBy);
            if (matcher.find()) {
                String columnName = matcher.group(1);
                if (matcher.group(3).equalsIgnoreCase("desc")) {
                    query.orderBy(criteriaBuilder.desc(root.get(columnName)));
                } else {
                    query.orderBy(criteriaBuilder.asc(root.get(columnName)));
                }
            }
        }
        return entityManager.createQuery(query).setFirstResult(pageNo).setMaxResults(pageSize).getResultList();
    }


    private List<UserEntity> getUsersByAddress(List<SearchCriteria> criteriaList, int pageNo, int pageSize, String sortBy, String address, String... search) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<UserEntity> query = builder.createQuery(UserEntity.class);
        Root<UserEntity> root = query.from(UserEntity.class);

        Predicate predicate = builder.conjunction();
        UserSearchCriteriaConsumer consumer = new UserSearchCriteriaConsumer(builder,predicate,root);

        if (StringUtils.hasLength(address)) {
            Join<UserEntity, AddressEntity> addressUserJoin = root.join("addresses");
            Predicate addressJoinPredicate = builder.like(addressUserJoin.get("city"), "%" + address + "%");
            query.where(predicate,addressJoinPredicate);
        }else{
            criteriaList.forEach(consumer);
            predicate = consumer.getPredicate();
            query.where(predicate);
        }

        if(StringUtils.hasLength(sortBy)){
            Pattern pattern = Pattern.compile("(\\w+?)(:)(asc|desc)");
            Matcher matcher = pattern.matcher(sortBy);
            if(matcher.find()){
                String columnName = matcher.group(1);
                if(matcher.group(3).equalsIgnoreCase("asc")){
                    query.orderBy(builder.asc(root.get(columnName)));
                }else{
                    query.orderBy(builder.desc(root.get(columnName)));
                }
            }
        }
        return entityManager.createQuery(query).setFirstResult(pageNo).setMaxResults(pageSize).getResultList();
    }

    private Long getTotalElements(List<SearchCriteria> criteriaList, String address) {
        CriteriaBuilder builder = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = builder.createQuery(Long.class);
        Root<UserEntity> root = query.from(UserEntity.class);

        Predicate predicate = builder.conjunction();
        UserSearchCriteriaConsumer consumer = new UserSearchCriteriaConsumer(builder,predicate,root);

        if (StringUtils.hasLength(address)) {
            Join<UserEntity, AddressEntity> addressUserJoin = root.join("addresses");
            Predicate addressJoinPredicate = builder.like(addressUserJoin.get("city"), "%" + address + "%");
            query.select(builder.count(root));
            query.where(predicate,addressJoinPredicate);
        }else{
            criteriaList.forEach(consumer);
            predicate = consumer.getPredicate();
            query.select(builder.count(root));
            query.where(predicate);
        }


        return entityManager.createQuery(query).getSingleResult();
    }

}
