package carcassone.alpine_meadows.servlets;

import carcassone.alpine_meadows.db.datasets.Player;
import carcassone.alpine_meadows.db.repositories.PlayerRepository;
import carcassone.alpine_meadows.utils.SendEmail;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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


    public SignUpServlet(PlayerRepository playerRepository, JedisPool jedisPool, Key key) {
        super(playerRepository, jedisPool, key);
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

        if (playerRepository.findByUsername(username) != null) {
            model.addAttribute("type", "Bad request");
            model.addAttribute("description", "Not unique username");
            log.info("Not unique username");
            return "error_page";
        }


        if (playerRepository.findByEmail(email) != null) {
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
        playerRepository.save(new Player(username, passwordHash, email, compactJws));
        log.info(String.format("Player %s signed up", username));

        model.addAttribute("username", username);
        model.addAttribute("email", email);
        return "signed_up";
    }
}
