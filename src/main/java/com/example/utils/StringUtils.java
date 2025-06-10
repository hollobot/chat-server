package com.example.utils;

import com.example.entity.enums.UserContactTypeEnum;
import org.springframework.util.DigestUtils;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.UUID;

public class StringUtils {


    // 时间戳格式
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMddHHmmssSSS");

    // 随机数生成器
    private static final Random RANDOM = new Random();


    /**
     * 生成用户id
     * @return
     */
    public static synchronized String generateUserId(){
       return UserContactTypeEnum.USER.getPrefix() +generateNumberString(11);
    }

    /**
     * 生成群聊id
     * @return
     */
    public static synchronized String generateGroupId(){
        return UserContactTypeEnum.GROUP.getPrefix()+generateNumberString(11);
    }



    /**
     * 生成token
     * @return
     */
    public static String generateToken (){
        return UUID.randomUUID().toString().replace("-","");
    }


    /**
     * 获取随机数字字符串
     * @param length
     * @return
     */
    public static  String generateNumberString(int length){
        // 获取当前时间戳
        String randomTimeString =RANDOM.nextInt(999999)+100000+DATE_FORMAT.format(new Date());
        if (length>randomTimeString.length()){
            String formatString = String.format("%-"+length+"s", randomTimeString);
            // 以零填充
            return formatString.replace(" ","0");
        }
        return randomTimeString.substring(0,length);
    }

    public static Boolean isEmpty(String string){
        if(string==null || string.equals("")){
            return true;
        }
        return false;
    }

    public static String cleanHtmlTag(String text){
        if(isEmpty(text)){
            return text;
        }
        text = text.replace("<","&lt");
        text = text.replace("\r\n","<br>");
        text = text.replace("\n","<br>");
        return text;
    }


    /**
     * 生成sessionId
     * @param userId
     * @param contactId
     * @return
     */
    public static String generateSessionId(String userId,String contactId){
        byte[] bytes = (userId + contactId).getBytes();
        Arrays.sort(bytes);
        return DigestUtils.md5DigestAsHex(bytes);
    }

    public static String generateSessionId(String groupId){
        return DigestUtils.md5DigestAsHex(groupId.getBytes());
    }

    /**
     * 获取文件扩展名
     * @param fileName
     * @return
     */
    public static String getFileSuffix(String fileName){
        return fileName.substring(fileName.lastIndexOf('.'));
    }

    /**
     * 判断是否是number
     * @param fileId
     * @return
     */
    public static Boolean isNumber(String fileId){
        if(isEmpty(fileId)){
            return false;
        }
        String fileIdNumber = "^[0-9]+$";
        return fileId.matches(fileIdNumber);
    }

}
