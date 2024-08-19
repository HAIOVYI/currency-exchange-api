package projects.currencyexchangeapi.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import projects.currencyexchangeapi.entity.RoleEntity;

public interface RoleRepository extends JpaRepository<RoleEntity, Long> {

    RoleEntity findByType(RoleEntity.RoleType type);
}
