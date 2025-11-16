package com.acme.orm.service;

import com.acme.orm.domain.Category;
import com.acme.orm.domain.Course;
import com.acme.orm.domain.Lesson;
import com.acme.orm.domain.Module;
import com.acme.orm.domain.Tag;
import com.acme.orm.domain.User;
import com.acme.orm.domain.enums.UserRole;
import com.acme.orm.repository.CategoryRepository;
import com.acme.orm.repository.CourseRepository;
import com.acme.orm.repository.TagRepository;
import com.acme.orm.service.exception.BusinessException;
import com.acme.orm.service.exception.NotFoundException;
import com.acme.orm.web.dto.CreateCourseRequest;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final UserService userService;

    @Transactional
    public Course createCourse(CreateCourseRequest request) {
        User teacher = userService.getUserOrThrow(request.teacherId());

        if (teacher.getRole() != UserRole.TEACHER && teacher.getRole() != UserRole.ADMIN) {
            throw new BusinessException("Only teachers or admins can own a course");
        }

        Category category = categoryRepository
            .findById(request.categoryId())
            .orElseThrow(() -> new NotFoundException("Category %d not found".formatted(request.categoryId())));

        Course course = Course.builder()
            .title(request.title())
            .description(request.description())
            .category(category)
            .teacher(teacher)
            .startDate(request.startDate())
            .durationDays(request.durationDays())
            .build();

        Set<Tag> tags = new HashSet<>();
        if (request.tagIds() != null && !request.tagIds().isEmpty()) {
            List<Tag> foundTags = tagRepository.findAllById(request.tagIds());
            if (foundTags.size() != request.tagIds().size()) {
                throw new NotFoundException("Some tags were not found for the request");
            }
            tags.addAll(foundTags);
        }
        course.setTags(tags);

        List<Module> modules = new ArrayList<>();
        if (request.modules() != null) {
            for (CreateCourseRequest.ModuleRequest moduleRequest : request.modules()) {
                Module module = Module.builder()
                    .title(moduleRequest.title())
                    .orderIndex(moduleRequest.orderIndex())
                    .course(course)
                    .build();

                List<Lesson> lessons = new ArrayList<>();
                if (moduleRequest.lessons() != null) {
                    moduleRequest.lessons()
                        .forEach(lessonRequest -> {
                            Lesson lesson = Lesson.builder()
                                .title(lessonRequest.title())
                                .content(lessonRequest.content())
                                .videoUrl(lessonRequest.videoUrl())
                                .module(module)
                                .build();
                            lessons.add(lesson);
                        });
                }
                module.setLessons(lessons);
                modules.add(module);
            }
        }
        course.setModules(modules);

        return courseRepository.save(course);
    }

    @Transactional(readOnly = true)
    public Course getCourseDetails(Long id) {
        Course course = courseRepository
            .findDetailedById(id)
            .orElseThrow(() -> new NotFoundException("Course %d not found".formatted(id)));
        initializeGraph(course);
        return course;
    }

    @Transactional(readOnly = true)
    public Course getCourse(Long id) {
        return courseRepository
            .findById(id)
            .orElseThrow(() -> new NotFoundException("Course %d not found".formatted(id)));
    }

    @Transactional(readOnly = true)
    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }

    private void initializeGraph(Course course) {
        if (course.getCategory() != null) {
            course.getCategory().getName();
        }
        if (course.getTeacher() != null) {
            course.getTeacher().getName();
        }
        course.getTags().size();

        course.getModules().forEach(module -> {
            if (module.getQuiz() != null) {
                module.getQuiz().getQuestions().forEach(question -> question.getOptions().size());
            }

            module.getLessons().forEach(lesson -> {
                lesson.getMaterials().size();
                lesson.getAssignments().forEach(assignment -> assignment.getSubmissions().size());
            });
        });
    }
}

