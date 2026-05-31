package com.inventory.security;

import com.inventory.model.User;
import com.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtils {

    private final UserRepository userRepository;

    /**
     * Повертає company_id поточного авторизованого користувача.
     * Використовується у всіх сервісах для фільтрації даних.
     */
    public Long getCurrentCompanyId() {
        String username = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Користувача не знайдено: " + username));

        if (user.getCompanyId() == null) {
            throw new RuntimeException(
                "Користувач '" + username + "' не прив'язаний до жодної компанії");
        }

        return user.getCompanyId();
    }

    /**
     * Повертає поточного авторизованого User.
     */
    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();

        return userRepository.findByUsername(username)
            .orElseThrow(() -> new RuntimeException("Користувача не знайдено: " + username));
    }
}
