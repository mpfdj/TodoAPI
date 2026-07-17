

# tasks
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Tasks</title>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">

    <!-- Bootstrap 5.3 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

    <!-- Custom CSS (alleen voor specifieke overschrijvingen) -->
    <link rel="stylesheet" th:href="@{/css/custom.css}" />

    <!-- HTMX -->
    <script src="https://unpkg.com/htmx.org@1.9.10"></script>

    <style>
        .handleDragAndDrop {
            cursor: grab;
        }
        .handleDragAndDrop:active {
            cursor: grabbing;
        }
        /* Add visual feedback for debugging */
        .handleDragAndDrop.dragging {
            background-color: #ffcccc;
        }
    </style>
</head>
<body class="container py-4">

<!-- Header -->
<div class="d-flex justify-content-between align-items-center mb-4">
    <h1 class="display-5 fw-bold text-primary">
        Tasks for List: <span th:text="${listName}" class="text-secondary"></span>
    </h1>

    <!-- Back to Lists -->
    <a th:href="@{/ui/users/{userId}/lists(userId=${userId})}" class="btn btn-outline-secondary">
        ← Back to Lists
    </a>
</div>

<hr class="my-4">

<!-- Create Task Form -->
<div class="card mb-4 shadow-sm">
    <div class="card-header bg-primary text-white">
        <h2 class="h5 mb-0">Add New Task</h2>
    </div>
    <div class="card-body">
        <form th:action="@{/ui/users/{userId}/lists/{listId}/tasks(userId=${userId}, listId=${listId})}"
              method="post"
              class="row g-3">

            <div class="col-md-4">
                <label for="title" class="form-label">Task title</label>
                <input type="text"
                       class="form-control"
                       id="title"
                       name="title"
                       placeholder="Enter task title"
                       required />
            </div>

            <div class="col-md-4">
                <label for="description" class="form-label">Description</label>
                <input type="text"
                       class="form-control"
                       id="description"
                       name="description"
                       placeholder="Description (optional)" />
            </div>

            <div class="col-md-2">
                <label for="dueDate" class="form-label">Due Date</label>
                <input type="date"
                       class="form-control"
                       id="dueDate"
                       name="dueDate"
                       required
                       th:value="${#dates.format(#dates.createNow(), 'yyyy-MM-dd')}" />
            </div>

            <div class="col-md-1">
                <label for="priority" class="form-label">Priority</label>
                <select class="form-select" id="priority" name="priority" required>
                    <option value="1">1</option>
                    <option value="2">2</option>
                    <option value="3">3</option>
                    <option value="4">4</option>
                    <option value="5">5</option>
                </select>
            </div>

            <div class="col-md-1 d-flex align-items-end">
                <button type="submit" class="btn btn-primary w-100">
                    Add Task
                </button>
            </div>
        </form>
    </div>
</div>

<!-- Tasks Container - This will be replaced on delete -->
<div th:replace="~{fragments/tasks-container :: tasks-container(tasks=${tasks}, userId=${userId}, listId=${listId})}"></div>

<!-- DEBUG -->
<div th:replace="~{fragments/debug :: debug}"></div>

<!-- Bootstrap JavaScript Bundle (voor interactieve componenten) -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

<script th:src="@{/js/table-dragger.min.js}"></script>
<script>
    document.addEventListener('DOMContentLoaded', function() {
        var el = document.getElementById('tasksTable');
        console.log('Table element found:', el);

        // Check if handle elements exist
        var handles = document.querySelectorAll('.handle');
        console.log('Handle elements found:', handles.length);
        console.log('Handle elements:', handles);

        var TableDragger = tableDragger.default || tableDragger;
        console.log('TableDragger type:', typeof TableDragger);

        if (typeof TableDragger === 'function') {
            console.log('Initializing TableDragger...');
            var dragger = TableDragger(el, {
                mode: 'row',
                dragHandler: '.handleDragAndDrop',
                onlyBody: true,
                animation: 300
            });

            console.log('Dragger object:', dragger);

            // Add event listeners for debugging
            dragger.on('drag', function(el, mode) {
                console.log('Drag event - mode:', mode);
            });

            dragger.on('drop', function(from, to, el, mode) {
                console.log('Dropped from', from, 'to', to, 'mode:', mode);
            });

            dragger.on('shadowMove', function(oldIndex, newIndex, el, mode) {
                console.log('Shadow move from', oldIndex, 'to', newIndex);
            });

            console.log('TableDragger initialized successfully!');
            console.log('Drag handler selector: .handle');
        } else {
            console.error('TableDragger is not a function. Type:', typeof TableDragger);
            console.log('Available:', Object.keys(tableDragger));
        }
    });
