package com.example.entity.enums;

public enum ExceptionCodeEnum {

    CODE_400(400, "请求失败，请规范操作"),
    CODE_401(401, "未授权,请重新登录"),
    CODE_402(402, "授权超时,请重新登录"),
    CODE_403(403, "无访问权限"),
    CODE_404(404, "地址不存在"),
    CODE_500(500, "服务器异常请联系管理员"),
    CODE_601(600, "没有权限下载该文件"),
    CODE_602(602, "文件不存在"),
    CODE_901(901,"您不是对方好友，请先向好友添加申请"),
    CODE_902(902,"您已经不在群聊，请重新申请进入群聊");


    private Integer code;
    private String message;

    ExceptionCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    public Integer getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }


    public ExceptionCodeEnum setMessage(String message) {
        this.message = message;
        return this;
    }
}
