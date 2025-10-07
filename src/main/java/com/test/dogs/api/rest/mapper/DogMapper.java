package com.test.dogs.api.rest.mapper;

import com.test.dogs.api.rest.model.DogDTO;
import com.test.dogs.api.rest.model.DogEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface DogMapper {

	// Map Entity to Model
	DogDTO toModel(DogEntity entity);

	// Map Model to Entity
	DogEntity toEntity(DogDTO model);

	// Map lists automatically
	List<DogDTO> toModel(List<DogEntity> entities);
}
