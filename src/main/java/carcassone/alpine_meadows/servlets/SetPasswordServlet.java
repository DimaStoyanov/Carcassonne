package carcassone.alpine_meadows.servlets;

import carcassone.alpine_meadows.db.datasets.Player;
import carcassone.alpine_meadows.db.datasets.PlayerReset;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;

/**
 * Created by Dmitrii Stoianov
 */

@Controller
public class SetPasswordServlet extends BaseServlet {


    private static final Logger log = Logger.getLogger(SetPasswordServlet.class);

    public SetPasswordServlet(ConfigurableApplicationContext context, Key key, JedisPool jedisPool) {
        super(context, jedisPool, key);
    }

    @RequestMapping(value = "/set_password")
    public String setPassword(@RequestParam(name = "token") String token, Model model,
                              HttpServletRequest request) {

        if(checkLimitRequestsPerSecond(request, model, log)){
            return "error_page";
        }

        PlayerReset playerReset = playerResetRepository.findTopByToken(token);

        if (playerReset == null) {
            log.info("Incorrect token, playerReset not found");
            model.addAttribute("type", "Bad request");
            model.addAttribute("description", "Incorrect token");
            return "error_page";
        }

        Player player = playerRepository.findOne(playerReset.getId());
        playerResetRepository.delete(playerReset);


        if (player == null) {
            log.error("Token is correct, but player does not exist");
            model.addAttribute("type", "Internal Server Error");
            model.addAttribute("description", "Player does not registered");
            return "error_page";
        }

        if(!checkToken(token, "reset", player.getUsername(), model, log)){
            return "error_page";
        }

        if (!player.isEmailConfirmed()) {
            log.info("Account does not verified");
            model.addAttribute("type", "Bad request");
            model.addAttribute("description", "Account does not verified. " +
                    "First of all verify your email address and then " +
                    "you can reset password.");
            return "error_page";
        }

        String password;
        try{
            Jws<Claims> claimsJws = Jwts.parser().setSigningKey(key).parseClaimsJws(token);
            password = claimsJws.getBody().get("password", String.class);
        } catch (SignatureException e){
            log.error(e);
            model.addAttribute("type", "Bad request");
            model.addAttribute("description", "Self-signed token");
            return "error_page";
        }

        player.setPassword(password);
        playerRepository.save(player);

        log.info(String.format("Player %s change password", player.getUsername()));
        model.addAttribute("username", player.getUsername());
        model.addAttribute("email", player.getEmail());
        return "changed_password";

    }
}
