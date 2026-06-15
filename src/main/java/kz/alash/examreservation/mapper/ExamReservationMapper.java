package kz.alash.examreservation.mapper;

import kz.alash.examreservation.dto.request.ExamReservationCreateRequest;
import kz.alash.examreservation.dto.request.ExamReservationUpdateRequest;
import kz.alash.examreservation.dto.response.ExamReservationResponse;
import kz.alash.examreservation.entity.ExamReservation;
import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ExamReservationMapper {

    @Mapping(target = "studentId", source = "student.id")
    ExamReservationResponse toDto(ExamReservation entity);

    @Mapping(target = "student", ignore = true)
    @Mapping(target = "status", ignore = true)
    ExamReservation toEntity(ExamReservationCreateRequest request);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "student", ignore = true)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(ExamReservationUpdateRequest request, @MappingTarget ExamReservation entity);
}
