package net.devstudy.resume.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import net.devstudy.resume.dto.PracticDto;
import net.devstudy.resume.entity.Practic;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PracticMapper {

    @Mapping(target = "finish", expression = "java(entity.getFinishDate() != null)")
    @Mapping(target = "finishDate", source = "finishDate")
    PracticDto toDto(Practic entity);

    @Mapping(target = "finishDate", expression = "java(mapFinishDate(dto))")
    Practic toEntity(PracticDto dto);

    default java.time.LocalDate mapFinishDate(PracticDto dto) {
        if (dto == null) {
            return null;
        }
        Boolean finish = dto.getFinish();
        if (finish != null && !finish) {
            return null;
        }
        return dto.getFinishDate();
    }
}
