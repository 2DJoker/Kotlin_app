# Ktor Shop Backend

Backend-сервис на Ktor для управления товарами и заказами.

## Реализовано

- Регистрация и логин (`JWT`)
- Пользовательские маршруты (`products`, `orders`, отмена заказа, история)
- Админ-маршруты (CRUD товаров + статистика заказов)
- PostgreSQL + Exposed ORM + Flyway миграции
- Redis-кэш (товары/заказы, TTL)
- RabbitMQ продюсер/консьюмер (worker + fake email лог)
- Swagger/OpenAPI (`/swagger`, `/openapi`)
- Тесты:
  - unit: 3+
  - integration (Testcontainers): 2
  - e2e API: 2
- Dockerfile + docker-compose
- GitHub Actions CI

## Структура

- `src/main/kotlin/com/example/domain` - модели и DTO
- `src/main/kotlin/com/example/repository` - работа с БД
- `src/main/kotlin/com/example/service` - бизнес-логика
- `src/main/kotlin/com/example/routing` - контроллеры
- `src/main/kotlin/com/example/cache` - Redis слой
- `src/main/kotlin/com/example/messaging` - RabbitMQ продюсер
- `src/main/kotlin/com/example/worker` - фоновый консьюмер
- `src/main/resources/db/migration` - Flyway

## Быстрый запуск

```bash
docker compose up --build
```

Сервис: `http://localhost:8080`

- Swagger UI: `http://localhost:8080/swagger`
- OpenAPI: `http://localhost:8080/openapi`
- Health: `http://localhost:8080/health`

## Локальный запуск без Docker

1. Поднять PostgreSQL, Redis, RabbitMQ
2. Выставить ENV:
- `DB_URL`, `DB_USER`, `DB_PASSWORD`
- `JWT_SECRET`
- `REDIS_HOST`, `REDIS_PORT`, `REDIS_ENABLED`
- `RABBIT_HOST`, `RABBIT_PORT`, `RABBIT_USER`, `RABBIT_PASSWORD`, `RABBIT_QUEUE`, `RABBIT_ENABLED`
3. Запустить:

```bash
./gradlew run
```

## Маршруты API

### Auth
- `POST /auth/register`
- `POST /auth/login`

### User
- `GET /products`
- `GET /products/{id}`
- `POST /orders`
- `GET /orders`
- `DELETE /orders/{id}`

### Admin
- `POST /admin/products`
- `PUT /admin/products/{id}`
- `DELETE /admin/products/{id}`
- `GET /admin/stats/orders`

## Деплой

Подходит Railway/Render/DigitalOcean (через Dockerfile).

Пример для Railway:
1. Создать новый проект из GitHub репозитория
2. Добавить переменные окружения из секции выше
3. Добавить PostgreSQL, Redis и RabbitMQ сервисы
4. Указать команду запуска (по умолчанию из Dockerfile)

После деплоя добавьте в README ссылку на live URL (например `https://your-app.up.railway.app`).
# Kotlin_app
