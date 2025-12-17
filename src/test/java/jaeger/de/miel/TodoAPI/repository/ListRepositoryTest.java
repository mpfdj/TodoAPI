package jaeger.de.miel.TodoAPI.repository;


import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.show-sql=true",
        "logging.level.org.hibernate.SQL=DEBUG",
        "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE"
})
public class ListRepositoryTest {

    @Autowired
    private ListRepository listRepository;

    @Test
    public void testWithJoin() {
        Iterable<jaeger.de.miel.TodoAPI.entity.List> lists = listRepository.findAll();

        List<jaeger.de.miel.TodoAPI.entity.List> listList = new ArrayList<>();
        lists.forEach(listList::add);

        var list = listList.getFirst();
        var name = list.getOwner().getName();

        System.out.println(list);
        assertEquals("Alice Johnson", name);
    }

    @Test
    public void testFindListsByUserId() {
        List<jaeger.de.miel.TodoAPI.entity.List> lists = listRepository.findListsByOwner_Id(1L);
        lists.forEach(System.out::println);
        assertEquals(2, lists.size());
    }

    @Test
    public void testFindListsByUserIdNotFound() {
        List<jaeger.de.miel.TodoAPI.entity.List> lists = listRepository.findListsByOwner_Id(-1L);
        assertEquals(0, lists.size());
    }

    @Transactional
    @Test
    public void testDeleteByIdAndOwner_Id() {
        Long listId = 2L;
        Long ownerId = 1L;

        try {
            var numberOfRecordsDeleted = listRepository.deleteByIdAndOwner_Id(listId, ownerId);
            System.out.println("Number of records deleted: " + numberOfRecordsDeleted);
        } catch (EmptyResultDataAccessException e) {
            System.out.println("List not found");;
        }
    }

    @Test
    public void testWithDeleteCascade() {
        listRepository.deleteById(3L);
    }

    @Test
    public void testDeleteById() {
        listRepository.deleteById(66L);
    }

}
