package com.acme.orm;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.acme.orm.domain.AnswerOption;
import com.acme.orm.domain.Course;
import com.acme.orm.domain.Enrollment;
import com.acme.orm.domain.Module;
import com.acme.orm.domain.Quiz;
import com.acme.orm.domain.QuizSubmission;
import com.acme.orm.domain.Submission;
import com.acme.orm.domain.Tag;
import com.acme.orm.domain.User;
import com.acme.orm.domain.enums.UserRole;
import com.acme.orm.repository.CategoryRepository;
import com.acme.orm.repository.CourseRepository;
import com.acme.orm.repository.EnrollmentRepository;
import com.acme.orm.repository.QuizRepository;
import com.acme.orm.repository.QuizSubmissionRepository;
import com.acme.orm.repository.TagRepository;
import com.acme.orm.repository.UserRepository;
import com.acme.orm.repository.SubmissionRepository;
import com.acme.orm.service.AssignmentService;
import com.acme.orm.service.CourseService;
import com.acme.orm.service.EnrollmentService;
import com.acme.orm.service.QuizService;
import com.acme.orm.service.SubmissionService;
import com.acme.orm.web.dto.CourseDetailsResponse;
import com.acme.orm.web.dto.CreateAssignmentRequest;
import com.acme.orm.web.dto.CreateCourseRequest;
import com.acme.orm.web.dto.CreateQuizRequest;
import com.acme.orm.web.dto.EnrollRequest;
import com.acme.orm.web.dto.GradeSubmissionRequest;
import com.acme.orm.web.dto.QuizAttemptRequest;
import com.acme.orm.web.dto.SubmissionRequest;
import com.acme.orm.web.dto.SubmissionResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@AutoConfigureMockMvc
class PlatformIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:15-alpine");

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
    }

    @Autowired
    private CourseService courseService;
    @Autowired
    private AssignmentService assignmentService;
    @Autowired
    private SubmissionService submissionService;
    @Autowired
    private EnrollmentService enrollmentService;
    @Autowired
    private QuizService quizService;
    @Autowired
    private QuizRepository quizRepository;
    @Autowired
    private QuizSubmissionRepository quizSubmissionRepository;
    @Autowired
    private SubmissionRepository submissionRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CourseRepository courseRepository;
    @Autowired
    private EnrollmentRepository enrollmentRepository;
    @Autowired
    private CategoryRepository categoryRepository;
    @Autowired
    private TagRepository tagRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @AfterEach
    void cleanup() {
        quizSubmissionRepository.deleteAll();
        submissionRepository.deleteAll();
        quizRepository.deleteAll();
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        tagRepository.deleteAll();
        categoryRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void shouldCreateCourseWithModulesAndLessons() {
        Course course = seedCourseGraph();

        Course detailed = courseService.getCourseDetails(course.getId());
        assertThat(detailed.getModules()).hasSize(1);
        assertThat(detailed.getModules().get(0).getLessons()).hasSize(1);
    }

    @Test
    void enrollmentLifecycleWorks() {
        Course course = seedCourseGraph();
        User student = userRepository.save(sampleUser(UserRole.STUDENT));

        Enrollment enrollment = enrollmentService.enroll(course.getId(), student.getId());
        assertThat(enrollment.getId()).isNotNull();

        enrollmentService.unenroll(enrollment.getId());
        assertThat(enrollmentService.getCoursesForStudent(student.getId())).isEmpty();
    }

    @Test
    void submissionCrudFlow() {
        Course course = seedCourseGraph();
        User student = userRepository.save(sampleUser(UserRole.STUDENT));

        Module module = course.getModules().get(0);
        var lesson = module.getLessons().get(0);
        var assignment = assignmentService.createAssignment(lesson.getId(), new CreateAssignmentRequest(
            "Project",
            "Finish ORM mapping",
            LocalDate.now().plusDays(2),
            100
        ));

        Submission submission = submissionService.submitAssignment(assignment.getId(), student.getId(), "My solution");
        assertThat(submission.getId()).isNotNull();

        Submission graded = submissionService.gradeSubmission(submission.getId(), 95, "Great job");
        assertThat(graded.getScore()).isEqualTo(95);
    }

    @Test
    void lazyLoadingRequiresFetchJoin() {
        Course course = seedCourseGraph();

        Course plainCourse = courseService.getCourse(course.getId());
        assertThrows(LazyInitializationException.class, () -> plainCourse.getModules().size());

        Course eagerCourse = courseService.getCourseDetails(course.getId());
        assertThat(eagerCourse.getModules()).isNotEmpty();
    }

    @Test
    void quizFlowPersistsResults() {
        Course course = seedCourseGraph();
        User student = userRepository.save(sampleUser(UserRole.STUDENT));

        Quiz quiz = quizRepository
            .findDetailedById(quizRepository.findByModuleId(course.getModules().get(0).getId()).orElseThrow().getId())
            .orElseThrow();

        var question = quiz.getQuestions().get(0);
        Long correctOptionId = question.getOptions()
            .stream()
            .filter(AnswerOption::isCorrect)
            .map(AnswerOption::getId)
            .findFirst()
            .orElseThrow();

        QuizSubmission submission = quizService.takeQuiz(
            quiz.getId(),
            new QuizAttemptRequest(
                student.getId(),
                List.of(new QuizAttemptRequest.AnswerSelection(question.getId(), List.of(correctOptionId)))
            )
        );

        assertThat(submission.getScore()).isEqualTo(100);
        assertThat(quizService.getResultsForStudent(student.getId())).hasSize(1);
    }

    @Test
    void restEndpointsLifecycle() throws Exception {
        User teacher = userRepository.save(sampleUser(UserRole.TEACHER));
        User student = userRepository.save(sampleUser(UserRole.STUDENT));
        var category = categoryRepository.save(com.acme.orm.domain.Category.builder().name("REST-" + UUID.randomUUID()).build());
        Tag tag = tagRepository.save(com.acme.orm.domain.Tag.builder().name("TAG-" + UUID.randomUUID()).build());

        CreateCourseRequest request = new CreateCourseRequest(
            "REST Course",
            "REST description",
            category.getId(),
            teacher.getId(),
            LocalDate.now(),
            20,
            List.of(tag.getId()),
            List.of(new CreateCourseRequest.ModuleRequest(
                "REST Module",
                1,
                List.of(new CreateCourseRequest.LessonRequest("REST Lesson", "REST content", null))
            ))
        );

        MvcResult creationResult = mockMvc.perform(post("/api/courses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.modules[0].lessons[0].title").value("REST Lesson"))
            .andReturn();

        CourseDetailsResponse courseResponse = objectMapper.readValue(
            creationResult.getResponse().getContentAsByteArray(),
            CourseDetailsResponse.class);

        Long courseId = courseResponse.id();
        Long lessonId = courseResponse.modules().get(0).lessons().get(0).id();

        mockMvc.perform(post("/api/courses/" + courseId + "/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new EnrollRequest(student.getId()))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.studentId").value(student.getId()));

        mockMvc.perform(get("/api/courses/" + courseId + "/students"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(student.getId()));

        mockMvc.perform(post("/api/courses/" + courseId + "/enrollments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new EnrollRequest(student.getId()))))
            .andExpect(status().isBadRequest());

        Long assignmentId = Long.valueOf(mockMvc.perform(post("/api/lessons/" + lessonId + "/assignments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new CreateAssignmentRequest(
                    "REST HW",
                    "Solve REST task",
                    LocalDate.now().plusDays(3),
                    50
                ))))
            .andExpect(status().isCreated())
            .andReturn()
            .getResponse()
            .getContentAsString());

        MvcResult submissionResult = mockMvc.perform(post("/api/assignments/" + assignmentId + "/submissions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new SubmissionRequest(student.getId(), "REST solution"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.assignmentId").value(assignmentId))
            .andReturn();

        SubmissionResponse submissionResponse = objectMapper.readValue(
            submissionResult.getResponse().getContentAsByteArray(),
            SubmissionResponse.class);

        mockMvc.perform(patch("/api/submissions/" + submissionResponse.id() + "/grade")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new GradeSubmissionRequest(45, "Nice work"))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.score").value(45))
            .andExpect(jsonPath("$.feedback").value("Nice work"));

        mockMvc.perform(get("/api/users/" + student.getId() + "/submissions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].feedback").value("Nice work"));
    }

    private Course seedCourseGraph() {
        User teacher = userRepository.save(sampleUser(UserRole.TEACHER));
        var category = categoryRepository.save(com.acme.orm.domain.Category.builder().name("Test Category").build());
        var tag = tagRepository.save(com.acme.orm.domain.Tag.builder().name(UUID.randomUUID().toString()).build());

        CreateCourseRequest request = new CreateCourseRequest(
            "Course " + UUID.randomUUID(),
            "Description",
            category.getId(),
            teacher.getId(),
            LocalDate.now(),
            10,
            List.of(tag.getId()),
            List.of(new CreateCourseRequest.ModuleRequest(
                "Module 1",
                1,
                List.of(new CreateCourseRequest.LessonRequest("Lesson 1", "Content", null))
            ))
        );

        Course course = courseService.createCourse(request);
        Course graph = courseService.getCourseDetails(course.getId());

        quizService.createQuiz(graph.getModules().get(0).getId(), new CreateQuizRequest(
            "Quiz",
            10,
            List.of(new CreateQuizRequest.QuestionRequest(
                "Question",
                com.acme.orm.domain.enums.QuestionType.SINGLE_CHOICE,
                List.of(
                    new CreateQuizRequest.AnswerRequest("Correct", true),
                    new CreateQuizRequest.AnswerRequest("Wrong", false)
                )
            ))
        ));

        return graph;
    }

    private User sampleUser(UserRole role) {
        return User.builder()
            .name(role.name() + "-" + UUID.randomUUID())
            .email(role.name().toLowerCase() + "+" + UUID.randomUUID() + "@example.com")
            .role(role)
            .build();
    }
}

