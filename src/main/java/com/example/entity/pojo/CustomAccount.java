package com.example.entity.pojo;


import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import javax.validation.constraints.Email;
import javax.validation.constraints.Size;

@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@TableName("`custom_account`")
public class CustomAccount {
    @TableId(type = IdType.ASSIGN_ID)
    private String customAccountId; // 唯一id
    @Email(message = "邮箱不规范")
    private String email;            // 用户邮箱
    @Size(min = 11, max = 11, message = "UUID为11位")
    private String userId;           // 自定义账户
    private Integer status;          // 自定义状态：0已被使用，1未被使用
}
