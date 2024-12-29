package com.example.demo.repository.specification;

import com.example.demo.model.UserEntity;
import com.example.demo.utils.Gender;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.jpa.domain.Specification;

public class UserSpec {
    public static Specification<UserEntity> hasFirstName(String firstName){
        return ((root, query, criteriaBuilder) -> criteriaBuilder.like(root.get("firstName"),"%"+firstName+"%"));
    }

    public static Specification<UserEntity> notEqualGender(Gender gender){
        return (((root, query, criteriaBuilder) -> criteriaBuilder.notEqual(root.get("gender"),gender)));
    }
}
