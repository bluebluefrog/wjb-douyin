package com.wjb.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class LoginWithAccountBO {

    //使用hibernate做校验 message提示
    @NotBlank(message = "username不能为空")
    private String username;

    @NotBlank(message = "密码不能为空")
    private String password;


}
