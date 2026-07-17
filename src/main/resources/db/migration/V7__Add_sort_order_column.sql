ALTER TABLE task ADD COLUMN sort_order INT DEFAULT 0;

MERGE INTO task t
    USING (
        SELECT id,
               ROW_NUMBER() OVER (
                   PARTITION BY list_id
                   ORDER BY created_at, id
                   ) AS new_sort_order
        FROM task
    ) x
ON t.id = x.id
WHEN MATCHED THEN
    UPDATE SET t.sort_order = x.new_sort_order;