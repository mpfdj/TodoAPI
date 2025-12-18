package jaeger.de.miel.TodoAPI.repository;

import jaeger.de.miel.TodoAPI.entity.AppUser;
import jaeger.de.miel.TodoAPI.entity.List;
import jaeger.de.miel.TodoAPI.entity.Task;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import java.time.Instant;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
//@TestPropertySource(properties = {
//        "spring.jpa.show-sql=true",
//        "logging.level.org.hibernate.SQL=DEBUG",
//        "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE"
//})
class TaskRepositoryTest {

    @Autowired
    private TaskRepository taskRepository;


    @Transactional
    @Test
    public void testFindTasksByList_IdAndCreator_Id() {
        Long listId = 1L;
        Long userId = 1L;

        var tasks = taskRepository.findTasksByList_IdAndCreator_Id(listId, userId);
        tasks.forEach(System.out::println);

        assertFalse(tasks.isEmpty());
    }


    @Transactional
    @Test
    public void testDeleteByIdAndList_IdAndCreator_Id() {
        Long listId = 1L;
        Long userId = 1L;

        var task = createTask(listId, userId);
        var created = taskRepository.save(task);
        Long taskId = created.getId();

        System.out.println(created);
        System.out.println("taskId: " + taskId);

        var numberOfRecordsDeleted = taskRepository.deleteByIdAndList_IdAndCreator_Id(taskId, listId, userId);
        System.out.println("Number of records deleted: " + numberOfRecordsDeleted);
        assertTrue(numberOfRecordsDeleted > 0);
    }


    private Task createTask(Long ownerId, Long listId) {
        List list = new List();
        list.setId(listId);

        AppUser creator = new AppUser();
        creator.setId(ownerId);

        LocalDate dueDate = LocalDate.of(2025, 12, 31);
        Instant now = Instant.now();

        var task = new Task();
        task.setList(list);
        task.setCreator(creator);
        task.setTitle("task title test");
        task.setDescription("task description test");
        task.setStatus("todo");
        task.setDueDate(dueDate);
        task.setPriority(1);
        task.setCreatedAt(now);
        task.setUpdatedAt(now);
        return task;
    }
}