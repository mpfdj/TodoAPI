package jaeger.de.miel.TodoAPI.mapper;

import jaeger.de.miel.TodoAPI.dto.CreateTaskRequestDTO;
import jaeger.de.miel.TodoAPI.dto.TaskDTO;
import jaeger.de.miel.TodoAPI.dto.TaskStatus;
import jaeger.de.miel.TodoAPI.entity.Task;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskMapperTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Task taskMock;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CreateTaskRequestDTO createTaskRequestDTOMock;


    @Test
    public void testToDTO() {
        LocalDate dueDate = LocalDate.of(2025, 12, 31);
        Instant createdAt = toInstant("2025-12-31 12:00:00.000000");
        Instant updatedAt = toInstant("2025-12-31 13:00:00.000000");

        when(taskMock.getId()).thenReturn(1L);
        when(taskMock.getList().getId()).thenReturn(1L);
        when(taskMock.getCreator().getId()).thenReturn(1L);
        when(taskMock.getTitle()).thenReturn("title");
        when(taskMock.getDescription()).thenReturn("description");
        when(taskMock.getStatus()).thenReturn("status");
        when(taskMock.getDueDate()).thenReturn(dueDate);
        when(taskMock.getPriority()).thenReturn(1);
        when(taskMock.getCreatedAt()).thenReturn(createdAt);
        when(taskMock.getUpdatedAt()).thenReturn(updatedAt);
        when(taskMock.getCompletedAt()).thenReturn(null);

        TaskDTO taskDTO = TaskMapper.toDTO(taskMock);
        System.out.println(taskDTO);

        assertEquals(1L, taskDTO.getId());
        assertEquals(1L, taskDTO.getListId());
        assertEquals(1L, taskDTO.getUserId());
        assertEquals("title", taskDTO.getTitle());
        assertEquals("description", taskDTO.getDescription());
        assertEquals("status", taskDTO.getStatus());
        assertEquals(dueDate, taskDTO.getDueDate());
        assertEquals(createdAt, taskDTO.getCreatedAt());
        assertEquals(updatedAt, taskDTO.getUpdatedAt());
        assertNull(taskDTO.getCompletedAt());
    }

    Instant toInstant(String s) {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
        LocalDateTime ldt = LocalDateTime.parse(s, fmt);
        ZoneId zone = ZoneId.of("Europe/Amsterdam");
        return ldt.atZone(zone).toInstant();
    }


    @Test
    public void testToEntity() {
        Long userId = 1L;
        Long listId = 1L;
        LocalDate dueDate = LocalDate.of(2025, 12, 31);

        when(createTaskRequestDTOMock.getTitle()).thenReturn("title");
        when(createTaskRequestDTOMock.getDescription()).thenReturn("description");
        when(createTaskRequestDTOMock.getStatus()).thenReturn(TaskStatus.TODO);  // enum -> toString() expected in entity
        when(createTaskRequestDTOMock.getDueDate()).thenReturn(dueDate);
        when(createTaskRequestDTOMock.getPriority()).thenReturn(1);

        Instant before = Instant.now();

        Task task = TaskMapper.toEntity(userId, listId, createTaskRequestDTOMock);
        System.out.println(task);

        Instant after = Instant.now();

        // Assertions
        assertEquals(1L, task.getList().getId());
        assertEquals(1L, task.getCreator().getId());
        assertEquals("title", task.getTitle());
        assertEquals("description", task.getDescription());
        assertEquals("todo", task.getStatus());
        assertEquals("2025-12-31", task.getDueDate().toString());
        assertEquals(1, task.getPriority());

        // Timestamps assertions
        assertThat(task.getCreatedAt()).isBetween(before, after);
        assertThat(task.getUpdatedAt()).isEqualTo(task.getCreatedAt());

    }
}