# Этап 1: Сборка
FROM gradle:8.1-jdk8 AS builder

# Устанавливаем рабочую директорию
WORKDIR /app

# Копируем исходный код
COPY . .

# Собираем проект
RUN ./gradlew build

# Этап 2: Запуск
FROM jetty:9-jre8

# Устанавливаем рабочую директорию
WORKDIR /var/lib/jetty

# Копируем собранный .war файл из этапа сборки
COPY --from=builder /app/build/libs/jtrac.war /var/lib/jetty/webapps/jtrac.war

# Устанавливаем JTRAC_HOME для хранения данных
ENV JTRAC_HOME /var/lib/jetty/jtrac-data
RUN mkdir -p $JTRAC_HOME

# Открываем порт
EXPOSE 8080

# Запускаем Jetty
CMD ["java", "-jar", "/usr/local/jetty/start.jar"]
