package com.openclassrooms.starterjwt.mapper;

import com.openclassrooms.starterjwt.dto.TeacherDto;
import com.openclassrooms.starterjwt.models.Teacher;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TeacherMapperTest {

    private final TeacherMapper mapper = Mappers.getMapper(TeacherMapper.class);

    @Test
    void toEntity_and_toDto_withNull() {
        assertThat(mapper.toEntity((TeacherDto) null)).isNull();
        assertThat(mapper.toDto((Teacher) null)).isNull();
    }

    @Test
    void listMappings_handleNullAndEmpty() {
        assertThat(mapper.toEntity((List<TeacherDto>) null)).isNull();
        assertThat(mapper.toDto((List<Teacher>) null)).isNull();

        assertThat(mapper.toEntity(List.of())).isEmpty();
        assertThat(mapper.toDto(List.of())).isEmpty();
    }

    @Test
    void toEntity_and_toDto_withNonNull() {
        TeacherDto dto = new TeacherDto();
        dto.setId(1L);
        dto.setFirstName("John");
        dto.setLastName("Doe");

        Teacher entity = mapper.toEntity(dto);

        assertThat(entity).isNotNull();
        assertThat(entity.getId()).isEqualTo(1L);
        assertThat(entity.getFirstName()).isEqualTo("John");
        assertThat(entity.getLastName()).isEqualTo("Doe");

        TeacherDto back = mapper.toDto(entity);

        assertThat(back).isNotNull();
        assertThat(back.getId()).isEqualTo(1L);
        assertThat(back.getFirstName()).isEqualTo("John");
        assertThat(back.getLastName()).isEqualTo("Doe");
    }
}