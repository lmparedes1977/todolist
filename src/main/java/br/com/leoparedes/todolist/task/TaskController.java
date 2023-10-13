package br.com.leoparedes.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import br.com.leoparedes.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {

    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping
    public ResponseEntity create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        var userId = ((UUID) request.getAttribute("userId"));
        taskModel.setUserId(userId);

        var currentDate = LocalDateTime.now();
        
        if (currentDate.isAfter(taskModel.getStartAt())){
            return ResponseEntity.badRequest().body("A data de início e término devem ser maiores do que a data atual.");
        }
        if (taskModel.getStartAt().isAfter(taskModel.getEndAt())){
            return ResponseEntity.badRequest().body("A data de início deve ser maior do que a data de fim.");
        }

        var task = this.taskRepository.save(taskModel);
        return ResponseEntity.ok().body(task); 
    }

    @GetMapping
    public List<TaskModel> list(HttpServletRequest request) {
        var tasks = this.taskRepository.findByUserId((UUID) request.getAttribute("userId"));
        return tasks;
    }

    @PutMapping("/{taskId}")
    public ResponseEntity update(@RequestBody TaskModel taskModel, HttpServletRequest request, @PathVariable UUID taskId) {
        
        var task = this.taskRepository.findById(taskId).orElse(null);

        if (task == null) {
            return ResponseEntity.badRequest().body("Tarefa não encontrada.");
        }

        var userId = ((UUID) request.getAttribute("userId"));
        if (!task.getUserId().equals(userId)) {
            return ResponseEntity.badRequest().body("A tarefa não pertence ao usuário.");
        }
        
        Utils.copyNonNullProperties(taskModel, task);

        var updatedTask = this.taskRepository.save(task);
        
        return ResponseEntity.ok().body(updatedTask) ;
    }
    
}
