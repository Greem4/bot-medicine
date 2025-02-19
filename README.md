# MedicineBot – Телеграм-бот для проверки сроков годности лекарств

**MedicineBot** – это телеграм-бот, написанный на **Java 21** с использованием **Spring Boot**. Проект собирается с помощью **Gradle** и готов к развёртыванию через **Docker**.

Бот помогает отслеживать сроки годности лекарств, показывая информацию о медикаментах с критическим сроком (менее 30 дней) и предоставляя удобный интерфейс для просмотра расписаний групп через инлайн-кнопки.

## Основные возможности

- **/start** – запускает бота и отправляет главное меню с кнопками:
    - **⏰ Срок годности** – проверяет и выводит список лекарств с критическим сроком годности.
    - **🗓 График** – отправляет изображение с расписанием для вашей группы.
    - **⚙ Админ-меню** (для администраторов) – позволяет выполнять административные операции:
        - Добавление/обновление группы.
        - Обновление расписания.
        - Удаление группы.
        - Удаление пользователя из группы.
- **/hi** – приветственное сообщение.
- **/help** – краткая справка по доступным командам.

Бот взаимодействует с сервером проекта **[spring-medicines](https://github.com/Greem4/spring-medicines)** для получения актуальных данных о лекарствах.

## Технологии

- **Java 21**
- **Spring Boot 3.4.2**
- **Gradle 8.5**
- **Docker** для контейнеризации и развёртывания
- **Spring Data JPA** для работы с базой данных
- **TelegramBots API** для интеграции с Telegram

## Сборка и запуск

настроить .env

### Сборка проекта

```bash
./gradlew build
