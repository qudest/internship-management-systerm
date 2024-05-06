package com.ds.ims.api.controller;

import com.ds.ims.api.dto.*;
import com.ds.ims.api.service.*;
import com.ds.ims.api.utils.ApiPaths;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@RestController
@RequestMapping(ApiPaths.ADMIN)
public class AdminController {
    AuthService authService;
    InternshipService internshipService;
    LessonService lessonService;
    TaskService taskService;
    InternshipRequestService internshipRequestService;
    UserTaskService userTaskService;
    MessageService messageService;

    @PostMapping(ApiPaths.INTERNSHIPS)
    public ResponseEntity<?> createInternship(@RequestBody InternshipDto internshipDto) {
        return internshipService.createInternship(internshipDto);
    }

    @PutMapping(ApiPaths.INTERNSHIP_BY_ID)
    public ResponseEntity<?> updateInternship(@PathVariable Long id, @RequestBody InternshipDto internshipDto) {
        return internshipService.updateInternship(id, internshipDto);
    }

    @DeleteMapping(ApiPaths.INTERNSHIP_BY_ID)
    public ResponseEntity<?> deleteInternship(@PathVariable Long id) {
        return internshipService.deleteInternship(id);
    }

    @PostMapping(ApiPaths.LESSONS)
    public ResponseEntity<?> createLesson(@PathVariable Long id, @RequestBody LessonDto lessonDto) {
        return lessonService.createLesson(id, lessonDto);
    }

    @PutMapping(ApiPaths.LESSON_BY_ID)
    public ResponseEntity<?> updateLesson(@PathVariable Long id, @PathVariable Long lessonId, @RequestBody LessonDto lessonDto) {
        return lessonService.updateLesson(id, lessonId, lessonDto);
    }

    @DeleteMapping(ApiPaths.LESSON_BY_ID)
    public ResponseEntity<?> deleteLesson(@PathVariable Long id, @PathVariable Long lessonId) {
        return lessonService.deleteLesson(id, lessonId);
    }

    @PostMapping(ApiPaths.TASKS)
    public ResponseEntity<?> createTask(@PathVariable Long id, @PathVariable Long lessonId, @RequestBody CreatingTaskDto creatingTaskDto) {
        TaskDto task = taskService.createTask(id, lessonId, creatingTaskDto);
        return userTaskService.forkTaskForInternshipUsers(id, lessonId, task.getId());
    }

    @DeleteMapping(ApiPaths.TASK_BY_ID)
    public ResponseEntity<?> deleteTask(@PathVariable Long id, @PathVariable Long lessonId, @PathVariable Long taskId) {
        return taskService.deleteTask(id, lessonId, taskId);
    }

    @GetMapping(ApiPaths.REQUESTS)
    public List<RequestDto> getPendingRequestsForInternship(@PathVariable("id") Long internshipId) {
        return internshipRequestService.getPendingInternshipRequestsByInternshipId(internshipId);
    }

    @PutMapping(ApiPaths.REQUEST_BY_ID)
    public ResponseEntity<?> considerRequest(@PathVariable("id") Long internshipId, @PathVariable("requestId") Long requestId, @RequestParam("isAccepted") boolean isAccepted) {
        return internshipRequestService.considerRequest(internshipId, requestId, isAccepted);
    }

    @GetMapping(ApiPaths.GRADE)
    public List<GradeDto> getGrade(@PathVariable Long id) {
        return userTaskService.getUsersGrade(id);
    }

    @GetMapping("/commits")
    public ResponseEntity<?> getCommits() {
        return ResponseEntity.ok(userTaskService.getFreshCommits());
    }

    @GetMapping("/messages")
    public List<MessageDto> getMessages() {
        return messageService.getMessages(authService.getAuthenticatedAccountId());
    }

    @PutMapping("/review/{userTaskId}")
    public ResponseEntity<?> reviewTask(@PathVariable Long userTaskId, @RequestBody TaskReviewDto taskReviewDto) {
        return userTaskService.reviewTask(userTaskId, taskReviewDto, authService.getAuthenticatedAccountId());
    }
}
