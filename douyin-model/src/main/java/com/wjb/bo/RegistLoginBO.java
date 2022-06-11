package com.wjb.bo;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class RegistLoginBO {

    //使用hibernate做校验 message提示
    @NotBlank(message = "手机号不能为空")
    @Length(min = 9, max = 10, message = "手机号最小为9 最大为10")
    private String mobile;
    @NotBlank(message = "验证码不能为空")
    private String smsCode;

}
