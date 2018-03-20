package carcassone.alpine_meadows.servlets;

import carcassone.alpine_meadows.db.datasets.Player;
import carcassone.alpine_meadows.db.repositories.PlayerRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.log4j.Logger;
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
public class SignInServlet extends BaseServlet {

    public SignInServlet(PlayerRepository playerRepository, JedisPool jedisPool, Key key) {
        super(playerRepository, jedisPool, key);
    }

    private static final Logger log = Logger.getLogger(SignInServlet.class);


    @RequestMapping(value = "/signing_in", method = RequestMethod.POST)
    public String singInPlayer(@RequestParam(name = "username") String username,
                               @RequestParam(name = "password") String password,
                               @RequestParam(name = "g-recaptcha-response") String captchaResponse,
                               HttpServletRequest request,
                               Model model) {

        if (checkLimitRequestsPerSecond(request, model, log) || !checkCaptcha(captchaResponse, model, log)) {
            return "error_page";
        }

        Player player = playerRepository.findByUsername(username);

        if (player == null || !argon2.verify(player.getPassword(), password)) {
            log.info("Incorrect username/password");
            model.addAttribute("type", "Bad request");
            model.addAttribute("description", "Incorrect username/password");
            return "error_page";
        }

        if (!player.isEmailConfirmed()) {
            log.info("Email does not confirmed");
            model.addAttribute("type", "Bad request");
            model.addAttribute("description", "Email address does not confirmed");
            return "error_page";
        }

        Map<String, Object> payload = new HashMap<>();
        payload.put("target", "sign_in");
        payload.put("rand", new Random().nextLong());

        String compactJws = Jwts.builder()
                .setClaims(payload)
                .setSubject(username)
                .signWith(SignatureAlgorithm.HS512, key)
                .compact();

        try (Jedis jedis = jedisPool.getResource()) {
            // Delete previous token if it's present
            String prevToken = jedis.get(username);
            if (prevToken != null) {
                jedis.del(prevToken);
            }


            // To have ability to delete token of user
            jedis.setex(username, 60 * 60 * 24, compactJws);
            // To have ability fast check if token is valid
            jedis.setex(compactJws, 60 * 60 * 24, username);
        }


        log.info(String.format("Player %s signed in", username));

        model.addAttribute("username", username);
        model.addAttribute("token", compactJws);
        return "signed_in";
    }
}
