package com.example.entity.dto;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
public class TokenUserInfoDto implements Serializable {

    private String email;
    private String token;
    private String userId;  // 用户id
    private String nickName; // 昵称
    private Boolean isAdmin;

}
