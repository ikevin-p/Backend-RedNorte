# ... etapa de construcción igual ... 

# ETAPA DE EJECUCION
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

EXPOSE 8081
# Cambiado de 8082 para que coincida con el servicio interno 

ENTRYPOINT ["java", "-jar", "app.jar"]