package kz.alash.examreservation.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI driverExamReservationOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("Driver Exam Reservation Service API")
                        .description("REST API for managing students and driving license exam reservations")
                        .version("v1")
                        .license(new License().name("Private")));
    }
}
