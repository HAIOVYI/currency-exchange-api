package projects.currencyexchangeapi.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import projects.currencyexchangeapi.entity.UserEntity;

public interface UserRepository extends JpaRepository<UserEntity, Long> {

    @EntityGraph(attributePaths = {"roles"})
    Optional<UserEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
