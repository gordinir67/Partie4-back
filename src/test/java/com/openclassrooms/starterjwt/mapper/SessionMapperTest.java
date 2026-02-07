package com.openclassrooms.starterjwt.mapper;

import com.openclassrooms.starterjwt.dto.SessionDto;
import com.openclassrooms.starterjwt.models.Session;
import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.models.User;
import com.openclassrooms.starterjwt.services.TeacherService;
import com.openclassrooms.starterjwt.services.UserService;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class SessionMapperTest {

    private final SessionMapper mapper = Mappers.getMapper(SessionMapper.class);

    @Test
    void toEntity_coversTeacherNullAndNotNull_usersNullEmpty_andUserNullBranch() {
        TeacherService teacherService = mock(TeacherService.class);
        UserService userService = mock(UserService.class);

        SessionMapperImpl impl = (SessionMapperImpl) mapper;
        impl.teacherService = teacherService;
        impl.userService = userService;

        // --- Cas 1 : teacher_id null + users null
        SessionDto dto1 = new SessionDto();
        dto1.setDescription("d1");
        dto1.setTeacher_id(null);
        dto1.setUsers(null);

        Session e1 = mapper.toEntity(dto1);

        assertThat(e1.getTeacher()).isNull();
        assertThat(e1.getUsers()).isEmpty();

        // --- Cas 2 : teacher_id non-null + users avec un id -> User + un id -> null
        Teacher t = Teacher.builder().id(10L).firstName("T").lastName("E").build();
        when(teacherService.findById(10L)).thenReturn(t);

        User u1 = User.builder()
                .id(1L).email("u1@x.com").firstName("U1").lastName("X")
                .password("p").admin(false)
                .build();
        when(userService.findById(1L)).thenReturn(u1);
        when(userService.findById(2L)).thenReturn(null);

        SessionDto dto2 = new SessionDto();
        dto2.setDescription("d2");
        dto2.setTeacher_id(10L);
        dto2.setUsers(List.of(1L, 2L));

        Session e2 = mapper.toEntity(dto2);

        assertThat(e2.getTeacher()).isNotNull();
        assertThat(e2.getTeacher().getId()).isEqualTo(10L);

        assertThat(e2.getUsers()).hasSize(2);
        assertThat(e2.getUsers().get(0)).isNotNull();
        assertThat(e2.getUsers().get(0).getId()).isEqualTo(1L);
        assertThat(e2.getUsers().get(1)).isNull();

        verify(teacherService).findById(10L);
        verify(userService).findById(1L);
        verify(userService).findById(2L);

        // --- Cas 3 : users vide
        SessionDto dto3 = new SessionDto();
        dto3.setDescription("d3");
        dto3.setTeacher_id(null);
        dto3.setUsers(List.of());

        Session e3 = mapper.toEntity(dto3);
        assertThat(e3.getUsers()).isEmpty();
    }

    @Test
    void toDto_coversUsersNullAndNotNull_andTeacherPresent() {
        Teacher t = Teacher.builder().id(99L).firstName("T").lastName("E").build();

        // --- Cas 1 : session.users null
        Session s1 = Session.builder()
                .description("x")
                .teacher(t)
                .users(null)
                .build();

        var d1 = mapper.toDto(s1);

        assertThat(d1.getTeacher_id()).isEqualTo(99L);
        assertThat(d1.getUsers()).isEmpty();

        // --- Cas 2 : session.users non-null
        User u = User.builder()
                .id(5L).email("a@b.com").firstName("A").lastName("B")
                .password("p").admin(false)
                .build();

        Session s2 = Session.builder()
                .description("y")
                .teacher(t)
                .users(List.of(u))
                .build();

        var d2 = mapper.toDto(s2);

        assertThat(d2.getTeacher_id()).isEqualTo(99L);
        assertThat(d2.getUsers()).containsExactly(5L);
    }

    @Test
    void toEntity_returnsNull_whenInputIsNull() {
        assertThat(mapper.toEntity((SessionDto) null)).isNull();
    }

    @Test
    void toDto_returnsNull_whenInputIsNull() {
        assertThat(mapper.toDto((Session) null)).isNull();
    }

    @Test
    void listMappings_returnNull_whenInputListIsNull() {
        assertThat(mapper.toEntity((List<SessionDto>) null)).isNull();
        assertThat(mapper.toDto((List<Session>) null)).isNull();
    }
}