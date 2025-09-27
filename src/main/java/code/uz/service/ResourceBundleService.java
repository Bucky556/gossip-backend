package code.uz.service;

import code.uz.enums.AppLanguage;
import lombok.RequiredArgsConstructor;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class ResourceBundleService {
    private final ResourceBundleMessageSource messageSource;

    public String getMessage(String key, AppLanguage language) {
        return messageSource.getMessage(key, null, new Locale(language.name()));
    }
}
