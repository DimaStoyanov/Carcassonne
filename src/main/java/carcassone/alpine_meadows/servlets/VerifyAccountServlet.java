package carcassone.alpine_meadows.servlets;

import carcassone.alpine_meadows.db.datasets.Player;
import carcassone.alpine_meadows.db.repositories.PlayerRepository;
import org.apache.log4j.Logger;
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

    public VerifyAccountServlet(PlayerRepository playerRepository, JedisPool jedisPool, Key key) {
        super(playerRepository, jedisPool, key);
    }

    private static final Logger log = Logger.getLogger(VerifyAccountServlet.class);


    @RequestMapping(value = "/verify")
    public String verify(@RequestParam(name = "token") String token, Model model, HttpServletRequest request) {

        if (checkLimitRequestsPerSecond(request, model, log)) {
            return "error_page";
        }


        Player player = playerRepository.findByConfirmationToken(token);
        if (player == null) {
            log.info("incorrect token");
            model.addAttribute("type", "Bad request");
            model.addAttribute("description", "Incorrect token");
            return "error_page";
        }


        if (!checkToken(token, "sign_up", player.getUsername(), model, log)) {
            log.info("incorrect token");
            model.addAttribute("type", "Bad request");
            model.addAttribute("description", "Target or username in token is incorrect");
            return "error_page";
        }

        player.setEmailConfirmed(true);
        player.setConfirmationToken(null);
        playerRepository.save(player);
        log.info(String.format("Player %s verified account", player.getUsername()));

        model.addAttribute("username", player.getUsername());
        model.addAttribute("email", player.getEmail());
        return "verify";
    }

}