</script>
</body>
</html>





# tasks-container.html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <!-- Bootstrap 5.3 CSS -->
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

    <!-- Custom CSS (voor eventuele overschrijvingen) -->
    <link rel="stylesheet" th:href="@{/css/custom.css}"/>
</head>
<body>

<div th:fragment="tasks-container(tasks, userId, listId)" id="tasks-container" class="mt-4">

    <!-- Header -->
    <div class="d-flex justify-content-between align-items-center mb-3">
        <h2 class="h4 mb-0">All Tasks</h2>
        <span class="badge bg-secondary rounded-pill" th:text="${#lists.size(tasks)} + ' tasks'"></span>
    </div>

    <!-- Sortable Tasks Table -->
    <div th:if="${not tasks.empty}">
        <div class="table-responsive">
            <table class="table table-hover align-middle" id="tasksTable">
                <thead class="table-light">
                <tr>
                    <th class="sortable-header" data-sort="status" style="width: 100px;">
                        Status <i class="bi bi-arrow-down-up sort-icon"></i>
                    </th>
                    <th class="sortable-header" data-sort="title" style="width: 40%;">
                        Title <i class="bi bi-arrow-down-up sort-icon"></i>
                    </th>
                    <th class="sortable-header" data-sort="description">
                        Description <i class="bi bi-arrow-down-up sort-icon"></i>
                    </th>
                    <th class="sortable-header" data-sort="priority" style="width: 100px;">
                        Priority <i class="bi bi-arrow-down-up sort-icon"></i>
                    </th>
                    <th class="sortable-header" data-sort="dueDate" style="width: 120px;">
                        Due Date <i class="bi bi-arrow-down-up sort-icon"></i>
                    </th>
                    <th class="text-center" style="width: 100px;">Actions</th>
                </tr>
                </thead>
                <tbody>

                    <th:block th:each="task : ${tasks}">

                        <th:block th:replace="~{fragments/task-item :: task-item(task=${task}, userId=${userId}, listId=${listId})}"></th:block>

                        <tr th:id="'task-edit-row-' + ${task.id}" style="display: none;">
                            <td colspan="6">
                                <div th:id="'task-edit-container-' + ${task.id}" class="edit-container">
                                </div>
                            </td>
                        </tr>

                    </th:block>
                </tbody>
            </table>
        </div>
    </div>

    <div th:if="${tasks.empty}" class="alert alert-info text-center py-5 my-3">
        <i class="bi bi-inbox fs-1 d-block mb-3"></i>
        <h4 class="alert-heading h5">No tasks yet</h4>
        <p class="mb-0">Create your first task using the form above!</p>
    </div>
</div>


<!-- Sortable Table JavaScript -->
<script></script>

<!-- Bootstrap Icons (optioneel, voor het icoontje) -->
<link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">

<!-- Bootstrap JS Bundle -->
<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>

</body>
</html>


# task-item.html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org" xmlns:hx-on="http://www.w3.org/1999/xhtml">
<head>
    <script src="https://unpkg.com/htmx.org@1.9.10"></script>
    <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css" rel="stylesheet">
    <link rel="stylesheet" href="https://cdn.jsdelivr.net/npm/bootstrap-icons@1.11.3/font/bootstrap-icons.min.css">
    <link rel="stylesheet" th:href="@{/css/custom.css}"/>
