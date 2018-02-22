package carcassone.alpine_meadows.db.repositories;

import carcassone.alpine_meadows.db.datasets.PlayerConfirmation;
import org.springframework.data.repository.CrudRepository;

/**
 * Created by Dmitrii Stoianov
 */


public interface PlayerConfirmationRepository extends CrudRepository<PlayerConfirmation, Long>{

    PlayerConfirmation findTopByToken(String token);

}
