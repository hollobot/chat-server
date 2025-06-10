package com.example.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Validated
public class UserUpdateInfoDto {

    @NotBlank
    private String userId;  // 用户id
    @NotBlank
    private String nickName; // 昵称
    @Min(value = 0)
    @Max(value = 1)
    private Integer joinType;  // 加入类型
    @Min(value = 0)
    @Max(value = 1)
    private Integer sex;   // 性别
    @NotBlank
    private String personalSignature;  // 个性签名
    @NotNull
    private String[] areaName;    // 地区

    private MultipartFile avatarFile;  // 头像文件对象
}
