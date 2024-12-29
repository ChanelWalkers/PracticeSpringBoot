package com.example.demo.repository.specification;

import com.example.demo.model.UserEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.jpa.domain.Specification;

@Getter
@AllArgsConstructor
public class UserSpecification implements Specification<UserEntity> {
    private SpecSearchCriteria criteria;
    @Override
    public Predicate toPredicate(Root<UserEntity> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        return switch (criteria.getOperation()){
            case EQUALITY -> criteriaBuilder.equal(root.get(criteria.getKey()),criteria.getValue());
            case NEGATION -> criteriaBuilder.notEqual(root.get(criteria.getKey()),criteria.getValue());
            case GREATER_THAN -> criteriaBuilder.greaterThan(root.get(criteria.getKey()),criteria.getValue().toString());
            case LESS_THAN -> criteriaBuilder.lessThan(root.get(criteria.getKey()),criteria.getValue().toString());
            case LIKE -> criteriaBuilder.like(root.get(criteria.getKey()),"%"+criteria.getValue().toString()+"%");
            case STARTS_WITH -> criteriaBuilder.like(root.get(criteria.getKey()),criteria.getValue().toString()+"%");
            case ENDS_WITH -> criteriaBuilder.like(root.get(criteria.getKey()),"%"+criteria.getValue().toString());
            case CONTAINS -> criteriaBuilder.like(root.get(criteria.getKey()),"%"+criteria.getValue()+"%");
        };
    }
}
