package jaeger.de.miel.TodoAPI.repository;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Transactional
@SpringBootTest
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
        List<jaeger.de.miel.TodoAPI.entity.List> lists = listRepository.findListsByUserId(1L);
        lists.forEach(list -> System.out.println(list));
        assertEquals(2, lists.size());
    }

    @Test
    public void testFindListsByUserIdNotFound() {
        List<jaeger.de.miel.TodoAPI.entity.List> lists = listRepository.findListsByUserId(-1L);
        assertEquals(0, lists.size());
    }

    @Test
    public void testWithDeleteCascade() {
        listRepository.deleteById(3L);
    }

}
