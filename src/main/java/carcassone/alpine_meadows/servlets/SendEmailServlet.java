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
public class SendEmailServlet extends BaseServlet {


    private static final Logger log = Logger.getLogger(SetPasswordServlet.class);

    public SendEmailServlet(PlayerRepository playerRepository, JedisPool jedisPool, Key key) {
        super(playerRepository, jedisPool, key);
    }

    @RequestMapping(value = "/send_email_req", method = RequestMethod.POST)
    public String sendEmail(@RequestParam(name = "login") String login,
                            @RequestParam(name = "type") String type,
                            @RequestParam(name = "password", required = false, defaultValue = "") String password,
                            @RequestParam(name = "g-recaptcha-response") String captchaResponse,
                            HttpServletRequest request,
                            Model model) {

        if (checkLimitRequestsPerSecond(request, model, log) || !checkCaptcha(captchaResponse, model, log)) {
            return "error_page";
        }

        Player player = playerRepository.findByUsername(login);
        if (player == null) {
            player = playerRepository.findByEmail(login);
        }

        if (player == null) {
            log.info("incorrect username/email");
            model.addAttribute("type", "Bad request");
            model.addAttribute("description", "Incorrect username/email address");
            return "error_page";
        }


        try (Jedis jedis = jedisPool.getResource()) {
            if (jedis.get(player.getEmail()) != null) {
                log.info("Too much email requests");
                model.addAttribute("type", "Bad request");
                model.addAttribute("description", "Too much requests");
                return "error_page";
            }
            jedis.setex(player.getEmail(), 10, "");
        }

        switch (type) {
            case "sign_up":
                if (player.isEmailConfirmed()) {
                    log.info(String.format("Player %s already verified account",
                            player.getUsername()));
                    model.addAttribute("type", "Bad request");
                    model.addAttribute("description", "Account has already been verified");
                    return "error_page";
                }


                Map<String, Object> payload = new HashMap<>();
                payload.put("target", "sign_up");
                payload.put("rand", new Random().nextLong());

                String compactJws = Jwts.builder()
                        .setClaims(payload)
                        .setSubject(player.getUsername())
                        .signWith(SignatureAlgorithm.HS512, key)
                        .compact();

                player.setConfirmationToken(compactJws);
                SendEmail.sendSignUpLetter(player.getEmail(), player.getUsername(), compactJws);
                playerRepository.save(player);

                log.info(String.format("Player %s re-requested sign up letter", player.getUsername()));
                break;

            case "reset":

                if (!player.isEmailConfirmed()) {
                    log.info(String.format("Player %s does not verified account yet", player.getUsername()));
                    model.addAttribute("type", "Bad request");
                    model.addAttribute("description", "First of all verify your email address and then "
                            + "you can reset password.");
                    return "error_page";
                }

                if (password.equals("")) {
                    log.info("missing parameter password");
                    model.addAttribute("type", "Bad request");
                    model.addAttribute("description", "Missing required parameter password");
                    return "error_page";
                }


                payload = new HashMap<>();
                payload.put("target", "reset");
                payload.put("rand", new Random().nextLong());
                payload.put("password", argon2.hash(2, 65536, 1, password));

                compactJws = Jwts.builder()
                        .setClaims(payload)
                        .setSubject(player.getUsername())
                        .signWith(SignatureAlgorithm.HS512, key)
                        .compact();

                player.setResetToken(compactJws);
                SendEmail.sendLostPasswordLetter(player.getEmail(), player.getUsername(), compactJws);
                playerRepository.save(player);
                log.info(String.format("Player %s requested reset account", player.getUsername()));
                break;

            default:
                log.error(String.format("Unknown type %s", type));
                model.addAttribute("type", "Internal Server Error");
                model.addAttribute("description", String.format("Unknown type %s", type));
                return "error_page";
        }

        model.addAttribute("username", player.getUsername());
        model.addAttribute("email", player.getEmail());
        return "resetting";
    }


}
