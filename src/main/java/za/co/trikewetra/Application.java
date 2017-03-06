package za.co.trikewetra;

import java.util.LinkedHashMap;
import java.util.Map;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.mangofactory.swagger.plugin.EnableSwagger;


@SpringBootApplication
@EnableSwagger
public class Application {

    public static void main(String[] args){
        SpringApplication.run(Application.class, args);
    }
}
