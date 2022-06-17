package com.wjb.constraints;


import com.wjb.annotations.VlogIdValid;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

public class VlogConstraints implements ConstraintValidator<VlogIdValid,Object> {

    public void initialize(VlogIdValid constraintAnnotation) {

    }

    public boolean isValid(Object value, ConstraintValidatorContext context) {

//        Vlog vlog = vlogService.queryById((String) value);
//        if (vlog == null) {
//            return false;
//        }
        return true;
    }
}
