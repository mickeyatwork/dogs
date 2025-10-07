package com.test.dogs.api.rest.service;

import com.test.dogs.api.rest.model.DogDTO;
import com.test.dogs.api.rest.exception.DogNotFoundException;
import com.test.dogs.api.rest.exception.DogServiceException;
import com.test.dogs.api.rest.exception.DogValidationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class DogService {

	@Value("${spring.datasource.url}")
	private String DB_URL;

	@Value("${spring.datasource.username}")
	private String DB_USER;

	@Value("${spring.datasource.password}")
	private String DB_PASSWORD;

	private Connection getConnection() throws SQLException {

		return java.sql.DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
	}

	/**
	 * Retrieves all active records from the DOGS table and maps them to a list.
	 * @return A List of DogDTO objects, or an empty list if no dogs are found.
	 */
	public List<DogDTO> getAllDogsList(String filter) {

		// Only select active records
		String sql = "SELECT * FROM DOGS WHERE dateDeleted IS NULL";

		List<String> parameters = new ArrayList<>();

		// Check if a filter term was provided
		if (filter != null && !filter.trim().isEmpty()) {
			sql += " AND (NAME LIKE ? OR BREED LIKE ? OR SUPPLIER LIKE ?)";

			String searchTerm = "%" + filter.trim() + "%";
			parameters.add(searchTerm);
			parameters.add(searchTerm);
			parameters.add(searchTerm);
		}

		List<DogDTO> dogs = new ArrayList<>();

		try (Connection conn = getConnection();
		     PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

			// Set the filter parameters, if the filter was used
			for (int i = 0; i < parameters.size(); i++) {
				preparedStatement.setString(i + 1, parameters.get(i));
			}

			try (ResultSet rs = preparedStatement.executeQuery()) {
				while (rs.next()) {
					DogDTO dog = mapResultSetToDogDto(rs);
					dogs.add(dog);
				}
			}
		} catch (SQLException e) {
			throw new DogServiceException("Error while retrieving all dogs: " + e.getMessage(), e);
		}

		return dogs;
	}

	/**
	 * Retrieves all records from the DOGS table and maps them to a list, including deleted records.
	 * @return A List of DogDTO objects, or an empty list if no dogs are found.
	 */
	public List<DogDTO> getAllDogsIncludingDeleted() {
		String sql = "SELECT * FROM DOGS";
		List<DogDTO> dogs = new ArrayList<>();

		try (Connection conn = getConnection();
		     PreparedStatement preparedStatement = conn.prepareStatement(sql);
		     ResultSet rs = preparedStatement.executeQuery()) {

			// Iterate over every row returned by the database
			while (rs.next()) {
				// Map the current row's data to a new DogDTO object
				DogDTO dog = mapResultSetToDogDto(rs);
				dogs.add(dog);
			}

		} catch (SQLException e) {
			throw new DogServiceException("Error while retrieving all dogs: " + e.getMessage(), e);
		}

		return dogs;
	}

	/**
	 * Retrieves a single record by its ID.
	 * @param id The ID of the record to retrieve.
	 * @return The specific DogDTO object if found.
	 * @throws DogNotFoundException if no dog is found with the given ID
	 */
	public DogDTO getDogById(int id) {
		String sql = "SELECT * FROM DOGS WHERE ID = ?";

		try (Connection conn = getConnection();
		     PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

			preparedStatement.setInt(1, id);

			try (ResultSet rs = preparedStatement.executeQuery()) {

				if (rs.next()) {
					DogDTO dog = mapResultSetToDogDto(rs);
					return dog;
				} else {
					throw new DogNotFoundException("Dog with ID " + id + " not found");
				}
			}

		} catch (SQLException e) {
			throw new DogServiceException("Error while retrieving dog with ID " + id + ": " + e.getMessage(), e);
		}
	}

	/**
	 * Saves a new entry to the 'DOGS' table.
	 *
	 * @param dogDto The DogDTO object containing the data to save.
	 * @return The saved DogDTO object with the generated ID.
	 */
	public DogDTO saveNewDog(DogDTO dogDto) {

		try {

			// Dog Name (Required/NOT NULL)
			String dogName = dogDto.getName();
			if (dogName == null || dogName.trim().isEmpty()) {
				throw new DogValidationException("Dog name is required.");
			}

			// Breed (Required/NOT NULL)
			String dogBreed = dogDto.getBreed();
			if (dogBreed == null || dogBreed.trim().isEmpty()) {
				throw new DogValidationException("Dog breed is required.");
			}

			// Supplier (Optional)
			String dogSupplier = dogDto.getSupplier();
			if (dogSupplier == null) {
				dogSupplier = "";
			}

			// 4. Gender (Optional)
			String dogGender = dogDto.getGender();
			if (dogGender == null) {
				dogGender = "";
			}

			// Status (Required - Must be one of the predefined values)
			String dogStatus = dogDto.getStatus();
			if (dogStatus == null || dogStatus.trim().isEmpty()) {
				throw new DogValidationException("Dog status is required.");
			}

			List<String> validStatuses = Arrays.asList("in training", "in service", "retired", "left");
			if (!validStatuses.contains(dogStatus.toLowerCase())) {
				throw new DogValidationException("Dog status must be one of the following: " + validStatuses);
			}

			// Leaving Reason (Optional)
			String dogLeavingReason = dogDto.getLeavingReason();
			if (dogLeavingReason == null) {
				dogLeavingReason = "";

			} else {
				List<String> validLeavingReason = Arrays.asList("transferred", "retired (put down)", "kia", "retired (re-homed)", "died");
				if (!validLeavingReason.contains(dogLeavingReason.toLowerCase())) {
					throw new DogValidationException("If provided, the leaving reason must be one of the following: " + validLeavingReason);
				}
			}

			// Kenneling Characteristics (Optional)
			String dogKennelingCharacteristics = dogDto.getKennelingCharacteristics();
			if (dogKennelingCharacteristics == null) {
				dogKennelingCharacteristics = "";
			}

			// BadgeID (Required/NOT NULL)
			Integer dogBadgeID = dogDto.getBadgeID();
			if (dogBadgeID == null) {
				throw new DogValidationException("Dog badge ID is required.");
			}
			// Check if the ID is negative
			if (dogBadgeID <= 0) {
				throw new DogValidationException("Dog badge ID must be a positive number.");
			}
			// Check if badgeID already exists
			String checkBadgeSql = "SELECT COUNT(*) FROM DOGS WHERE badgeID = ?";
			try (Connection conn = getConnection();
			     PreparedStatement checkStmt = conn.prepareStatement(checkBadgeSql)) {
			    checkStmt.setInt(1, dogBadgeID);
			    try (ResultSet rs = checkStmt.executeQuery()) {
			        if (rs.next() && rs.getInt(1) > 0) {
			            throw new DogValidationException("Dog badge ID already exists. Please check and try again or " +
					            "use the PUT method to update the existing record.");
			        }
			    }
			}

			// Birth Date (Optional)
			Date dogBirthDate = dogDto.getBirthDate();

			// Date Acquired (Optional)
			Date dogDateAcquired = dogDto.getDateAcquired();

			// Leaving Date (Optional)
			Date dogLeavingDate = dogDto.getLeavingDate();

			// Check dates - Leaving Date cannot be before Date Acquired
			if (dogLeavingDate != null && dogDateAcquired != null &&
			    dogLeavingDate.toLocalDate().isBefore(dogDateAcquired.toLocalDate())) {
				throw new DogValidationException("Leaving date cannot be before the acquisition date.");
			}

			// The SQL INSERT statement to add record to the database
			String sql = "INSERT INTO DOGS (name, breed, supplier, badgeID, gender, birthDate, dateAcquired, status, leavingDate, leavingReason, kennelingCharacteristics) " +
					"VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

			try (Connection conn = getConnection();
			     PreparedStatement preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

				preparedStatement.setString(1, dogName);
				preparedStatement.setString(2, dogBreed);
				preparedStatement.setString(3, dogSupplier);
				preparedStatement.setInt(4, dogBadgeID);
				preparedStatement.setString(5, dogGender);
				preparedStatement.setDate(6, dogBirthDate);
				preparedStatement.setDate(7, dogDateAcquired);
				preparedStatement.setString(8, dogStatus);
				preparedStatement.setDate(9, dogLeavingDate);
				preparedStatement.setString(10, dogLeavingReason);
				preparedStatement.setString(11, dogKennelingCharacteristics);

				// Execute the statement
				int affectedRows = preparedStatement.executeUpdate();

				if (affectedRows == 0) {
					throw new DogServiceException("Creating dog failed, no record added.");
				}

				try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
					if (generatedKeys.next()) {
						int generatedId = generatedKeys.getInt(1);
						// Return the complete record with the generated ID
						return getDogById(generatedId);
					} else {
						throw new DogServiceException("Creating dog failed, no ID obtained.");
					}
				}

			} catch (SQLException e) {
				throw new DogServiceException("Error while saving new record: " + e.getMessage(), e);
			}
		} catch (DogValidationException e) {
			throw e;
		} catch (Exception e) {
			throw new DogServiceException("Unexpected error: " + e.getMessage(), e);
		}
	}
		

	/**
	 * Updates an existing dog record in the DOGS table.
	 * Only updates fields that are provided (not null), keeping existing values for others.
	 * @param id The ID of the dog to update.
	 * @param dogDto The DogDTO containing the new data.
	 * @throws DogNotFoundException if no dog is found with the given ID
	 */
	public DogDTO updateDog(int id, DogDTO dogDto) {
		// First, get the existing record to preserve values for fields not being updated
		DogDTO existingDog = getDogById(id);

		// Validate only the fields that are being provided
		if (dogDto.getName() != null && dogDto.getName().trim().isEmpty()) {
			throw new DogValidationException("Name cannot be empty if provided.");
		}

		if (dogDto.getBreed() != null && dogDto.getBreed().trim().isEmpty()) {
			throw new DogValidationException("Breed cannot be empty if provided.");
		}

		if (dogDto.getBadgeID() != null && dogDto.getBadgeID() <= 0) {
			throw new DogValidationException("Badge ID must be a positive number if provided.");
		}

		if (dogDto.getStatus() != null) {
			if (dogDto.getStatus().trim().isEmpty()) {
				throw new DogValidationException("Status cannot be empty if provided.");
			}

			List<String> validStatuses = Arrays.asList("in training", "in service", "retired", "left");
			if (!validStatuses.contains(dogDto.getStatus().toLowerCase())) {
				throw new DogValidationException("Status must be one of the following: " + validStatuses);
			}
		}

		if (dogDto.getLeavingReason() != null) {
			if (dogDto.getLeavingReason().trim().isEmpty()) {
				throw new DogValidationException("Leaving Reason cannot be empty if provided.");
			}

			List<String> validLeavingReason = Arrays.asList("transferred", "retired (put down)", "kia", "retired (re-homed)","died");
			if (!validLeavingReason.contains(dogDto.getLeavingReason().toLowerCase())) {
				throw new DogValidationException("Leaving reason must be one of the following: " + validLeavingReason);
			}
		}

		// Build dynamic SQL based on which fields are provided
		StringBuilder sqlBuilder = new StringBuilder("UPDATE DOGS SET ");
		List<Object> parameters = new ArrayList<>();
		boolean hasUpdates = false;

		// Check each field and add to update if provided
		// When any fields are provided, set hasUpdates to true
		if (dogDto.getName() != null) {
			sqlBuilder.append("name = ?, ");
			parameters.add(dogDto.getName());
			hasUpdates = true;
		}

		if (dogDto.getBreed() != null) {
			sqlBuilder.append("breed = ?, ");
			parameters.add(dogDto.getBreed());
			hasUpdates = true;
		}

		if (dogDto.getSupplier() != null) {
			sqlBuilder.append("supplier = ?, ");
			parameters.add(dogDto.getSupplier());
			hasUpdates = true;
		}

		if (dogDto.getBadgeID() != null) {
			sqlBuilder.append("badgeID = ?, ");
			parameters.add(dogDto.getBadgeID());
			hasUpdates = true;
		}

		if (dogDto.getGender() != null) {
			sqlBuilder.append("gender = ?, ");
			parameters.add(dogDto.getGender());
			hasUpdates = true;
		}

		if (dogDto.getBirthDate() != null) {
			sqlBuilder.append("birthDate = ?, ");
			parameters.add(dogDto.getBirthDate());
			hasUpdates = true;
		}

		if (dogDto.getDateAcquired() != null) {
			sqlBuilder.append("dateAcquired = ?, ");
			parameters.add(dogDto.getDateAcquired());
			hasUpdates = true;
		}

		if (dogDto.getStatus() != null) {
			sqlBuilder.append("status = ?, ");
			parameters.add(dogDto.getStatus());
			hasUpdates = true;
		}

		if (dogDto.getLeavingDate() != null) {
			sqlBuilder.append("leavingDate = ?, ");
			parameters.add(dogDto.getLeavingDate());
			hasUpdates = true;
		}

		if (dogDto.getLeavingReason() != null) {
			sqlBuilder.append("leavingReason = ?, ");
			parameters.add(dogDto.getLeavingReason());
			hasUpdates = true;
		}

		if (dogDto.getKennelingCharacteristics() != null) {
			sqlBuilder.append("kennelingCharacteristics = ?, ");
			parameters.add(dogDto.getKennelingCharacteristics());
			hasUpdates = true;
		}

		// If no fields to update, return an error
		if (!hasUpdates) {
			throw new DogValidationException("No update values have been provided.");
		}

		// Remove the trailing comma and space, then add WHERE clause
		String sql = sqlBuilder.toString().replaceAll(", $", "") + " WHERE ID = ?";
		parameters.add(id);

		// Validate date logic if any dates are being updated
		Date finalBirthDate = dogDto.getBirthDate() != null ? dogDto.getBirthDate() : existingDog.getBirthDate();
		Date finalLeavingDate = dogDto.getLeavingDate() != null ? dogDto.getLeavingDate() : existingDog.getLeavingDate();
		Date finalDateAcquired = dogDto.getDateAcquired() != null ? dogDto.getDateAcquired() : existingDog.getDateAcquired();

		if (finalLeavingDate != null && finalDateAcquired != null &&
		    finalLeavingDate.toLocalDate().isBefore(finalDateAcquired.toLocalDate())) {
			throw new DogValidationException("Leaving date cannot be before the acquisition date.");
		}

		if (finalBirthDate != null && finalDateAcquired != null &&
		    finalBirthDate.toLocalDate().isAfter(finalDateAcquired.toLocalDate())) {
			throw new DogValidationException("Birth date cannot be after the acquisition date.");
		}

		try (Connection conn = getConnection();
		     PreparedStatement preparedStatement = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			// Set all parameters
			for (int i = 0; i < parameters.size(); i++) {
				Object param = parameters.get(i);
				if (param instanceof String) {
					preparedStatement.setString(i + 1, (String) param);
				} else if (param instanceof Integer) {
					preparedStatement.setInt(i + 1, (Integer) param);
				} else if (param instanceof Date) {
					preparedStatement.setDate(i + 1, (Date) param);
				}
			}

			int affectedRows = preparedStatement.executeUpdate();

			// Throw exception if no rows were updated (record not found)
			if (affectedRows == 0) {
				throw new DogNotFoundException("Dog with ID " + id + " not found for update");
			}

			try (ResultSet generatedKeys = preparedStatement.getGeneratedKeys()) {
				if (generatedKeys.next()) {
					int generatedId = generatedKeys.getInt(1);
					// Return the complete record with the generated ID
					return getDogById(generatedId);
				} else {
					throw new DogServiceException("Creating dog failed, no ID obtained.");
				}
			}
		} catch (SQLException e) {
			throw new DogServiceException("Error while updating dog with ID " + id + ": " + e.getMessage(), e);
		}
	}

	/**
	 * Performs a soft delete by marking the dog record with the current date.
	 * @param id The ID of the dog to soft-delete.
	 * @throws DogNotFoundException if no dog is found with the given ID
	 */
	public void markDogAsDeleted(int id) {

		String sql = "UPDATE DOGS SET dateDeleted = ? WHERE ID = ?";

		try (Connection conn = getConnection();
		     PreparedStatement preparedStatement = conn.prepareStatement(sql)) {

			// Set the dateDeleted parameter to the current system date
			preparedStatement.setDate(1, Date.valueOf(LocalDate.now()));
			preparedStatement.setInt(2, id);

			int affectedRows = preparedStatement.executeUpdate();

			// Throw exception if no rows were updated (record not found)
			if (affectedRows == 0) {
				throw new DogNotFoundException("Dog with ID " + id + " not found for deletion");
			}

		} catch (SQLException e) {
			throw new DogServiceException("Error while marking dog with ID " + id + " as deleted: " + e.getMessage(), e);
		}
	}

	/**
	 * Helper method to map the current row of a ResultSet to a DogDTO object.
	 */
	private DogDTO mapResultSetToDogDto(ResultSet rs) throws SQLException {
		DogDTO dog = new DogDTO();

		dog.setId((long) rs.getInt("ID"));

		// Map standard fields
		dog.setName(rs.getString("NAME"));
		dog.setBreed(rs.getString("BREED"));
		dog.setSupplier(rs.getString("SUPPLIER"));
		dog.setBadgeID(rs.getInt("BADGEID"));
		dog.setGender(rs.getString("GENDER"));
		dog.setStatus(rs.getString("STATUS"));

		if (rs.getDate("BIRTHDATE") != null) {
			dog.setBirthDate(rs.getDate("BIRTHDATE"));
		} else {
			dog.setBirthDate(null);
		}

		if (rs.getDate("DATEACQUIRED") != null) {
			dog.setDateAcquired(rs.getDate("DATEACQUIRED"));
		} else {
			dog.setDateAcquired(null);
		}

		if (rs.getDate("LEAVINGDATE") != null) {
			dog.setLeavingDate(rs.getDate("LEAVINGDATE"));
		} else {
			dog.setLeavingDate(null);
		}

		dog.setLeavingReason(rs.getString("LEAVINGREASON"));
		dog.setKennelingCharacteristics(rs.getString("KENNELINGCHARACTERISTICS"));
		dog.setDateDeleted(rs.getDate("DATEDELETED"));

		return dog;
	}
}
