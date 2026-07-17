Currently, your migration sets:

```sql
UPDATE task
SET sort_order = id
WHERE sort_order = 0;
```

That makes `sort_order` globally increase across all tasks because it uses the task's identity value.

If you want the ordering to start at **1 for each list**, populate `sort_order` per `list_id` using a window function:

```sql
ALTER TABLE task ADD COLUMN sort_order INT DEFAULT 0;

UPDATE task t
SET sort_order = x.sort_order
FROM (
    SELECT id,
           ROW_NUMBER() OVER (
               PARTITION BY list_id
               ORDER BY created_at, id
           ) AS sort_order
    FROM task
) x
WHERE t.id = x.id;
```

This gives:

```text
List 1:
Task A -> 1
Task B -> 2
Task C -> 3

List 2:
Task D -> 1
Task E -> 2
```

For **new tasks**, don't use a fixed default of `0`. Instead, when inserting, calculate the next position within the list:

```sql
SELECT COALESCE(MAX(sort_order), 0) + 1
FROM task
WHERE list_id = :listId;
```

Then insert:

```sql
INSERT INTO task (..., list_id, sort_order)
VALUES (..., :listId, :nextSortOrder);
```

That ensures every new list starts at `1`, and additional tasks are appended to the end of that specific list rather than continuing a global sequence.
