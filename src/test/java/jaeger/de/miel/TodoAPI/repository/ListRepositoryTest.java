package jaeger.de.miel.TodoAPI.repository;


import jaeger.de.miel.TodoAPI.entity.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
//@TestPropertySource(properties = {
//        "spring.jpa.show-sql=true",
//        "logging.level.org.hibernate.SQL=DEBUG",
//        "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE"
//})
public class ListRepositoryTest {

    @Autowired
    private ListRepository listRepository;


    @Transactional
    @Test
    public void testFindListsByOwner_Id() {
        List<jaeger.de.miel.TodoAPI.entity.List> lists = listRepository.findListsByOwner_Id(1L);
        lists.forEach(System.out::println);
        assertFalse(lists.isEmpty());
    }


    @Transactional
    @Test
    public void testFindListsByOwner_IdNotFound() {
        List<jaeger.de.miel.TodoAPI.entity.List> lists = listRepository.findListsByOwner_Id(-1L);
        boolean isEmpty = lists.isEmpty();
        System.out.println("Lists is empty: " + isEmpty);
        assertTrue(isEmpty);
    }


    @Transactional
    @Test
    public void testFindListsByOwner_IdAndNameIgnoreCase() {
        Long ownerId = 1L;
        var list = createList(ownerId);

        var created = listRepository.save(list);
        Long listId = created.getId();

        System.out.println("Created list with id: " + listId);
        assertTrue(listId > 0);

        boolean exists = listRepository.existsByOwner_IdAndNameIgnoreCase(ownerId, "LiSt NaMe TeSt");
        assertTrue(exists);
    }


    @Transactional
    @Test
    public void testDeleteByIdAndOwner_Id() {
        Long ownerId = 1L;
        var list = createList(ownerId);

        var created = listRepository.save(list);
        Long listId = created.getId();

        System.out.println("Created list with id: " + listId);
        assertTrue(listId > 0);

        var numberOfRecordsDeleted = listRepository.deleteByIdAndOwner_Id(listId, ownerId);
        System.out.println("Number of records deleted: " + numberOfRecordsDeleted);
        assertTrue(numberOfRecordsDeleted > 0);
    }


    private jaeger.de.miel.TodoAPI.entity.List createList(Long ownerId) {
        AppUser owner = new AppUser();
        owner.setId(ownerId);
        Instant now = Instant.now();

        var list = new jaeger.de.miel.TodoAPI.entity.List();
        list.setOwner(owner);
        list.setName("list name test");
        list.setDescription("list description test");
        list.setCreatedAt(now);
        list.setUpdatedAt(now);
        return list;
    }

}
