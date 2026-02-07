package com.openclassrooms.starterjwt.mapper;

import com.openclassrooms.starterjwt.dto.UserDto;
import com.openclassrooms.starterjwt.models.User;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    @Test
    void mapsBothDirections_andLists_andNulls() {
        UserMapperImpl mapper = new UserMapperImpl();

        User user = User.builder()
                .id(1L)
                .email("a@b.com")
                .firstName("A")
                .lastName("B")
                .password("pwd")
                .admin(false)
                .build();

        UserDto dto = mapper.toDto(user);
        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getEmail()).isEqualTo("a@b.com");

        User roundtrip = mapper.toEntity(dto);
        assertThat(roundtrip.getEmail()).isEqualTo("a@b.com");

        assertThat(mapper.toDto(List.of(user))).hasSize(1);
        assertThat(mapper.toEntity(List.of(dto))).hasSize(1);

        // branches MapStruct (null guards) — important
        assertThat(mapper.toDto((User) null)).isNull();
        assertThat(mapper.toEntity((UserDto) null)).isNull();
        assertThat(mapper.toDto((List<User>) null)).isNull();
        assertThat(mapper.toEntity((List<UserDto>) null)).isNull();
    }
}