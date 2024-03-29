package ml.echelon133.services.graphpathfinder;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import ml.echelon133.graph.Graph;
import ml.echelon133.graph.Vertex;
import ml.echelon133.graph.VertexResult;
import ml.echelon133.graph.json.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.math.BigDecimal;
import java.util.Map;

@SpringBootApplication
@EnableDiscoveryClient
@EnableSwagger2
@EnableFeignClients
public class GraphPathFinderApp {

    private static final Logger LOGGER = LoggerFactory.getLogger(GraphPathFinderApp.class);

    @Bean
    public static ObjectMapper objectMapper() {
        LOGGER.info("Started setup of ObjectMapper");
        SimpleModule module = new SimpleModule();
        ObjectMapper mapper = new ObjectMapper();

        JavaType vertexType = mapper.constructType(Vertex.class);
        JavaType graphBigDecimalType = mapper.getTypeFactory().constructParametricType(Graph.class, BigDecimal.class);
        JavaType vertexResultType = mapper.constructType(VertexResult.class);
        JavaType resultMapType = mapper.getTypeFactory().constructMapType(Map.class, Vertex.class, VertexResult.class);

        module.addSerializer(new VertexSerializer(vertexType));
        module.addSerializer(new VertexResultSerializer(vertexResultType));
        module.addSerializer(new ResultMapSerializer(resultMapType));

        module.addDeserializer(Graph.class, new GraphDeserializer(graphBigDecimalType));

        mapper.registerModule(module);

        LOGGER.info("Finished setup of ObjectMapper");
        return mapper;
    }

    @Bean
    public Docket swaggerApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .select()
                .apis(RequestHandlerSelectors.basePackage("ml.echelon133.services.graphpathfinder"))
                .paths(PathSelectors.any())
                .build()
                .apiInfo(new ApiInfoBuilder()
                        .title("Graph Path Finder Service API")
                        .version("1.0")
                        .description("This is the documentation of Graph Path Finder Service")
                        .build());
    }

    public static void main(String[] args) {
        new SpringApplicationBuilder(GraphPathFinderApp.class).run(args);
    }
}
