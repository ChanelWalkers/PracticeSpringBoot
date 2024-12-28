package com.example.demo.repository.criteria;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.function.Consumer;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Slf4j
public class UserSearchCriteriaConsumer implements Consumer<SearchCriteria> {
    private CriteriaBuilder builder;
    private Predicate predicate;
    private Root root;


    @Override
    public void accept(SearchCriteria param) {
        if (param.getOperation().equals(">")) {
            predicate = builder.and(predicate, builder.greaterThanOrEqualTo(root.get(param.getKey()), param.getValue().toString()));
        } else if (param.getOperation().equals("<")) {
            predicate = builder.and(predicate, builder.lessThanOrEqualTo(root.get(param.getKey()), param.getValue().toString()));
        } else {
            if (root.get(param.getKey()).getJavaType() == String.class) {
                predicate = builder.and(predicate, builder.like(root.get(param.getKey()), "%" + param.getValue().toString() + "%"));
            } else if(root.get(param.getKey()).getJavaType() == Date.class){
                    Date dateValue = converStringToDate(param.getValue().toString());
                    predicate = builder.and(predicate, builder.equal(root.get(param.getKey()),dateValue));
            }
            else {
                predicate = builder.and(predicate, builder.equal(root.get(param.getKey()), param.getValue().toString()));
            }
        }
    }

    private Date converStringToDate(String date){
        try {
            return Date.valueOf(date);
        }catch (Exception ex){
            log.error("Error: {}",ex.getMessage(),ex.getCause());
        }
        return null;
    }
}
