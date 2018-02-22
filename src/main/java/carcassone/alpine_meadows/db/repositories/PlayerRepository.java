package carcassone.alpine_meadows.db.repositories;

import carcassone.alpine_meadows.db.datasets.Player;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by Dmitrii Stoianov
 */


public interface PlayerRepository extends CrudRepository<Player, Long> {
    // Email is unique, so result is also unique, and to get player instead of array used top
    Player findTopByUsername(String username);

    Player findTopByEmail(String email);
}
