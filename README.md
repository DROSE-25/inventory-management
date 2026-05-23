# Inventory Management System

Система оптимізації товарних запасів на основі алгоритмів EOQ, Safety Stock, ABC/XYZ аналізу та методів прогнозування попиту.

## Технологічний стек

| Технологія | Версія | Призначення |
|---|---|---|
| Java | 21 | Мова програмування |
| Spring Boot | 3.x | Фреймворк |
| PostgreSQL | 15 | База даних |
| Flyway | - | Міграції БД |
| JWT | - | Автентифікація |
| Apache POI | 5.x | Експорт Excel |
| iText | 5.x | Експорт PDF |
| Docker | - | Контейнеризація |

## Функціонал

- CRUD: товари, постачальники, склади, продажі, користувачі
- Алгоритми: EOQ, Safety Stock, Reorder Point
- Прогнозування: SMA, WMA, SES, Holt, Linear Regression
- Аналітика: ABC/XYZ класифікація
- Звіти: CSV, Excel, PDF
- Ролі: ADMIN, MANAGER, ANALYST

## Швидкий старт

### Docker (рекомендовано)

```bash
git clone https://github.com/DROSE-25/inventory-management.git
cd inventory-management
docker-compose up --build
```

Swagger UI: http://localhost:8080/swagger-ui.html

### Локально

```bash
# 1. Запустити PostgreSQL
# 2. Створити БД inventory_db
# 3. Налаштувати application.properties
./mvnw spring-boot:run
```

## Змінні середовища

| Змінна | Опис | Default |
|---|---|---|
| SPRING_DATASOURCE_URL | URL бази даних | jdbc:postgresql://localhost:5432/inventory_db |
| SPRING_DATASOURCE_USERNAME | Користувач БД | postgres |
| SPRING_DATASOURCE_PASSWORD | Пароль БД | postgres |
| JWT_SECRET | Секрет для JWT токенів | default-secret |

## Ролі та доступ

| Роль | Доступ |
|---|---|
| ADMIN | Повний доступ |
| MANAGER | Перегляд + реєстрація продажів |
| ANALYST | Тільки перегляд та аналітика |