</head>
<body>

<tr th:fragment="task-item(task, userId, listId)"
    th:id="'task-' + ${task.id}"
    class='handleDragAndDrop'>

    <script>
        function handleClick(element) {
            const taskId = element.dataset.taskId;
            const userId = element.dataset.userId;
            const listId = element.dataset.listId;
            const taskTitle = element.dataset.taskTitle;
            const taskStatus = element.dataset.taskStatus;

            console.log('Task ID:', taskId);
            console.log('User ID:', userId);
            console.log('List ID:', listId);
            console.log('Title:', taskTitle);
            console.log('Status:', taskStatus);
        }

        // Edit toggle functie
        function toggleEdit(button) {
            const taskId = button.getAttribute('data-task-id');
            const editUrl = button.getAttribute('data-edit-url');
            const targetRow = document.getElementById('task-edit-row-' + taskId);
            const icon = button.querySelector('i');
            const containerId = 'task-edit-container-' + taskId;

            if (button.getAttribute('data-state') === 'open') {
                if (targetRow) targetRow.style.display = 'none';
                button.setAttribute('data-state', 'closed');
                icon.classList.remove('bi-x-circle');
                icon.classList.add('bi-pencil');
            } else {
                if (targetRow) {
                    targetRow.style.display = 'table-row';
                    htmx.ajax('GET', editUrl, {
                        target: '#' + containerId,
                        swap: 'innerHTML'
                    });
                }
                button.setAttribute('data-state', 'open');
                icon.classList.remove('bi-pencil');
                icon.classList.add('bi-x-circle');
            }
        }
    </script>

    <script>
        // Voeg dit toe aan je hoofdpagina (bijvoorbeeld list-detail.html of waar de tabel staat)
        document.body.addEventListener('htmx:afterSwap', function(event) {
            // Check of het een task update betreft
            if (event.detail.target && event.detail.target.id && event.detail.target.id.startsWith('task-')) {
                const taskId = event.detail.target.id.replace('task-', '');
                console.log('Task updated, closing edit form for task:', taskId);

                // Kleine vertraging om DOM te laten stabiliseren
                setTimeout(function() {
                    const editRow = document.getElementById('task-edit-row-' + taskId);
                    const editBtn = document.getElementById('edit-btn-' + taskId);

                    console.log('Found edit row:', editRow);
                    console.log('Found edit button:', editBtn);

                    if(editRow) {
                        editRow.style.display = 'none';
                    }

                    if(editBtn) {
                        editBtn.setAttribute('data-state', 'closed');
                        const icon = editBtn.querySelector('i');
                        if(icon) {
                            icon.classList.remove('bi-x-circle');
                            icon.classList.add('bi-pencil');
                        }
                    }
                }, 100);
            }
        });
    </script>

    <!-- Status cel -->
    <td th:id="'status-container-' + ${task.id}">
        <form th:hx-put="@{/ui/users/{userId}/lists/{listId}/tasks/{taskId}(userId=${userId}, listId=${listId}, taskId=${task.id}, source='task-item')}"
              th:hx-target="'#status-container-' + ${task.id}"
              th:hx-swap="outerHTML"
              th:hx-trigger="change"
              class="d-inline">
            <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
            <select name="status" class="form-select form-select-sm" style="width: auto;">
                <option value="todo" th:selected="${task.status == 'todo'}">📋 Todo</option>
                <option value="in_progress" th:selected="${task.status == 'in_progress'}">⚙️ In Progress</option>
                <option value="done" th:selected="${task.status == 'done'}">✅ Done</option>
                <option value="archived" th:selected="${task.status == 'archived'}">📦 Archived</option>
            </select>
        </form>
    </td>

    <!-- Title cel -->
    <td th:id="'title-container-' + ${task.id}">
            <span th:class="(${task.status == 'archived'} ? 'text-secondary text-decoration-line-through fw-bold' : 'fw-bold')"
                  th:text="${task.title}">Task Title</span>
    </td>

    <!-- Beschrijving cel -->
    <td>
        <span class="text-secondary small" th:text="${task.description}"></span>
    </td>

    <!-- Priority cel -->
    <td>
        <span class="text-secondary small" th:text="${task.priority}"></span>
    </td>

    <!-- Due Date cel -->
    <td>
        <span class="text-secondary small" th:text="${task.dueDate}"></span>
    </td>

    <!-- Actions cel -->
    <td>
        <div class="d-flex gap-2 align-items-center flex-shrink-0">
            <!-- Edit task button -->
            <button class="btn btn-sm btn-outline-primary"
                    th:id="'edit-btn-' + ${task.id}"
                    th:data-task-id="${task.id}"
                    th:data-edit-url="@{/ui/users/{userId}/lists/{listId}/tasks/{taskId}/edit-form(userId=${userId}, listId=${listId}, taskId=${task.id})}"
                    th:data-state="closed"
                    th:onclick="toggleEdit(this)">
                <i class="bi bi-pencil"></i> Edit
            </button>

            <!-- Delete task button -->
            <form class="delete-form d-inline"
                  th:hx-delete="@{/ui/users/{userId}/lists/{listId}/tasks/{taskId}(userId=${userId}, listId=${listId}, taskId=${task.id})}"
                  th:hx-target="'#tasks-container'"
                  th:hx-swap="outerHTML"
                  th:hx-confirm="'Delete this task?'">
                <input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
                <button type="submit" class="btn btn-sm btn-outline-danger">
                    <i class="bi bi-trash"></i> Delete
                </button>
            </form>

            <!-- Info button -->
            <button class="btn btn-sm btn-outline-info"
                    th:data-task-id="${task.id}"
                    th:data-user-id="${userId}"
                    th:data-list-id="${listId}"
                    th:data-task-title="${task.title}"
                    th:data-task-status="${task.status}"
                    th:onclick="handleClick(this)">
                <i class="bi bi-info-circle"></i> Info
            </button>
        </div>
    </td>
