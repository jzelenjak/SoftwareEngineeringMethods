package nl.tudelft.sem.hiring.procedure.recommendation;

import java.time.LocalDateTime;
import java.util.Optional;
import nl.tudelft.sem.hiring.procedure.entities.Application;
import nl.tudelft.sem.hiring.procedure.repositories.ApplicationRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class TimesSelectedStrategyTest {
    @Autowired
    private transient ApplicationRepository repo;

    private transient RecommendationStrategy strategy;

    @BeforeEach
    public void setup() {
        this.strategy = new TimesSelectedStrategy(repo);
    }

    @Test
    void test() {
        LocalDateTime time = LocalDateTime.now();

        Application appl = repo.save(new Application(1L, 2L, time));

        Optional<Application> optApplication = repo.findByUserIdAndCourseId(1L, 2L);
        Assertions
                .assertThat(optApplication)
                .isPresent();
        repo.delete(appl);
    }

    @Test
    void helloWorldTest() {
        System.out.println("Hello world");
    }



}
