# ORM & Hibernate Learning Platform

Учебная платформа на Spring Boot, демонстрирующая работу с JPA/Hibernate, связями 1-1/1-N/N-M, ленивой загрузкой и типовыми учебными сценариями: управление курсами, запись студентов, задания/решения и викторины.

## Стек
- Java 17, Spring Boot 3 (Web, Data JPA, Validation)
- PostgreSQL
- Maven Wrapper (`mvnw`) для сборки
- Testcontainers (PostgreSQL) для интеграционных тестов
- Docker + Docker Compose (готовый стенд)

## Быстрый старт

1. **Настройка окружения**
   - Нужна JDK 17+
   - PostgreSQL 14+ (или совместимая СУБД)
   - Переменные окружения (или значения по умолчанию из `application.yml`):
     ```
     APP_DB_URL=jdbc:postgresql://localhost:5432/orm_platform
     APP_DB_USERNAME=postgres
     APP_DB_PASSWORD=postgres
     ```

2. **Сборка и запуск локально**
   ```bash
   ./mvnw spring-boot:run
   ```
   При первом запуске автоматически создаются учебные данные (учитель, студент, курс «ORM & Hibernate Bootcamp», задание и квиз). В профиле `test` сидер отключён (`@Profile("!test")`).

3. **Запуск через Docker Compose**
   ```bash
   docker compose up --build
   ```
   Сервисы:
   - `db` — PostgreSQL 15 (порт 5432, логин/пароль `postgres`);
   - `app` — сборка через multi-stage Dockerfile, приложение доступно на `http://localhost:8080`.

4. **Профиль для тестов**
   ```bash
   ./mvnw test -Dspring.profiles.active=test
   ```
   Тесты используют Testcontainers и требуют Docker. Если Docker недоступен, сценарии будут пропущены (build останется зелёным, но без фактического прогона).

## Основные REST-эндпойнты

| Метод | URI | Описание |
|-------|-----|----------|
| `POST` | `/api/courses` | Создать курс с модулями и уроками |
| `GET` | `/api/courses/{id}` | Получить курс со структурой (модули → уроки → задания) |
| `GET` | `/api/courses` | Список курсов |
| `POST` | `/api/courses/{id}/enrollments` | Записать студента на курс |
| `DELETE` | `/api/enrollments/{enrollmentId}` | Отменить запись |
| `GET` | `/api/courses/{id}/students` | Список студентов курса |
| `GET` | `/api/users/{userId}/courses` | Курсы, на которые записан студент |
| `POST` | `/api/lessons/{lessonId}/assignments` | Создать задание |
| `POST` | `/api/assignments/{assignmentId}/submissions` | Отправить решение |
| `PATCH` | `/api/submissions/{submissionId}/grade` | Проверить/оценить решение |
| `GET` | `/api/users/{userId}/submissions` | Все решения студента |
| `POST` | `/api/modules/{moduleId}/quiz` | Создать квиз для модуля |
| `POST` | `/api/quizzes/{quizId}/attempts` | Пройти квиз |
| `GET` | `/api/users/{userId}/quiz-results` | Результаты квизов студента |

Ответы оформляются через DTO, ошибки — через `ApiError` с HTTP-кодами 4xx/5xx.

## Модель данных
Реализовано 17 сущностей: `User`, `Profile`, `Course`, `Category`, `Tag`, `Module`, `Lesson`, `LessonMaterial`, `Assignment`, `Submission`, `Enrollment`, `Quiz`, `Question`, `AnswerOption`, `QuizSubmission`, `CourseReview`, `CourseSchedule`.  
Покрыты связи 1-1 (например `User` ↔ `Profile`, `Module` ↔ `Quiz`), 1-N (`Course` → `Module`, `Lesson` → `Assignment`, `Assignment` → `Submission`), N-M (`Course` ↔ `Tag` через `course_tags`, `User` ↔ `Course` через `Enrollment`).  
Все коллекции объявлены с `fetch = LAZY`, что позволяет отлавливать `LazyInitializationException`. Для выдачи полных графов используются `JOIN FETCH`-запросы и DTO.

## Тестирование

- `PlatformIntegrationTest` (SpringBootTest + Testcontainers) покрывает:
  - каскадное создание курса с модулями/уроками
  - жизненный цикл Enrollment
  - выдача/сдача/проверка задания
  - демонстрацию `LazyInitializationException` и правильную загрузку через `findDetailedById`

> ⚠️ Для их выполнения нужны и JDK 17+, и Docker (Testcontainers поднимает PostgreSQL-контейнер). Если Docker отключён, тесты будут автоматически помечены как `skipped`. На CI или локально с Docker Desktop они выполняются полностью.

## Работа с ленивой загрузкой
- `CourseService#getCourseDetails` использует `JOIN FETCH`, чтобы вернуть курс с модулями, уроками и заданиями;
- Тест `lazyLoadingRequiresFetchJoin` демонстрирует исключение при обращении к ленивой коллекции уже после закрытия транзакции;
- В REST-слое коллекции никогда не передаются напрямую — данные конвертируются в DTO на сервисном уровне, пока транзакция активна.

## Деплой и Render
Конфигурация БД вынесена в переменные окружения; приложение не использует платформозависимых скриптов. Для Render (или другого PaaS) достаточно передать `APP_DB_*` переменные и запустить `./mvnw spring-boot:run` или собрать jar (`./mvnw package` → `java -jar target/orm-platform-0.0.1-SNAPSHOT.jar`).
