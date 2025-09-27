package code.uz.entity;


import code.uz.enums.SmsType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "sms_history")
@Getter
@Setter
public class SmsHistoryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    @Column(name = "phone")
    private String phone;
    @Column(name = "message", columnDefinition = "TEXT")
    private String message;
    @Column(name = "code")
    private String code;
    @Column(name = "send_time")
    private LocalDateTime sendTime;
    @Column(name = "sms_type")
    @Enumerated(EnumType.STRING)
    private SmsType smsType;
    @Column(name = "attempt_count")
    private Integer attemptCount = 0;

}
