package com.openclassrooms.starterjwt.services;

import com.openclassrooms.starterjwt.models.Teacher;
import com.openclassrooms.starterjwt.repository.TeacherRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class TeacherServiceTest {

    @Mock TeacherRepository teacherRepository;
    @InjectMocks TeacherService teacherService;

    @Test
    void findAll_shouldReturnAllTeachers() {
        when(teacherRepository.findAll()).thenReturn(List.of(new Teacher(), new Teacher()));

        List<Teacher> result = teacherService.findAll();

        assertEquals(2, result.size());
        verify(teacherRepository).findAll();
    }

    @Test
    void findById_shouldReturnTeacher_whenExists() {
        Teacher t = new Teacher().setId(1L);
        when(teacherRepository.findById(1L)).thenReturn(Optional.of(t));

        Teacher result = teacherService.findById(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(teacherRepository).findById(1L);
    }

    @Test
    void findById_shouldReturnNull_whenMissing() {
        when(teacherRepository.findById(1L)).thenReturn(Optional.empty());

        Teacher result = teacherService.findById(1L);

        assertNull(result);
        verify(teacherRepository).findById(1L);
    }
}