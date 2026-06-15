package kz.alash.examreservation.mapper;

import kz.alash.examreservation.dto.request.StudentCreateRequest;
import kz.alash.examreservation.dto.request.StudentUpdateRequest;
import kz.alash.examreservation.dto.response.StudentResponse;
import kz.alash.examreservation.entity.Student;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StudentMapper {

    StudentResponse toDto(Student entity);

    @Mapping(target = "reservations", ignore = true)
    Student toEntity(StudentCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "reservations", ignore = true)
    void updateEntity(StudentUpdateRequest request, @MappingTarget Student entity);
}
