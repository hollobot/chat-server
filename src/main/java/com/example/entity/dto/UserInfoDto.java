package com.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class UserInfoDto{
    private String userId;  // 用户id
//    @Email
    private String email;    // 用户邮箱
    private String nickName; // 昵称
    private Integer joinType;  // 加入类型
    private Integer sex;   // 性别
    @NotBlank
    private String password;  // 密码
    private String personalSignature;  // 个性签名
    private Integer status;  // 状态

    @NotBlank
    private String codeKey; //验证码key
    @NotBlank
    private String checkCode; //验证码
}
