package com.wjb.constraints;


import com.wjb.annotations.VlogIdValid;


import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class VlogConstraints implements ConstraintValidator<VlogIdValid,Object> {

    public void initialize(VlogIdValid constraintAnnotation) {

    }

    public boolean isValid(Object value, ConstraintValidatorContext context) {
        System.out.println(value);
        return true;
    }
}
