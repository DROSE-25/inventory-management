package com.inventory;

import com.inventory.repository.CategoryRepository;
import com.inventory.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataInitRunner implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public void run(String... args) {
        System.out.println("=== Перевірка репозиторіїв ===");

        userRepository.findByUsername("admin")
            .ifPresentOrElse(
                u -> System.out.println("Admin знайдений: " + u.getEmail()),
                () -> System.out.println("Admin НЕ знайдений!")
            );

        long categoryCount = categoryRepository.count();
        System.out.println("Категорій у БД: " + categoryCount);

        System.out.println("=== Перевірка завершена ===");
    }
}