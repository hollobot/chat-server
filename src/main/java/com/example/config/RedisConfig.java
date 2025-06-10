package com.example.config;
import com.example.entity.enums.ExceptionCodeEnum;
import com.example.handler.CustomException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.redis.host}")
    public String redisHost;

    @Value("${spring.redis.port}")
    public String redisPort;

    @Value("${spring.redis.password}")
    public String redisPassword;

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 使用 Jackson2JsonRedisSerializer 来序列化/反序列化
        RedisSerializer<String> keySer   = new StringRedisSerializer();
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);

        //Redis 键的序列化器配置了 StringRedisSerializer，它将 String 类型的键序列化为字符串格式。
        template.setKeySerializer(keySer);
        //为 Redis 值的序列化器设置了 Jackson2JsonRedisSerializer，这样存储到 Redis 中的值将会被序列化为 JSON 格式。
        template.setValueSerializer(serializer);
        /*下面是hash字节的序列化配置*/
        template.setHashKeySerializer(keySer);
        template.setHashValueSerializer(serializer);

        //afterPropertiesSet() 是 InitializingBean 接口中的一个方法，
        // Spring 会在 RedisTemplate 配置完成之后调用它，确保所有属性都已经设置好。
        template.afterPropertiesSet();
        return template;
    }

    /**
     * 指定bean销毁调用这个方法进行销毁 destroyMethod = "shutdown"
     * @return
     */
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonClient(){
        RedissonClient redissonClient = null;
        try{
            Config config = new Config();
            config.setCodec(new JsonJacksonCodec()); // 使用JSON序列化与接受的数据对象需要序列化相同，否则监听不到
            config.useSingleServer().setAddress("redis://"+redisHost+":"+redisPort).setPassword(redisPassword);
            redissonClient = Redisson.create(config);
        }catch (Exception e){
            log.error("redisson连接失败，请检查redisson初始化配置bean\n{}",e);
            throw new CustomException(ExceptionCodeEnum.CODE_500);
        }
        return redissonClient;
    }
}
