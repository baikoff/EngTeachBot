# Используем базовый образ с Java
FROM amazoncorretto:21-alpine

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем JAR-файл приложения в контейнер
COPY target/EnglishLearningBot-1.0-SNAPSHOT.jar ./myapp.jar

# Указываем команду для запуска приложения
CMD ["java", "-jar", "-Dserver.port=8080",  "myapp.jar"]

