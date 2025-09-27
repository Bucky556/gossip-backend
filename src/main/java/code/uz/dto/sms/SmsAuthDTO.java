package code.uz.dto.sms;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SmsAuthDTO {
    private String email; // provider-da email suragani uchun
    private String password;
}
