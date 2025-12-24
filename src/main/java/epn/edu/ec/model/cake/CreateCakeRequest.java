package epn.edu.ec.model.cake;

import io.swagger.v3.oas.annotations.info.Info;
import lombok.Data;

@Data
public class CreateCakeRequest {
    private String title;
    private String description;

}
