The error:

    InvalidDataAccessApiUsageException: No EntityManager with actual transaction available for current thread - cannot reliably process 'remove' call

means **your delete operation is running outside of a transaction**. JPA requires an active transaction for any `remove` or `delete` operation.

***

### ✅ Why it happens

*   `@SpringBootTest` loads the full context but **does not make test methods transactional by default**.
*   Your repository method `deleteByIdAndOwner_Id(...)` is a **derived delete query**, which internally calls `EntityManager.remove()`. Without a transaction, Spring throws this exception.

***

### ✅ How to fix

Add **`@Transactional`** to your test method or class:

```java
import jakarta.transaction.Transactional;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.show-sql=true",
        "logging.level.org.hibernate.SQL=DEBUG",
        "logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE"
})
@Transactional // ✅ Ensures the test runs inside a transaction
public class ListRepositoryTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private ListRepository listRepository;

    @Test
    public void testDeleteByIdAndOwner_Id() {
        Long listId = 2L;
        Long ownerId = 1L;

        try {
            long numberOfRecordsDeleted = listRepository.deleteByIdAndOwner_Id(listId, ownerId);
            System.out.println("Number of records deleted: " + numberOfRecordsDeleted);

            em.flush(); // ✅ Forces SQL execution before assertions
        } catch (EmptyResultDataAccessException e) {
            System.out.println("List not found");
        }
    }
}
```

***

### ✅ Best Practices

1.  **Use `@Transactional` for write operations in tests**  
    Without it, JPA cannot manage persistence context for deletes.

2.  **Flush after delete**  
    Call `em.flush()` or `listRepository.flush()` (if using `JpaRepository`) to force SQL execution before assertions.

3.  **Consider `@DataJpaTest` for repository tests**  
    It automatically makes tests transactional and rolls back after each test.

***

### ✅ Alternative: Explicit JPQL delete

If you use `@Modifying @Query` for delete, annotate the method with `@Transactional`:

```java
@Modifying
@Transactional
@Query("DELETE FROM List l WHERE l.id = :listId AND l.owner.id = :ownerId")
int deleteByIds(@Param("listId") Long listId, @Param("ownerId") Long ownerId);
```

***

👉 Do you want me to give you a **full working example** of this test that:

*   Inserts a `List` entity with an owner
*   Deletes it using `deleteByIdAndOwner_Id`
*   Asserts deletion worked?
