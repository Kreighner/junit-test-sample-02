package epn.edu.ec.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import epn.edu.ec.model.cake.CakeResponse;
import epn.edu.ec.model.cake.CakesResponse;
import epn.edu.ec.model.cake.CreateCakeRequest;
import epn.edu.ec.model.cake.UpdateCakeRequest;
import epn.edu.ec.service.CakeService;
import lombok.Builder;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = CakeController.class,
        excludeAutoConfiguration= {SecurityAutoConfiguration.class})
@ActiveProfiles("test")
public class CakeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean // ->@Mock
    private CakeService cakeService;

    private final long cakeId = 1;
    private final CakeResponse mockCakeResponse = new CakeResponse(
            cakeId, "Mock Cake", "Moke Cake Description"
    );

    @Test
    public void getCakes_shouldReturnListOfCakes() throws Exception {
        //ARRANGE
        // Codigo similar
        //List<CakeResponse> cakeList = new ArrayList<>();
        //cakeList.add(mockCakeResponse);

        CakesResponse cakesResponse = new CakesResponse(List.of(mockCakeResponse));
        when(cakeService.getCakes()).thenReturn(cakesResponse);

        //ACT
        ResultActions result = mockMvc.perform(get("/cakes")
                .contentType("application/json"));

        //ASSERT
        result.andExpect(status().isOk());
        result.andExpect(content().contentType("application/json"));
        result.andExpect(content().json(objectMapper.writeValueAsString(cakesResponse)));

        // System.out.println(result.andReturn().getResponse().getContentAsString());

        verify(cakeService, times(1)).getCakes();
    }

    @Test
    public void createCake_shouldCreateCake() throws Exception {
        //ARRANGE
        //Request
        CreateCakeRequest createCakeRequest =
                CreateCakeRequest.builder().title("New Cake")
                        .description("New Cake Description").build();
        //Response
        CakeResponse cakeResponse =
                CakeResponse.builder().id(2l).
                        title("New Cake")
                        .description("New Cake Description").build();
        when(cakeService.createCake(createCakeRequest)).thenReturn(cakeResponse);
        //ACT
        ResultActions result = mockMvc.perform(post("/cakes")
                .content("application/json")
                .content(objectMapper.writeValueAsString(createCakeRequest)));

        //ASSERTS
        result.andExpect(status().isCreated());
    }

    @Test
    public void getCakes_shouldReturnEmptyList() throws Exception {
        // ARRANGE
        CakesResponse emptyResponse = new CakesResponse(List.of());
        when(cakeService.getCakes()).thenReturn(emptyResponse);

        // ACT
        ResultActions result = mockMvc.perform(get("/cakes")
                .contentType(MediaType.APPLICATION_JSON));

        // ASSERT
        result.andExpect(status().isOk());
        result.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        result.andExpect(content().json(objectMapper.writeValueAsString(emptyResponse)));
        verify(cakeService, times(1)).getCakes();
    }

    @Test
    public void getCakeById_shouldReturnCake() throws Exception {
        // ARRANGE
        when(cakeService.getCakeById(cakeId)).thenReturn(mockCakeResponse);

        // ACT
        ResultActions result = mockMvc.perform(get("/cakes/{id}", cakeId)
                .contentType(MediaType.APPLICATION_JSON));

        // ASSERT
        result.andExpect(status().isOk());
        result.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        result.andExpect(content().json(objectMapper.writeValueAsString(mockCakeResponse)));
        verify(cakeService, times(1)).getCakeById(cakeId);
    }

    // valida que GET /cakes/{id} responda 404 cuando el servicio indica que no existe.
    @Test
    public void getCakeById_shouldReturnNotFound() throws Exception {
        // ARRANGE
        when(cakeService.getCakeById(cakeId)).thenThrow(new RuntimeException("Cake not found"));

        // ACT
        ResultActions result = mockMvc.perform(get("/cakes/{id}", cakeId)
                .contentType(MediaType.APPLICATION_JSON));

        // ASSERT
        result.andExpect(status().isNotFound());
        verify(cakeService, times(1)).getCakeById(cakeId);
    }

    //valida que PUT /cakes/{id} responda 200 y devuelva el cake actualizado.
    @Test
    public void updateCake_shouldUpdateCake() throws Exception {
        // ARRANGE
        UpdateCakeRequest updateRequest = UpdateCakeRequest.builder()
                .title("Updated Cake")
                .description("Updated Description")
                .build();

        CakeResponse updatedResponse = CakeResponse.builder()
                .id(cakeId)
                .title("Updated Cake")
                .description("Updated Description")
                .build();

        when(cakeService.updateCake(eq(cakeId), any(UpdateCakeRequest.class))).thenReturn(updatedResponse);

        // ACT
        ResultActions result = mockMvc.perform(put("/cakes/{id}", cakeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        // ASSERT
        result.andExpect(status().isOk());
        result.andExpect(content().contentType(MediaType.APPLICATION_JSON));
        result.andExpect(content().json(objectMapper.writeValueAsString(updatedResponse)));
        verify(cakeService, times(1)).updateCake(eq(cakeId), any(UpdateCakeRequest.class));
    }

    //valida que PUT /cakes/{id} responda 404 si se intenta actualizar un cake inexistente.
    @Test
    public void updateCake_shouldReturnNotFound() throws Exception {
        // ARRANGE
        UpdateCakeRequest updateRequest = UpdateCakeRequest.builder()
                .title("Updated Cake")
                .description("Updated Description")
                .build();

        when(cakeService.updateCake(eq(cakeId), any(UpdateCakeRequest.class)))
                .thenThrow(new RuntimeException("Cake not found"));

        // ACT
        ResultActions result = mockMvc.perform(put("/cakes/{id}", cakeId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)));

        // ASSERT
        result.andExpect(status().isNotFound());
        verify(cakeService, times(1)).updateCake(eq(cakeId), any(UpdateCakeRequest.class));
    }

    @Test
    public void deleteCake_shouldDeleteCake() throws Exception {
        // ARRANGE
        doNothing().when(cakeService).deleteCake(cakeId);

        // ACT
        ResultActions result = mockMvc.perform(delete("/cakes/{id}", cakeId));

        // ASSERT
        result.andExpect(status().isNoContent());
        verify(cakeService, times(1)).deleteCake(cakeId);
    }
}
