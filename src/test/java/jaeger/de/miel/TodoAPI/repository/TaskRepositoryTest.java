package jaeger.de.miel.TodoAPI.repository;

import jakarta.persistence.EntityManager;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.show-sql=true",
        "logging.level.org.hibernate.SQL=DEBUG",
        "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE"
})
class TaskRepositoryTest {


    @Autowired
    private EntityManager em;


    @Autowired
    private TaskRepository taskRepository;

    @Test
    public void testFindTasksByList_IdAndCreator_Id() {
        Long listId = 1L;
        Long userId = 1L;

        var tasks = taskRepository.findTasksByList_IdAndCreator_Id(listId, userId);
        tasks.forEach(System.out::println);
    }

    @Transactional
    @Test
    public void testDeleteByIdAndList_IdAndCreator_Id() {
        Long taskId = 1L;
        Long listId = 1L;
        Long userId = 12345L;

        try {
            var numberOfRecordsDeleted = taskRepository.deleteByIdAndList_IdAndCreator_Id(taskId, listId, userId);
            System.out.println("Number of records deleted: " + numberOfRecordsDeleted);
        } catch (EmptyResultDataAccessException e) {
            System.out.println("Task not found");;
        }
    }

    @Test
    public void testDeleteById() {
        Long taskId = 1L;

        try {
            taskRepository.deleteById(taskId);
        } catch (EmptyResultDataAccessException e) {
            System.out.println("Task not found");;
        }
    }

}