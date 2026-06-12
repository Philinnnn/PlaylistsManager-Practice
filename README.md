# PlaylistsManagerAPI

Сервис для автоматического создания плейлистов в Spotify на основе предпочтений пользователя. Подбор треков выполняется с помощью локальной LLM (Llama через Ollama) на основе жанра, региона, настроения или истории прослушиваний пользователя. Дополнительно поддерживается поиск видеоклипов через YouTube Data API.

## Стек

- **Backend:** Java, Spring Boot, Spring Security, Spring Data JPA
- **Аутентификация:** регистрация/логин с TOTP-двухфакторной аутентификацией, OAuth2 (Google, Spotify), Remember Me
- **Интеграции:**
  - Spotify Web API — получение треков пользователя, топ-треков артистов, создание плейлистов
  - Ollama (локальная LLM, модель Llama) — генерация списков артистов и подбор треков по жанру/настроению/региону
  - YouTube Data API — поиск видео по запросу
- **БД:** реляционная БД через JPA/Hibernate (настраивается через переменные окружения)
- **Документация API:** Swagger / OpenAPI (springdoc)

## Возможности

- Регистрация и вход пользователей с поддержкой TOTP 2FA (Google Authenticator и аналоги)
- Вход через Google и Spotify (OAuth2)
- Подключение Spotify-аккаунта для работы с плейлистами и историей прослушиваний
- Генерация рекомендаций треков с помощью локальной LLM:
  - по жанру, региону и настроению
  - на основе топ-треков пользователя (похожие исполнители и треки)
- Создание готового плейлиста в Spotify из сгенерированного списка треков
- Поиск видеоклипов на YouTube по названию трека/исполнителя

## Архитектура

Принцип работы AI-рекомендаций:

1. Пользователь задаёт параметры (жанр/регион/настроение) или используется его история прослушиваний (Spotify top tracks)
2. `LlamaService` формирует текстовый промпт и отправляет его в локальный Ollama-сервер (`/api/generate`)
3. Модель возвращает список исполнителей или треков; ответ парсится в структурированный формат
4. Для предложенных исполнителей через Spotify API подгружаются их реальные топ-треки
5. Из получившегося пула треков LLM отбирает финальный список (до 50 треков), соответствующий запросу
6. Финальный список передаётся в `PlaylistController` для создания плейлиста в Spotify аккаунте пользователя

## Запуск проекта

### Требования

- Java 17+
- Maven
- Запущенный [Ollama](https://ollama.com/) с загруженной моделью (например, `llama3`)
- Реляционная БД (PostgreSQL/MySQL/MSSQL — на выбор, настраивается через переменные окружения)
- Зарегистрированные OAuth2-приложения в Spotify Developer Dashboard и Google Cloud Console
- API-ключ Google (для YouTube Data API)

### Переменные окружения

Создайте файл `.env` (или настройте переменные окружения системы) со следующими параметрами:

```env
SERVER_PORT=8080

# База данных
DB_URL=jdbc:postgresql://localhost:5432/playlistsdb
DB_USERNAME=postgres
DB_PASSWORD=your_password
DB_DRIVER_CLASS_NAME=org.postgresql.Driver
HIBERNATE_DDL_AUTO=update
HIBERNATE_SHOW_SQL=true
HIBERNATE_DIALECT=org.hibernate.dialect.PostgreSQLDialect

# Google OAuth2 / YouTube
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
GOOGLE_SCOPE=email,profile
GOOGLE_API_KEY=your_youtube_api_key

# Spotify OAuth2
SPOTIFY_CLIENT_ID=your_spotify_client_id
SPOTIFY_CLIENT_SECRET=your_spotify_client_secret
SPOTIFY_SCOPE=user-read-private,user-read-email,user-top-read,playlist-modify-public,playlist-modify-private
SPOTIFY_AUTHORIZATION_URI=https://accounts.spotify.com/authorize
SPOTIFY_TOKEN_URI=https://accounts.spotify.com/api/token
SPOTIFY_USER_INFO_URI=https://api.spotify.com/v1/me
SPOTIFY_USER_NAME_ATTRIBUTE=id

# Локальная LLM (Ollama)
LLAMA_HOST=http://localhost:11434
LLAMA_MODEL=llama3
```

### Запуск Ollama

```bash
ollama pull llama3
ollama serve
```

### Сборка и запуск приложения

```bash
mvn clean install
mvn spring-boot:run
```

Приложение будет доступно по адресу `http://localhost:8080`. Документация API — `http://localhost:8080/swagger-ui.html`.

## Структура проекта

```
src/main/java/.../practice/
├── config/         # конфигурация Spring Security, загрузка переменных окружения
├── controllers/     # REST-контроллеры (auth, 2FA, Spotify, Llama, YouTube, плейлисты)
├── services/        # бизнес-логика (Auth, Spotify, Llama, Playlist)
├── entities/         # JPA-сущности (пользователи, плейлисты)
├── repositories/     # Spring Data репозитории
├── dto/              # объекты передачи данных
├── enums/            # перечисления
└── utils/            # утилиты (TOTP, сессии, загрузка свойств)
src/main/resources/
├── templates/        # HTML-страницы (Thymeleaf): логин, регистрация, 2FA, дашборд
└── static/           # CSS и JS
```

## Примечания

Проект разработан в рамках учебной практики. Для работы AI-рекомендаций требуется локально запущенный Ollama с моделью Llama — без него эндпоинты `/llama/**` вернут пустой результат.
