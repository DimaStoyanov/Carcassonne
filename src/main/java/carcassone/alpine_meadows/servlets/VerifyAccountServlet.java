package carcassone.alpine_meadows.servlets;

import carcassone.alpine_meadows.db.datasets.Player;
import carcassone.alpine_meadows.db.datasets.PlayerConfirmation;
import carcassone.alpine_meadows.db.repositories.PlayerConfirmationRepository;
import carcassone.alpine_meadows.db.repositories.PlayerRepository;
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
import org.springframework.web.bind.annotation.RequestParam;
import redis.clients.jedis.JedisPool;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;

/**
 * Created by Dmitrii Stoianov
 */

@Controller
public class VerifyAccountServlet extends BaseServlet {

    public VerifyAccountServlet(ConfigurableApplicationContext context, JedisPool jedisPool, Key key){
        super(context, jedisPool, key);
    }

    private static final Logger log = Logger.getLogger(VerifyAccountServlet.class);


    @RequestMapping(value = "/verify")
    public String verify(@RequestParam(name = "token") String token, Model model, HttpServletRequest request) {

        if(checkLimitRequestsPerSecond(request, model, log)){
            return "error_page";
        }


        PlayerConfirmation playerConfirmation = playerConfirmationRepository.findTopByToken(token);


        if (playerConfirmation == null) {
            log.info("incorrect token");
            model.addAttribute("type", "Bad request");
            model.addAttribute("description", "Incorrect token");
            return "error_page";
        }


        Player player = playerRepository.findOne(playerConfirmation.getId());
        playerConfirmationRepository.delete(playerConfirmation);

        if (player == null) {
            log.error(String.format("Player  exist in db \'confirmation\', but doesn't exist in " +
                    "db \'players\'"));
            model.addAttribute("type", "Internal Server Error");
            model.addAttribute("description", "Player does not exist");
            return "error_page";
        }

        if(!checkToken(token, "sign_up", player.getUsername(), model, log)){
            return "error_page";
        }

        player.setEmailConfirmed(true);
        playerRepository.save(player);
        log.info(String.format("Player %s verified account", player.getUsername()));

        model.addAttribute("username", player.getUsername());
        model.addAttribute("email", player.getEmail());
        return "verify";
    }

}
