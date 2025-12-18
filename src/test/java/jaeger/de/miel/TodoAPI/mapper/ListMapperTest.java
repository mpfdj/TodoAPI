package jaeger.de.miel.TodoAPI.mapper;

import jaeger.de.miel.TodoAPI.dto.CreateListRequestDTO;
import jaeger.de.miel.TodoAPI.dto.ListDTO;
import jaeger.de.miel.TodoAPI.entity.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ListMapperTest {

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private List listMock;

    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private CreateListRequestDTO createListRequestDTOMock;


    @Test
    public void testToDTO() {
        when(listMock.getId()).thenReturn(1L);
        when(listMock.getOwner().getId()).thenReturn(1L);
        when(listMock.getName()).thenReturn("name");
        when(listMock.getDescription()).thenReturn("description");

        ListDTO listDTO = ListMapper.toDTO(listMock);

        assertEquals(1L, listDTO.getId());
        assertEquals(1L, listDTO.getUserId());
        assertEquals("name", listDTO.getName());
        assertEquals("description", listDTO.getDescription());
    }


    @Test
    public void testToEntity() {
        when(createListRequestDTOMock.getName()).thenReturn("name");
        when(createListRequestDTOMock.getDescription()).thenReturn("description");

        Instant before = Instant.now();

        List list = ListMapper.toEntity(1L, createListRequestDTOMock);
        System.out.println(list);

        Instant after = Instant.now();

        // Assertions
        assertEquals(1L, list.getOwner().getId());
        assertEquals("name", list.getName());
        assertEquals("description", list.getDescription());

        // Timestamps assertions
        assertThat(list.getCreatedAt()).isBetween(before, after);
        assertThat(list.getUpdatedAt()).isEqualTo(list.getCreatedAt());
    }

}
