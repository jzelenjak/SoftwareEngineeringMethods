package nl.tudelft.sem.courses.controllers;


import nl.tudelft.sem.courses.respositories.CourseRepository;
import nl.tudelft.sem.courses.respositories.GradeRepository;
import nl.tudelft.sem.courses.respositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public @ResponseBody String getHelloWorld(){
        return "Hello world";
    }


}
