package carcassone.alpine_meadows.db.repositories;

import carcassone.alpine_meadows.db.datasets.Player;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by Dmitrii Stoianov
 */


public interface PlayerRepository extends CrudRepository<Player, Long> {
    Player findByUsername(String username);

    Player findByEmail(String email);

    Player findByConfirmationToken(String token);

    Player findByResetToken(String token);
}
