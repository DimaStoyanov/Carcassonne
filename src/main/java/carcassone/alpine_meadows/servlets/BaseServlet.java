package carcassone.alpine_meadows.servlets;

import carcassone.alpine_meadows.db.pages.CaptchaVerify;
import carcassone.alpine_meadows.db.repositories.PlayerConfirmationRepository;
import carcassone.alpine_meadows.db.repositories.PlayerRepository;
import carcassone.alpine_meadows.db.repositories.PlayerResetRepository;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import org.apache.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.ui.Model;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.Key;
import java.util.Objects;

/**
 * Created by Dmitrii Stoianov
 */


public class BaseServlet {

    final PlayerRepository playerRepository;
    final PlayerResetRepository playerResetRepository;
    final PlayerConfirmationRepository playerConfirmationRepository;
    final JedisPool jedisPool;
    final Key key;


    static final Argon2 argon2 = Argon2Factory.create();
    private static final RestTemplate restTemplate = new RestTemplate();
    private static final String captchaSecret = "6LdIYToUAAAAABabCpd3LjNHfyNzDQyEStkuDpF4";

    protected BaseServlet(ConfigurableApplicationContext context, JedisPool jedisPool, Key key) {
        playerRepository = context.getBean(PlayerRepository.class);
        playerResetRepository = context.getBean(PlayerResetRepository.class);
        playerConfirmationRepository = context.getBean(PlayerConfirmationRepository.class);
        this.jedisPool = jedisPool;
        this.key = key;
    }

    // Allows no more than 2 requests per second from one user
    boolean checkLimitRequestsPerSecond(HttpServletRequest request, Model model, Logger log) {
        try (Jedis jedis = jedisPool.getResource()) {
            String countStr = jedis.get(request.getRemoteAddr());
            if (countStr == null) {
                jedis.psetex(request.getRemoteAddr(), 500L, "1");
                return false;
            } else {
                log.info("Too much requests per second");
                model.addAttribute("type", "Bad request");
                model.addAttribute("description", "Too much request per second");
                return true;
            }
        }

    }

    // Check signature of json web token and the equality of body values and specified arguments
    boolean checkToken(String token, String target, String subject, Model model, Logger log) {
        try {
            Jws<Claims> claims = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
            if(Objects.equals(target, claims.getBody().get("target", String.class))
                    && Objects.equals(subject, claims.getBody().getSubject()))
                return true;
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        log.info("Incorrect token");
        model.addAttribute("type", "Bad request");
        model.addAttribute("description", "Incorrect token");
        return false;
    }

    // Do response to check if user solved captcha
    boolean checkCaptcha(String captchaResponse, Model model, Logger log){
        String url = String.format("https://www.google.com/recaptcha/api/siteverify" +
                "?secret=%s&response=%s", captchaSecret, captchaResponse);
        CaptchaVerify captcha = restTemplate.getForObject(url, CaptchaVerify.class);

        System.out.println(captcha);

        if(!captcha.getSuccess()){
            log.info(captcha);
            model.addAttribute("type", "Bad request");
            model.addAttribute("description", "Incorrect captcha response");
            return false;
        }

        return true;

    }


}
