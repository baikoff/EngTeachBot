package org.engteachbot.reposiroty;

import org.engteachbot.model.StoryState;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StoryStateRepository extends JpaRepository<StoryState, Long> {
}
