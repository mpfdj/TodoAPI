Hier is de complete implementatie voor **Java Spring Boot** om de gesleepte volgorde van je todo-lijst op te slaan met TableDragger.

## Backend: Spring Boot Controller

### 1. Entity met sortOrder veld

```java
package jaeger.de.miel.TodoAPI.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "todo_items")
public class TodoItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    private String title;
    private String description;
    private String status;
    
    @Column(name = "sort_order")
    private Integer sortOrder = 0;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
```

### 2. DTO voor order update

```java
package jaeger.de.miel.TodoAPI.dto;

import lombok.Data;
import java.util.List;

@Data
public class ReorderRequestDTO {
    private List<Long> ids;
}
```

### 3. Repository

```java
package jaeger.de.miel.TodoAPI.repository;

import jaeger.de.miel.TodoAPI.entity.TodoItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TodoItemRepository extends JpaRepository<TodoItem, Long> {
}
```

### 4. Service

```java
package jaeger.de.miel.TodoAPI.service;

import jaeger.de.miel.TodoAPI.entity.TodoItem;
import jaeger.de.miel.TodoAPI.repository.TodoItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoItemService {
    
    private final TodoItemRepository todoItemRepository;
    
    @Transactional
    public void reorderItems(List<Long> ids) {
        for (int i = 0; i < ids.size(); i++) {
            Long id = ids.get(i);
            TodoItem item = todoItemRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Item not found with id: " + id));
            item.setSortOrder(i);
            todoItemRepository.save(item);
        }
    }
    
    public List<TodoItem> getAllItemsSorted() {
        return todoItemRepository.findAll()
                .stream()
                .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                .toList();
    }
}
```

### 5. Controller

```java
package jaeger.de.miel.TodoAPI.controller;

import jaeger.de.miel.TodoAPI.dto.ReorderRequestDTO;
import jaeger.de.miel.TodoAPI.entity.TodoItem;
import jaeger.de.miel.TodoAPI.service.TodoItemService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/todo")
@RequiredArgsConstructor
public class TodoItemController {
    
    private final TodoItemService todoItemService;
    
    @GetMapping("/items")
    public ResponseEntity<List<TodoItem>> getItems() {
        return ResponseEntity.ok(todoItemService.getAllItemsSorted());
    }
    
    @PostMapping("/reorder")
    public ResponseEntity<Map<String, String>> reorderItems(@RequestBody ReorderRequestDTO request) {
        todoItemService.reorderItems(request.getIds());
        
        Map<String, String> response = new HashMap<>();
        response.put("message", "Volgorde succesvol bijgewerkt");
        response.put("status", "success");
        return ResponseEntity.ok(response);
    }
}
```

### 6. Optioneel: Bulk update met JPA (efficiënter)

```java
@Service
@RequiredArgsConstructor
public class TodoItemService {
    
    private final TodoItemRepository todoItemRepository;
    private final EntityManager entityManager;
    
    @Transactional
    public void reorderItemsBulk(List<Long> ids) {
        for (int i = 0; i < ids.size(); i++) {
            TodoItem item = entityManager.getReference(TodoItem.class, ids.get(i));
            item.setSortOrder(i);
        }
        // Hibernate flushed automatisch aan het einde van de transactie
    }
}
```

### 7. Optioneel: Met custom query (meest efficiënt)

```java
@Repository
public interface TodoItemRepository extends JpaRepository<TodoItem, Long> {
    
    @Modifying
    @Query("UPDATE TodoItem t SET t.sortOrder = :order WHERE t.id = :id")
    void updateSortOrder(@Param("id") Long id, @Param("order") Integer order);
}

@Service
@RequiredArgsConstructor
public class TodoItemService {
    
    private final TodoItemRepository todoItemRepository;
    
    @Transactional
    public void reorderItems(List<Long> ids) {
        for (int i = 0; i < ids.size(); i++) {
            todoItemRepository.updateSortOrder(ids.get(i), i);
        }
    }
}
```

### 8. Exception handling (voor foutafhandeling)

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, String>> handleRuntimeException(RuntimeException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", ex.getMessage());
        error.put("status", "error");
        return ResponseEntity.badRequest().body(error);
    }
}
```

## Frontend: HTML met TableDragger + Spring Boot

```html
<!DOCTYPE html>
<html>
<head>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/table-dragger@1.0.3/dist/table-dragger.min.css">
    <style>
        .handle {
            cursor: grab;
            padding: 8px;
            background: #f0f0f0;
            border-radius: 4px;
            user-select: none;
        }
        .handle:active {
            cursor: grabbing;
        }
    </style>
