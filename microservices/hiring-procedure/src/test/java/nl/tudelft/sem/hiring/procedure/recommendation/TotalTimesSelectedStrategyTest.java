package nl.tudelft.sem.hiring.procedure.recommendation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import nl.tudelft.sem.hiring.procedure.entities.Application;
import nl.tudelft.sem.hiring.procedure.repositories.ApplicationRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;


@SpringBootTest
public class TotalTimesSelectedStrategyTest {
    @Autowired
    private transient ApplicationRepository repo;

    private transient RecommendationStrategy strategy;

    @BeforeEach
    public void setup() {
        this.strategy = new TotalTimesSelectedStrategy(repo);
    }

    @Test
    void test() {
        // LocalDateTime time = LocalDateTime.now();
        //
        // Application appl1 = repo.save(new Application(42L, 69L, time));
        // Application appl2 = repo.save(new Application(42L, 66L, time));
        // Application appl3 = repo.save(new Application(42L, 68L, time));
        // Application appl4 = repo.save(new Application(42L, 61L, time));
        //
        // Application appl5 = repo.save(new Application(43L, 56L, time));
        // Application appl6 = repo.save(new Application(43L, 69L, time));
        //
        // Application appl7 = repo.save(new Application(44L, 39L, time));
        // Application appl8 = repo.save(new Application(44L, 49L, time));
        // Application appl9 = repo.save(new Application(44L, 69L, time));
        //
        // Application appl10 = repo.save(new Application(45L, 21L, time));
        // Application appl11 = repo.save(new Application(45L, 76L, time));
        // Application appl12 = repo.save(new Application(45L, 23L, time));
        // Application appl13 = repo.save(new Application(45L, 43L, time));
        //
        // Application appl14 = repo.save(new Application(46L, 69L, time));
        //
        // Assertions
        //     .assertThat(this.strategy.recommend(69L, 1))
        //     .isEqualTo(List.of(new Recommendation(42L, 3)));
        //
    }

    @Test
    void helloWorldTest() {
        System.out.println("Hello world");
    }



}
