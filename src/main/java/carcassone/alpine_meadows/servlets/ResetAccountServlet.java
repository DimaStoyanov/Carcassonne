package carcassone.alpine_meadows.servlets;

import carcassone.alpine_meadows.db.datasets.Player;
import carcassone.alpine_meadows.db.datasets.PlayerReset;
import carcassone.alpine_meadows.db.repositories.PlayerRepository;
import carcassone.alpine_meadows.db.repositories.PlayerResetRepository;
import carcassone.alpine_meadows.utils.SendEmail;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Dmitrii Stoianov
 */

@Controller
public class ResetAccountServlet extends BaseServlet {


    private final static Logger log = Logger.getLogger(ResetAccountServlet.class);

    public ResetAccountServlet(ConfigurableApplicationContext context, Key key, JedisPool jedisPool) {
        super(context, jedisPool, key);
    }



    @RequestMapping(value = "reset_password", method = RequestMethod.POST)
    public String resetAccount(@RequestParam(name = "login") String login,
                               @RequestParam(name = "password") String password,
                               @RequestParam(name = "g-recaptcha-response") String captchaResponse,
                               HttpServletRequest request,
                               Model model) {

        if (checkLimitRequestsPerSecond(request, model, log) || !checkCaptcha(captchaResponse, model, log)) {
            return "error_page";
        }

        Player player = playerRepository.findTopByUsername(login);
        if (player == null) {
            player = playerRepository.findTopByEmail(login);
        }

        if (player == null) {
            model.addAttribute("type", "Bad request");
            model.addAttribute("description",
                    "Player with specified username/email is not registered");
            log.info("Incorrect username/email");
            return "error_page";
        }

        try (Jedis jedis = jedisPool.getResource()) {

            if (jedis.get(player.getEmail()) != null) {
                model.addAttribute("type", "Bad request");
                model.addAttribute("description", "A letter with instructions" +
                        "for resetting the password has already been sent to your mail. " +
                        "If you did not receive the letter, please wait 30 seconds and try again");
                log.info(String.format("Too much email requests from player %s",
                        player.getUsername()));
                return "error_page";
            }

            jedis.setex(player.getEmail(), 30, "");
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("target", "reset");
        payload.put("rand", new Random().nextLong());
        payload.put("password", argon2.hash(2, 65536, 1, password));

        // Json web token
        String compactJws = Jwts.builder()
                .setClaims(payload)
                .setSubject(player.getUsername())
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();

        PlayerReset playerReset = playerResetRepository.findOne(player.getId());
        if(playerReset == null){
            playerReset = new PlayerReset(player.getId(), compactJws);
         } else {
            playerReset.setToken(compactJws);
        }

        playerResetRepository.save(playerReset);
        SendEmail.sendLostPasswordLetter(player.getEmail(), player.getUsername(), compactJws);
        log.info(String.format("Player %s requested reset password", player.getUsername()));

        model.addAttribute("username", player.getUsername());
        model.addAttribute("email", player.getEmail());
        return "resetting";
    }
}
