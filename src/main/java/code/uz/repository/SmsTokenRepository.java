package code.uz.repository;

import code.uz.entity.SmsTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Transactional
public interface SmsTokenRepository extends JpaRepository<SmsTokenEntity, Integer> {
    Optional<SmsTokenEntity> findTopByOrderByIdDesc();
}