</head>
<body>
    <table id="todo-table">
        <thead>
            <tr>
                <th>#</th>
                <th>Taak</th>
                <th>Status</th>
            </tr>
        </thead>
        <tbody id="table-body">
            <!-- Wordt geladen door JavaScript -->
        </tbody>
    </table>

    <div id="message" style="margin-top: 20px; display: none;"></div>

    <script src="https://cdn.jsdelivr.net/npm/table-dragger@1.0.3/dist/table-dragger.min.js"></script>
    <script>
        // 1. Laad items van de backend
        function loadItems() {
            fetch('/api/todo/items')
                .then(response => response.json())
                .then(items => {
                    const tbody = document.getElementById('table-body');
                    tbody.innerHTML = items.map(item => `
                        <tr data-id="${item.id}">
                            <td><span class="handle">↕</span></td>
                            <td>${item.title}</td>
                            <td>${item.status || 'todo'}</td>
                        </tr>
                    `).join('');
                    
                    // 2. Initialiseer TableDragger na laden
                    initDragger();
                })
                .catch(error => console.error('Fout bij laden:', error));
        }

        // 3. TableDragger initialiseren
        function initDragger() {
            const table = document.getElementById('todo-table');
            const dragger = tableDragger(table, {
                mode: 'row',
                dragHandler: '.handle',
                onlyBody: true
            });

            // 4. Event listener voor drop (na slepen)
            dragger.on('drop', function (from, to, el, mode) {
                const rows = table.querySelectorAll('tbody tr');
                const ids = Array.from(rows).map(row => row.dataset.id);

                // 5. Stuur naar Spring Boot backend
                fetch('/api/todo/reorder', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ ids: ids })
                })
                .then(response => response.json())
                .then(data => {
                    showMessage('✅ ' + data.message, 'success');
                })
                .catch(error => {
                    showMessage('❌ Fout bij opslaan: ' + error.message, 'error');
                    console.error('Error:', error);
                });
            });
        }

        // 6. Toon feedback message
        function showMessage(text, type) {
            const msg = document.getElementById('message');
            msg.textContent = text;
            msg.style.display = 'block';
            msg.style.padding = '10px';
            msg.style.borderRadius = '5px';
            msg.style.background = type === 'success' ? '#d4edda' : '#f8d7da';
            msg.style.color = type === 'success' ? '#155724' : '#721c24';
            msg.style.border = '1px solid ' + (type === 'success' ? '#c3e6cb' : '#f5c6cb');
            
            setTimeout(() => {
                msg.style.display = 'none';
            }, 3000);
        }

        // Start de applicatie
        loadItems();
    </script>
</body>
</html>
```

## SQL Migration (bijvoorbeeld Flyway of Liquibase)

```sql
-- V1__add_sort_order_column.sql
ALTER TABLE todo_items ADD COLUMN sort_order INT DEFAULT 0;

-- Update bestaande items met een initiële volgorde
UPDATE todo_items SET sort_order = id WHERE sort_order = 0;
```

## Volledig werkend voorbeeld met Thymeleaf

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Todo List</title>
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/table-dragger@1.0.3/dist/table-dragger.min.css">
    <link rel="stylesheet" th:href="@{/css/style.css}"/>
</head>
<body>
    <div class="container">
        <h1>📋 Todo List</h1>
        
        <table id="todo-table" class="table">
            <thead>
                <tr>
                    <th style="width: 50px;">Volgorde</th>
                    <th>Taak</th>
                    <th>Status</th>
                </tr>
            </thead>
            <tbody id="table-body">
                <tr th:each="item : ${items}" th:data-id="${item.id}">
                    <td><span class="handle">↕</span></td>
                    <td th:text="${item.title}"></td>
                    <td th:text="${item.status}"></td>
                </tr>
            </tbody>
        </table>
        
        <div id="message"></div>
    </div>
    
    <script src="https://cdn.jsdelivr.net/npm/table-dragger@1.0.3/dist/table-dragger.min.js"></script>
    <script th:inline="javascript">
        /*<![CDATA[*/
        document.addEventListener('DOMContentLoaded', function() {
            const table = document.getElementById('todo-table');
            
            if (!table || table.querySelectorAll('tbody tr').length === 0) {
                console.log('Geen items om te slepen');
                return;
            }
            
            const dragger = tableDragger(table, {
                mode: 'row',
                dragHandler: '.handle',
                onlyBody: true
            });
            
            dragger.on('drop', function (from, to, el, mode) {
                const rows = table.querySelectorAll('tbody tr');
                const ids = Array.from(rows).map(row => row.dataset.id);
                
                showMessage('Bezig met opslaan...', 'info');
                
                fetch('/api/todo/reorder', {
                    method: 'POST',
                    headers: { 'Content-Type': 'application/json' },
                    body: JSON.stringify({ ids: ids })
                })
                .then(response => response.json())
                .then(data => {
                    showMessage('✅ ' + data.message, 'success');
                })
                .catch(error => {
                    showMessage('❌ Fout: ' + error.message, 'error');
                });
            });
            
            function showMessage(text, type) {
                const msg = document.getElementById('message');
                msg.textContent = text;
                msg.className = 'alert alert-' + type;
                msg.style.display = 'block';
                
                setTimeout(() => {
                    msg.style.display = 'none';
                }, 3000);
            }
        });
        /*]]>*/
    </script>
</body>
</html>
```

## Samenvatting van de flow

```
1. Gebruiker sleept een rij in de tabel
         ↓
2. TableDragger 'drop' event wordt getriggerd
         ↓
3. JavaScript verzamelt alle IDs in de nieuwe volgorde
         ↓
4. POST request naar /api/todo/reorder met { "ids": [4, 1, 3, 2] }
         ↓
5. Spring Boot Controller ontvangt request
         ↓
6. Service update de sortOrder van elk item
         ↓
7. Response: { "message": "Volgorde succesvol bijgewerkt" }
         ↓
8. Feedback message wordt getoond in de frontend
```

Deze implementatie is volledig en klaar voor gebruik in je Spring Boot applicatie! 🎯