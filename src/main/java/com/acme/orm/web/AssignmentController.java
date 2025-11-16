package com.acme.orm.web;

import com.acme.orm.domain.Assignment;
import com.acme.orm.domain.Submission;
import com.acme.orm.service.AssignmentService;
import com.acme.orm.service.SubmissionService;
import com.acme.orm.web.dto.CreateAssignmentRequest;
import com.acme.orm.web.dto.GradeSubmissionRequest;
import com.acme.orm.web.dto.SubmissionRequest;
import com.acme.orm.web.dto.SubmissionResponse;
import com.acme.orm.web.mapper.DtoMapper;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@Validated
@RequiredArgsConstructor
public class AssignmentController {

    private final AssignmentService assignmentService;
    private final SubmissionService submissionService;

    @PostMapping("/lessons/{lessonId}/assignments")
    public ResponseEntity<Long> createAssignment(
        @PathVariable Long lessonId,
        @Valid @RequestBody CreateAssignmentRequest request
    ) {
        Assignment assignment = assignmentService.createAssignment(lessonId, request);
        return ResponseEntity.created(URI.create("/api/assignments/" + assignment.getId())).body(assignment.getId());
    }

    @PostMapping("/assignments/{assignmentId}/submissions")
    public ResponseEntity<SubmissionResponse> submitAssignment(
        @PathVariable Long assignmentId,
        @Valid @RequestBody SubmissionRequest request
    ) {
        Submission submission = submissionService.submitAssignment(assignmentId, request.studentId(), request.content());
        SubmissionResponse response = DtoMapper.toSubmissionResponse(submission);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/submissions/{submissionId}/grade")
    public SubmissionResponse gradeSubmission(
        @PathVariable Long submissionId,
        @Valid @RequestBody GradeSubmissionRequest request
    ) {
        Submission submission = submissionService.gradeSubmission(submissionId, request.score(), request.feedback());
        return DtoMapper.toSubmissionResponse(submission);
    }

    @GetMapping("/users/{userId}/submissions")
    public List<SubmissionResponse> listSubmissions(@PathVariable Long userId) {
        return submissionService.findByStudent(userId)
            .stream()
            .map(DtoMapper::toSubmissionResponse)
            .collect(Collectors.toList());
    }
}

