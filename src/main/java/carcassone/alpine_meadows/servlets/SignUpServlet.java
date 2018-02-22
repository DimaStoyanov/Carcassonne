package carcassone.alpine_meadows.servlets;

import carcassone.alpine_meadows.db.datasets.Player;
import carcassone.alpine_meadows.db.datasets.PlayerConfirmation;
import carcassone.alpine_meadows.db.repositories.PlayerConfirmationRepository;
import carcassone.alpine_meadows.db.repositories.PlayerRepository;
import carcassone.alpine_meadows.utils.SendEmail;
import de.mkammerer.argon2.Argon2;
import de.mkammerer.argon2.Argon2Factory;
import io.jsonwebtoken.*;
import org.apache.log4j.Logger;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Created by Dmitrii Stoianov
 */

@Controller
public class SignUpServlet extends BaseServlet {


    private static final Logger log = Logger.getLogger(SignUpServlet.class);


    public SignUpServlet(Key key, ConfigurableApplicationContext context, JedisPool jedisPool) {
        super(context, jedisPool, key);
    }

    // Just for test
    @RequestMapping(value = "/truncate")
    public String truncateTable() {
        playerConfirmationRepository.deleteAll();
        playerRepository.deleteAll();
        playerResetRepository.deleteAll();
        return "error";
    }

    @RequestMapping(value = "/signing_up", method = RequestMethod.POST)
    public String signUpPlayer(@RequestParam(name = "username") String username,
                               @RequestParam(name = "password") String password,
                               @RequestParam(name = "email") String email,
                               @RequestParam(name = "g-recaptcha-response") String captchaResponse,
                               HttpServletRequest request,
                               Model model) {

        if (checkLimitRequestsPerSecond(request, model, log) || !checkCaptcha(captchaResponse, model, log)) {
            return "error_page";
        }

        if (playerRepository.findTopByUsername(username) != null) {
            model.addAttribute("type", "Bad request");
            model.addAttribute("description", "Not unique username");
            log.info("Not unique username");
            return "error_page";
        }


        if (playerRepository.findTopByEmail(email) != null) {
            model.addAttribute("type", "Bad request");
            model.addAttribute("description", "Not unique email address");
            log.info("Not unique email address");
            return "error_page";
        }

        String passwordHash = argon2.hash(2, 65536, 1, password);
        if (!argon2.verify(passwordHash, password)) {
            log.error(String.format("Argon2 generated hash error: " +
                    "password=%s passwordHash=%s", password, passwordHash));
            model.addAttribute("type", "Internal Server Error");
            model.addAttribute("description", "Error occurred while generating hash");
            return "error_page";
        }


        Map<String, Object> payload = new HashMap<>();
        payload.put("target", "sign_up");
        payload.put("rand", new Random().nextLong());

        // Use javascript web token
        String compactJws = Jwts.builder()
                .setClaims(payload)
                .setSubject(username)
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();


        SendEmail.sendSignUpLetter(email, username, compactJws);
        Player player = playerRepository.save(new Player(username, passwordHash, email));
        playerConfirmationRepository.save(new PlayerConfirmation(player.getId(), compactJws));
        log.info(String.format("Player %s signed up", username));

        model.addAttribute("username", username);
        model.addAttribute("email", email);
        return "signed_up";
    }
}
