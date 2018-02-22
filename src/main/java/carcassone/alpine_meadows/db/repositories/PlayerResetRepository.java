package carcassone.alpine_meadows.db.repositories;

import carcassone.alpine_meadows.db.datasets.PlayerReset;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by Dmitrii Stoianov
 */


public interface PlayerResetRepository extends CrudRepository<PlayerReset, Long> {
    PlayerReset findTopByToken(String token);
}
