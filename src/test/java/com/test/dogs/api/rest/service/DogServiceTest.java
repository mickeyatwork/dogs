package com.test.dogs.api.rest.service;

import com.test.dogs.api.rest.model.DogDTO;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.sql.*;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.testng.Assert.*;

public class DogServiceTest {

    @Mock
    private Connection connection;

    @Mock
    private PreparedStatement preparedStatement;

    @Mock
    private ResultSet resultSet;

    @InjectMocks
    private DogService dogService;

    private AutoCloseable mocks;
    private MockedStatic<DriverManager> driverManagerMock;

    @BeforeMethod
    public void setUp() throws SQLException {
        mocks = MockitoAnnotations.openMocks(this);

        // Set test database properties using reflection
        ReflectionTestUtils.setField(dogService, "DB_URL", "jdbc:h2:mem:testdb");
        ReflectionTestUtils.setField(dogService, "DB_USER", "sa");
        ReflectionTestUtils.setField(dogService, "DB_PASSWORD", "");

        // Mock database connections
        driverManagerMock = mockStatic(DriverManager.class);
        driverManagerMock.when(() -> DriverManager.getConnection(anyString(), anyString(), anyString()))
                         .thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
    }

    @AfterMethod
    public void tearDown() throws Exception {
        if (mocks != null) {
            mocks.close();
        }
        if (driverManagerMock != null) {
            driverManagerMock.close();
        }
    }

    @Test
    public void testGetAllDogsListWithoutFilter() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(true, false); // One row then no more
        when(resultSet.getInt("ID")).thenReturn(1);
        when(resultSet.getString("NAME")).thenReturn("Buddy");
        when(resultSet.getString("BREED")).thenReturn("Golden Retriever");
        when(resultSet.getString("SUPPLIER")).thenReturn("Test Supplier");
        when(resultSet.getInt("BADGEID")).thenReturn(12345);
        when(resultSet.getString("GENDER")).thenReturn("Male");
        when(resultSet.getDate("BIRTHDATE")).thenReturn(Date.valueOf(LocalDate.of(2020, 1, 1)));
        when(resultSet.getDate("DATEACQUIRED")).thenReturn(Date.valueOf(LocalDate.of(2021, 1, 1)));
        when(resultSet.getString("STATUS")).thenReturn("Active");
        when(resultSet.getDate("LEAVINGDATE")).thenReturn(null);
        when(resultSet.getString("LEAVINGREASON")).thenReturn(null);
        when(resultSet.getString("KENNELINGCHARACTERISTICS")).thenReturn("Friendly");
        when(resultSet.getDate("DATEDELETED")).thenReturn(null);

        // Act
        List<DogDTO> result = dogService.getAllDogsList(null);

