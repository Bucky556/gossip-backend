package code.uz.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "sms_token")
@Getter
@Setter
public class SmsTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Integer id;

    @Column(name = "token", columnDefinition = "TEXT")
    private String token;

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @Column(name = "exp_date")
    private LocalDateTime expDate;
}
