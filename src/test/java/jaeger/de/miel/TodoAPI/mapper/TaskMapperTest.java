package jaeger.de.miel.TodoAPI.mapper;

import jaeger.de.miel.TodoAPI.dto.TaskDTO;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskMapperTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private Task task;

//    @InjectMocks
//    private TaskMapper taskMapper;

    @Test
    public void testFieldMapping() {
        LocalDate dueDate = LocalDate.of(2025, 12, 31);
        Instant createdAt = toInstant("2025-12-31 12:00:00.000000");
        Instant updatedAt = toInstant("2025-12-31 13:00:00.000000");

        when(task.getId()).thenReturn(1L);
        when(task.getList().getId()).thenReturn(1L);
        when(task.getCreator().getId()).thenReturn(1L);
        when(task.getTitle()).thenReturn("title");
        when(task.getDescription()).thenReturn("description");
        when(task.getStatus()).thenReturn("status");
        when(task.getDueDate()).thenReturn(dueDate);
        when(task.getPriority()).thenReturn(1);
        when(task.getCreatedAt()).thenReturn(createdAt);
        when(task.getUpdatedAt()).thenReturn(updatedAt);
        when(task.getCompletedAt()).thenReturn(null);

        TaskDTO taskDTO = TaskMapper.toDTO(task);
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
}