</tr>

<!-- Alleen de status update - GEEN extra wrapping -->
<th:block th:fragment="statusUpdate">
<td th:id="'status-container-' + ${task.id}">
<form th:hx-put="@{/ui/users/{userId}/lists/{listId}/tasks/{taskId}(userId=${userId}, listId=${listId}, taskId=${task.id}, source='task-item')}"
th:hx-target="'#status-container-' + ${task.id}"
th:hx-swap="outerHTML"
th:hx-trigger="change"
class="d-inline">
<input type="hidden" th:name="${_csrf.parameterName}" th:value="${_csrf.token}"/>
<select name="status" class="form-select form-select-sm" style="width: auto;">
<option value="todo" th:selected="${task.status == 'todo'}">📋 Todo</option>
<option value="in_progress" th:selected="${task.status == 'in_progress'}">⚙️ In Progress</option>
<option value="done" th:selected="${task.status == 'done'}">✅ Done</option>
<option value="archived" th:selected="${task.status == 'archived'}">📦 Archived</option>
</select>
</form>
</td>
</th:block>

<!-- Alleen de title update (OOB) -->
<th:block th:fragment="titleUpdate">
<td th:id="'title-container-' + ${task.id}"
th:hx-swap-oob="innerHTML">
<span th:class="(${task.status == 'archived'} ? 'text-secondary text-decoration-line-through fw-bold' : 'fw-bold')"
th:text="${task.title}">Task Title</span>
</td>
</th:block>

<!-- Beide updates samen -->
<th:block th:fragment="updateStatusAndTitle">
<th:block th:replace="~{fragments/task-item :: statusUpdate}"></th:block>
<th:block th:replace="~{fragments/task-item :: titleUpdate}"></th:block>
</th:block>

<script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"></script>
</body>
</html>



After pressing "Delete task button" in task-item.html table-dragger is not working anymore (drag and drop doesn't work anymore)