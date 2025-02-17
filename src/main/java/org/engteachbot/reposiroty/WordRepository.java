package org.engteachbot.reposiroty;

import org.engteachbot.model.WordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WordRepository extends JpaRepository<WordEntity, Long> {
    Optional<WordEntity> findByWord(String word);
}
