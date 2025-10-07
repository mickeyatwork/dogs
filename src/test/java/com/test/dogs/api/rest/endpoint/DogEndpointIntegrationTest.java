package com.test.dogs.api.rest.endpoint;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.dogs.api.rest.model.DogDTO;
import com.test.dogs.api.rest.service.DogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.springframework.test.web.servlet.MockMvc;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DogEndpoint.class)
@SuppressWarnings("deprecation")
public class DogEndpointIntegrationTest extends AbstractTestNGSpringContextTests {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DogService dogService;

    @Autowired
    private ObjectMapper objectMapper;

    private DogDTO testDog;

    @BeforeMethod
    public void setUp() {
        testDog = new DogDTO();
        testDog.setId(1L);
        testDog.setName("Buddy");
        testDog.setBreed("Golden Retriever");
        testDog.setSupplier("Test Supplier");
        testDog.setBadgeID(12345);
        testDog.setGender("Male");
        testDog.setBirthDate(Date.valueOf(LocalDate.of(2020, 1, 1)));
        testDog.setDateAcquired(Date.valueOf(LocalDate.of(2021, 1, 1)));
        testDog.setStatus("Active");
    }

    @Test
    public void testGetAllDogsEndpoint() throws Exception {
        // Arrange
        List<DogDTO> dogs = Collections.singletonList(testDog);
        when(dogService.getAllDogsList(null)).thenReturn(dogs);

        // Act & Assert
        mockMvc.perform(get("/")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Buddy"))
                .andExpect(jsonPath("$[0].breed").value("Golden Retriever"))
                .andExpect(jsonPath("$[0].id").value(1));

        verify(dogService, times(1)).getAllDogsList(null);
    }

    @Test
    public void testGetAllDogsWithFilterEndpoint() throws Exception {
        // Arrange
        List<DogDTO> dogs = Collections.singletonList(testDog);
        when(dogService.getAllDogsList("Golden")).thenReturn(dogs);

        // Act & Assert
        mockMvc.perform(get("/")
                .param("filter", "Golden")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].breed").value("Golden Retriever"));

        verify(dogService, times(1)).getAllDogsList("Golden");
    }

    @Test
    public void testGetDogByIdEndpoint() throws Exception {
        // Arrange
        when(dogService.getDogById(1)).thenReturn(testDog);

        // Act & Assert
        mockMvc.perform(get("/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Buddy"))
                .andExpect(jsonPath("$.breed").value("Golden Retriever"))
                .andExpect(jsonPath("$.id").value(1));

        verify(dogService, times(1)).getDogById(1);
    }

    @Test
    public void testGetAllDogsIncludingDeletedEndpoint() throws Exception {
        // Arrange
        List<DogDTO> dogs = Collections.singletonList(testDog);
        when(dogService.getAllDogsIncludingDeleted()).thenReturn(dogs);

        // Act & Assert
        mockMvc.perform(get("/dogs")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Buddy"));

        verify(dogService, times(1)).getAllDogsIncludingDeleted();
    }

    @Test
    public void testCreateDogEndpoint() throws Exception {
        // Arrange
        DogDTO newDog = new DogDTO();
        newDog.setName("Max");
        newDog.setBreed("Labrador");
        newDog.setGender("Male");

        // Mock the service to return the created dog with an ID
        DogDTO savedDog = new DogDTO();
        savedDog.setId(1L);
        savedDog.setName("Max");
        savedDog.setBreed("Labrador");
        savedDog.setGender("Male");
        when(dogService.saveNewDog(any(DogDTO.class))).thenReturn(savedDog);

        // Act & Assert
        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(newDog)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Max"))
                .andExpect(jsonPath("$.breed").value("Labrador"));

        verify(dogService, times(1)).saveNewDog(any(DogDTO.class));
    }

    @Test
    public void testUpdateDogEndpoint() throws Exception {
        // Arrange
        DogDTO updatedDog = new DogDTO();
        updatedDog.setId(1L);
        updatedDog.setName("Buddy Updated");
        updatedDog.setBreed("Golden Retriever");

        // Mock the service to return the updated dog
        DogDTO returnedDog = new DogDTO();
        returnedDog.setId(1L);
        returnedDog.setName("Buddy Updated");
        returnedDog.setBreed("Golden Retriever");
        when(dogService.updateDog(eq(1), any(DogDTO.class))).thenReturn(returnedDog);

        // Act & Assert
        mockMvc.perform(put("/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedDog)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Buddy Updated"));

        verify(dogService, times(1)).updateDog(eq(1), any(DogDTO.class));
    }

    @Test
    public void testDeleteDogEndpoint() throws Exception {
        // Arrange
        doNothing().when(dogService).markDogAsDeleted(1);

        // Act & Assert
        mockMvc.perform(delete("/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("Dog with ID 1 has been successfully deleted"));

        verify(dogService, times(1)).markDogAsDeleted(1);
    }

    @Test
    public void testCreateDogWithInvalidDataEndpoint() throws Exception {
        // Arrange
        DogDTO invalidDog = new DogDTO();
        // Empty dog - no required fields set

        // Act & Assert
        mockMvc.perform(post("/")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDog)))
                .andExpect(status().isOk());

        verify(dogService, times(1)).saveNewDog(any(DogDTO.class));
    }
}
