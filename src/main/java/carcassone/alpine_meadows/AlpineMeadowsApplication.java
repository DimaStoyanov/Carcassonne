package carcassone.alpine_meadows;

import io.jsonwebtoken.impl.crypto.MacProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.Key;

@SpringBootApplication
@EnableAutoConfiguration
@Configuration
@ComponentScan
public class AlpineMeadowsApplication {

    public static void main(String[] args) {
        SpringApplication.run(AlpineMeadowsApplication.class, args);
    }

    @Bean
    Key key() {
        return MacProvider.generateKey();
    }

    @Bean
    public static JedisPool jedisPool() throws URISyntaxException {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(10);
        poolConfig.setMaxIdle(5);
        poolConfig.setMinIdle(1);
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        String envVariable = System.getenv("REDIS_URL");
        JedisPool pool;
        if (envVariable == null)
            pool = new JedisPool(poolConfig, "localhost");
        else
            pool = new JedisPool(poolConfig, new URI(envVariable));
        return pool;
    }


}