        // Assert
        assertNotNull(result);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getName(), "Buddy");
        assertEquals(result.get(0).getBreed(), "Golden Retriever");

        verify(connection, times(1)).prepareStatement(contains("SELECT * FROM DOGS WHERE dateDeleted IS NULL"));
        verify(preparedStatement, times(1)).executeQuery();
    }

    @Test
    public void testGetAllDogsListWithFilter() throws SQLException {
        // Arrange
        String filter = "Golden";
        when(resultSet.next()).thenReturn(true, false);
        when(resultSet.getInt("ID")).thenReturn(1);
        when(resultSet.getString("NAME")).thenReturn("Buddy");
        when(resultSet.getString("BREED")).thenReturn("Golden Retriever");
        when(resultSet.getString("SUPPLIER")).thenReturn("Test Supplier");
        when(resultSet.getInt("BADGEID")).thenReturn(12345);
        when(resultSet.getString("GENDER")).thenReturn("Male");
        when(resultSet.getDate("BIRTHDATE")).thenReturn(Date.valueOf(LocalDate.of(2020, 1, 1)));
        when(resultSet.getDate("DATEACQUIRED")).thenReturn(Date.valueOf(LocalDate.of(2021, 1, 1)));
        when(resultSet.getString("STATUS")).thenReturn("Active");
        when(resultSet.getDate("LEAVINGDATE")).thenReturn(null);
        when(resultSet.getString("LEAVINGREASON")).thenReturn(null);
        when(resultSet.getString("KENNELINGCHARACTERISTICS")).thenReturn("Friendly");
        when(resultSet.getDate("DATEDELETED")).thenReturn(null);

        // Act
        List<DogDTO> result = dogService.getAllDogsList(filter);

        // Assert
        assertNotNull(result);
        assertEquals(result.size(), 1);
        assertEquals(result.get(0).getBreed(), "Golden Retriever");

        verify(connection, times(1)).prepareStatement(contains("AND (NAME LIKE ? OR BREED LIKE ? OR SUPPLIER LIKE ?)"));
        verify(preparedStatement, times(3)).setString(anyInt(), eq("%" + filter + "%"));
        verify(preparedStatement, times(1)).executeQuery();
    }

    @Test
    public void testGetAllDogsListEmptyResult() throws SQLException {
        // Arrange
        when(resultSet.next()).thenReturn(false); // No rows

        // Act
        List<DogDTO> result = dogService.getAllDogsList(null);

        // Assert
        assertNotNull(result);
        assertEquals(result.size(), 0);

        verify(connection, times(1)).prepareStatement(anyString());
        verify(preparedStatement, times(1)).executeQuery();
    }

    @Test
    public void testGetDogByIdSuccess() throws SQLException {
        // Arrange
        int dogId = 1;
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getInt("ID")).thenReturn(1);
        when(resultSet.getString("NAME")).thenReturn("Buddy");
        when(resultSet.getString("BREED")).thenReturn("Golden Retriever");
        when(resultSet.getString("SUPPLIER")).thenReturn("Test Supplier");
        when(resultSet.getInt("BADGEID")).thenReturn(12345);
        when(resultSet.getString("GENDER")).thenReturn("Male");
        when(resultSet.getDate("BIRTHDATE")).thenReturn(Date.valueOf(LocalDate.of(2020, 1, 1)));
        when(resultSet.getDate("DATEACQUIRED")).thenReturn(Date.valueOf(LocalDate.of(2021, 1, 1)));
        when(resultSet.getString("STATUS")).thenReturn("Active");
        when(resultSet.getDate("LEAVINGDATE")).thenReturn(null);
        when(resultSet.getString("LEAVINGREASON")).thenReturn(null);
        when(resultSet.getString("KENNELINGCHARACTERISTICS")).thenReturn("Friendly");
        when(resultSet.getDate("DATEDELETED")).thenReturn(null);

        // Act
        DogDTO result = dogService.getDogById(dogId);

        // Assert
        assertNotNull(result);
        assertEquals(result.getName(), "Buddy");
        assertEquals(result.getId().longValue(), 1L);

        verify(connection, times(1)).prepareStatement(contains("SELECT * FROM DOGS WHERE ID = ?"));
        verify(preparedStatement, times(1)).setInt(1, dogId);
        verify(preparedStatement, times(1)).executeQuery();
    }

    @Test
    public void testSaveNewDog() throws SQLException {
        // Arrange
        DogDTO newDog = new DogDTO();
        newDog.setName("Max");
        newDog.setBreed("Labrador");
        newDog.setGender("Male");
        newDog.setBadgeID(12345);
        newDog.setBirthDate(Date.valueOf(LocalDate.of(2020, 6, 15)));
        newDog.setDateAcquired(Date.valueOf(LocalDate.of(2021, 6, 15)));
        newDog.setStatus("In Training");

        // Mock for INSERT operation
        PreparedStatement insertStatement = mock(PreparedStatement.class);
        ResultSet generatedKeys = mock(ResultSet.class);

        // Mock for SELECT operation (getDogById call after INSERT)
        PreparedStatement selectStatement = mock(PreparedStatement.class);
        ResultSet selectResultSet = mock(ResultSet.class);

        // Setup connection mocking for different SQL statements
        when(connection.prepareStatement(contains("INSERT INTO DOGS"), eq(Statement.RETURN_GENERATED_KEYS)))
                .thenReturn(insertStatement);
        when(connection.prepareStatement(contains("SELECT * FROM DOGS WHERE ID = ?")))
                .thenReturn(selectStatement);

        // Mock INSERT operation
        when(insertStatement.executeUpdate()).thenReturn(1);
        when(insertStatement.getGeneratedKeys()).thenReturn(generatedKeys);
        when(generatedKeys.next()).thenReturn(true);
        when(generatedKeys.getInt(1)).thenReturn(1);

        // Mock SELECT operation for getDogById
        when(selectStatement.executeQuery()).thenReturn(selectResultSet);
        when(selectResultSet.next()).thenReturn(true);
        when(selectResultSet.getInt("ID")).thenReturn(1);
        when(selectResultSet.getString("NAME")).thenReturn("Max");
        when(selectResultSet.getString("BREED")).thenReturn("Labrador");
        when(selectResultSet.getString("SUPPLIER")).thenReturn(null);
        when(selectResultSet.getInt("BADGEID")).thenReturn(12345);
        when(selectResultSet.getString("GENDER")).thenReturn("Male");
        when(selectResultSet.getDate("BIRTHDATE")).thenReturn(Date.valueOf(LocalDate.of(2020, 6, 15)));
        when(selectResultSet.getDate("DATEACQUIRED")).thenReturn(Date.valueOf(LocalDate.of(2021, 6, 15)));
        when(selectResultSet.getString("STATUS")).thenReturn("In Training");
        when(selectResultSet.getDate("LEAVINGDATE")).thenReturn(null);
        when(selectResultSet.getString("LEAVINGREASON")).thenReturn(null);
        when(selectResultSet.getString("KENNELINGCHARACTERISTICS")).thenReturn(null);
        when(selectResultSet.getDate("DATEDELETED")).thenReturn(null);

        // Act
        DogDTO result = dogService.saveNewDog(newDog);

        // Assert
        assertNotNull(result);
        assertEquals(result.getName(), "Max");
        assertEquals(result.getBreed(), "Labrador");
        assertEquals(result.getId().longValue(), 1L);

        verify(connection, times(1)).prepareStatement(contains("INSERT INTO DOGS"), eq(Statement.RETURN_GENERATED_KEYS));
        verify(insertStatement, times(1)).setString(1, "Max");
        verify(insertStatement, times(1)).setString(2, "Labrador");
        verify(insertStatement, times(1)).setInt(4, 12345);
        verify(insertStatement, times(1)).setString(5, "Male");
        verify(insertStatement, times(1)).executeUpdate();
    }

    @Test
    public void testUpdateDog() throws SQLException {
        // Arrange
        int dogId = 1;
        DogDTO updatedDog = new DogDTO();
        updatedDog.setName("Buddy Updated");
        updatedDog.setBreed("Golden Retriever");
        updatedDog.setStatus("In Training");

        // Mock for SELECT operation (getDogById call at the beginning of updateDog)
        PreparedStatement selectStatement1 = mock(PreparedStatement.class);
        ResultSet selectResultSet1 = mock(ResultSet.class);

        // Mock for UPDATE operation
        PreparedStatement updateStatement = mock(PreparedStatement.class);
        ResultSet updateGeneratedKeys = mock(ResultSet.class);

        // Mock for SELECT operation (getDogById call after UPDATE to return updated record)
        PreparedStatement selectStatement2 = mock(PreparedStatement.class);
        ResultSet selectResultSet2 = mock(ResultSet.class);

        // Setup connection mocking for different SQL statements in sequence
        when(connection.prepareStatement(contains("SELECT * FROM DOGS WHERE ID = ?")))
            .thenReturn(selectStatement1)  // First call for initial getDogById
            .thenReturn(selectStatement2); // Second call for final getDogById
        when(connection.prepareStatement(contains("UPDATE DOGS SET"), eq(Statement.RETURN_GENERATED_KEYS)))
            .thenReturn(updateStatement);

        // Mock first SELECT operation (getDogById at start of updateDog)
        when(selectStatement1.executeQuery()).thenReturn(selectResultSet1);
        when(selectResultSet1.next()).thenReturn(true);
        when(selectResultSet1.getInt("ID")).thenReturn(1);
        when(selectResultSet1.getString("NAME")).thenReturn("Buddy");
        when(selectResultSet1.getString("BREED")).thenReturn("Golden Retriever");
        when(selectResultSet1.getString("SUPPLIER")).thenReturn("Test Supplier");
        when(selectResultSet1.getInt("BADGEID")).thenReturn(12345);
        when(selectResultSet1.getString("GENDER")).thenReturn("Male");
        when(selectResultSet1.getDate("BIRTHDATE")).thenReturn(Date.valueOf(LocalDate.of(2020, 1, 1)));
        when(selectResultSet1.getDate("DATEACQUIRED")).thenReturn(Date.valueOf(LocalDate.of(2021, 1, 1)));
        when(selectResultSet1.getString("STATUS")).thenReturn("Active");
        when(selectResultSet1.getDate("LEAVINGDATE")).thenReturn(null);
        when(selectResultSet1.getString("LEAVINGREASON")).thenReturn(null);
        when(selectResultSet1.getString("KENNELINGCHARACTERISTICS")).thenReturn("Friendly");
        when(selectResultSet1.getDate("DATEDELETED")).thenReturn(null);

        // Mock UPDATE operation
        when(updateStatement.executeUpdate()).thenReturn(1);
        when(updateStatement.getGeneratedKeys()).thenReturn(updateGeneratedKeys);
        when(updateGeneratedKeys.next()).thenReturn(true);
        when(updateGeneratedKeys.getInt(1)).thenReturn(1);

        // Mock second SELECT operation (getDogById at end of updateDog to return updated record)
        when(selectStatement2.executeQuery()).thenReturn(selectResultSet2);
        when(selectResultSet2.next()).thenReturn(true);
        when(selectResultSet2.getInt("ID")).thenReturn(1);
        when(selectResultSet2.getString("NAME")).thenReturn("Buddy Updated");
        when(selectResultSet2.getString("BREED")).thenReturn("Golden Retriever");
        when(selectResultSet2.getString("SUPPLIER")).thenReturn("Test Supplier");
        when(selectResultSet2.getInt("BADGEID")).thenReturn(12345);
        when(selectResultSet2.getString("GENDER")).thenReturn("Male");
        when(selectResultSet2.getDate("BIRTHDATE")).thenReturn(Date.valueOf(LocalDate.of(2020, 1, 1)));
        when(selectResultSet2.getDate("DATEACQUIRED")).thenReturn(Date.valueOf(LocalDate.of(2021, 1, 1)));
        when(selectResultSet2.getString("STATUS")).thenReturn("In Training");
        when(selectResultSet2.getDate("LEAVINGDATE")).thenReturn(null);
        when(selectResultSet2.getString("LEAVINGREASON")).thenReturn(null);
        when(selectResultSet2.getString("KENNELINGCHARACTERISTICS")).thenReturn("Friendly");
        when(selectResultSet2.getDate("DATEDELETED")).thenReturn(null);

        // Act
        DogDTO result = dogService.updateDog(dogId, updatedDog);

        // Assert
        assertNotNull(result);
        assertEquals(result.getName(), "Buddy Updated");
        assertEquals(result.getStatus(), "In Training");

        verify(connection, times(2)).prepareStatement(contains("SELECT * FROM DOGS WHERE ID = ?"));
        verify(connection, times(1)).prepareStatement(contains("UPDATE DOGS SET"), eq(Statement.RETURN_GENERATED_KEYS));
        verify(updateStatement, times(1)).executeUpdate();
    }

    @Test
    public void testMarkDogAsDeleted() throws SQLException {
        // Arrange
        int dogId = 1;
        when(preparedStatement.executeUpdate()).thenReturn(1);

        // Act
        dogService.markDogAsDeleted(dogId);

        // Assert
        verify(connection, times(1)).prepareStatement(contains("UPDATE DOGS SET dateDeleted = ? WHERE ID = ?"));
        verify(preparedStatement, times(1)).setDate(eq(1), any(Date.class));
        verify(preparedStatement, times(1)).setInt(2, dogId);
        verify(preparedStatement, times(1)).executeUpdate();
    }
}
