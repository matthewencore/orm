package com.acme.orm.config;

import com.acme.orm.domain.Category;
import com.acme.orm.domain.Course;
import com.acme.orm.domain.Lesson;
import com.acme.orm.domain.Module;
import com.acme.orm.domain.Tag;
import com.acme.orm.domain.User;
import com.acme.orm.domain.enums.UserRole;
import com.acme.orm.repository.CategoryRepository;
import com.acme.orm.repository.TagRepository;
import com.acme.orm.repository.UserRepository;
import com.acme.orm.service.AssignmentService;
import com.acme.orm.service.CourseService;
import com.acme.orm.service.EnrollmentService;
import com.acme.orm.service.QuizService;
import com.acme.orm.web.dto.CreateAssignmentRequest;
import com.acme.orm.web.dto.CreateCourseRequest;
import com.acme.orm.web.dto.CreateQuizRequest;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final CourseService courseService;
    private final AssignmentService assignmentService;
    private final QuizService quizService;
    private final EnrollmentService enrollmentService;

    @Override
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return;
        }

        User teacher = userRepository.save(User.builder()
            .name("Professor Hibernate")
            .email("professor@example.com")
            .role(UserRole.TEACHER)
            .build());

        User student = userRepository.save(User.builder()
            .name("Student ORM")
            .email("student@example.com")
            .role(UserRole.STUDENT)
            .build());

        Category category = categoryRepository.save(Category.builder().name("Programming").build());
        Tag javaTag = tagRepository.save(Tag.builder().name("Java").build());
        Tag hibernateTag = tagRepository.save(Tag.builder().name("Hibernate").build());

        CreateCourseRequest.ModuleRequest lessonModule = new CreateCourseRequest.ModuleRequest(
            "Introduction to ORM",
            1,
            List.of(
                new CreateCourseRequest.LessonRequest("JPA Overview", "Understand ORM basics", null),
                new CreateCourseRequest.LessonRequest("Hibernate Session", "Dive deep into Session API", null)
            )
        );

        CreateCourseRequest request = new CreateCourseRequest(
            "ORM & Hibernate Bootcamp",
            "Full course that follows the project requirements",
            category.getId(),
            teacher.getId(),
            LocalDate.now().plusDays(3),
            30,
            List.of(javaTag.getId(), hibernateTag.getId()),
            List.of(lessonModule)
        );

        Course course = courseService.createCourse(request);
        enrollmentService.enroll(course.getId(), student.getId());

        Course persisted = courseService.getCourseDetails(course.getId());
        Module firstModule = persisted.getModules().get(0);
        Lesson firstLesson = firstModule.getLessons().get(0);

        assignmentService.createAssignment(firstLesson.getId(), new CreateAssignmentRequest(
            "First homework",
            "Implement entity mapping",
            LocalDate.now().plusDays(5),
            100
        ));

        quizService.createQuiz(firstModule.getId(), new CreateQuizRequest(
            "Module quiz",
            30,
            List.of(
                new CreateQuizRequest.QuestionRequest(
                    "What annotation maps an entity?",
                    com.acme.orm.domain.enums.QuestionType.SINGLE_CHOICE,
                    List.of(
                        new CreateQuizRequest.AnswerRequest("@Entity", true),
                        new CreateQuizRequest.AnswerRequest("@Column", false)
                    )
                )
            )
        ));
    }
}

