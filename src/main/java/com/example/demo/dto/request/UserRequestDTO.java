package com.example.demo.dto.request;

import com.example.demo.model.AddressEntity;
import com.example.demo.utils.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.validation.annotation.Validated;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Getter
public class UserRequestDTO implements Serializable {
    @NotBlank(message = "firstName must not be blank")
    private String firstName;

    @NotNull(message = "lastName must not be blank")
    private String lastName;
    @Email(message = "email invalid format")
    private String email;

    //    @Pattern(regexp = "^\\d{10}$",message = "phone invalid format")
    @PhoneNumber
    private String phone;

    @NotNull(message = "dateOfBirth must be not null")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = "MM/dd/yyyy")
    private Date dateOfBirth;

    @NotEmpty(message = "addresses is not empty")
    private Set<AddressDTO> addresses;

    @EnumPattern(regexp = "^ACTIVE|UNACTIVE|NONE$",name = "status")
    private UserStatus status;

    @GenderSubset(anyOf = {Gender.MALE,Gender.FEMALE,Gender.OTHER})
    private Gender gender;

    @NotNull(message = "user type not null")
    @EnumValue(name = "type", enumClass = UserType.class)
    private String userType;

    @NotNull(message = "username must be not null")
    private String username;

    @NotNull(message = "password must be not null")
    private String password;

    public UserRequestDTO(String firstName, String lastName, String email, String phone) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
    }


}
