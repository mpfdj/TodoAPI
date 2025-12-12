package jaeger.de.miel.TodoAPI.repository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
@SpringBootTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void findTasksByUserIdAndListId() {
        Long userId = 1L;
        Long listId = 1L;

        var tasks = taskRepository.findTasksByUserIdAndListId(userId, listId);
        tasks.forEach(System.out::println);

        assertEquals(18, tasks.size());
    }
}