package com.wjb.annotations;

import com.wjb.constraints.VlogConstraints;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)//注解被保留在runtime
@Constraint(validatedBy = VlogConstraints.class)
public @interface VlogIdValid {

    String message() default "vlogId不存在";

    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
