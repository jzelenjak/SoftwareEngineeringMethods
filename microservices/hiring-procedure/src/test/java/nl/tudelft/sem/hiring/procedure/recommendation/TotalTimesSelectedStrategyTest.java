package nl.tudelft.sem.hiring.procedure.recommendation;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Scanner;
import nl.tudelft.sem.hiring.procedure.entities.Submission;
import nl.tudelft.sem.hiring.procedure.entities.SubmissionStatus;
import nl.tudelft.sem.hiring.procedure.recommendation.entities.Recommendation;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.Recommender;
import nl.tudelft.sem.hiring.procedure.recommendation.strategies.TotalTimesSelectedStrategy;
import nl.tudelft.sem.hiring.procedure.repositories.SubmissionRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.web.server.ResponseStatusException;


@SpringBootTest
public class TotalTimesSelectedStrategyTest {
    @Autowired
    private transient SubmissionRepository repo;

    private transient Recommender strategy;

    private final transient LocalDateTime time = LocalDateTime.now();

    private static final transient String ONE_LF = "1\n";
    private static final transient String TWO_LF = "2\n";
    private static final transient String THREE_LF = "3\n";

    @BeforeEach
    public void setup() {
        this.strategy = new TotalTimesSelectedStrategy(repo, null, "placeholder");

        // Clear the database
        repo.deleteAll();
    }

    /**
     * A helper method to add applications to the repository.
     *
     * @param input the input string in the following format:
     *              N            (total number of users)
     *              n            (number of applications for the following user)
     *              userId courseId1 STATUS1 userId courseId2 STATUS2 ...
     *              ...
     *
     *              I needed to duplicate userId for each application, because PMD
     *              was complaining about DU anomalies since I was accessing the userId
     *              variable outside the for-loop.
     */
    private void addApplications(String input) {
        Scanner sc = new Scanner(input);
        try (sc) {
            int numberOfApplications = sc.nextInt();
            sc.nextLine();
            for (int i = 0; i < numberOfApplications; ++i) {
                int applications = sc.nextInt();
                sc.nextLine();
                for (int j = 0; j < applications; ++j) {
                    Submission appl = new Submission(sc.nextLong(), sc.nextLong(), time);
                    appl.setStatus(SubmissionStatus.valueOf(sc.next()));
                    this.repo.save(appl);
                }
                sc.nextLine();
            }
        }
    }


    /**
     * Tests without filtering (focus on the logic).
     */

    @Test
    public void testRecommendUserWhoHasNotAppliedMustBeIgnored() {
        // Number of applicants
        String input = TWO_LF
            // Applicant No43; selected in the past: 1 time
            + "2\n43 56 ACCEPTED 43 69 IN_PROGRESS\n"
            // Not an Applicant No45; selected in the past: 4 times
            + "4\n45 21 ACCEPTED 45 76 ACCEPTED 45 23 ACCEPTED 45 43 ACCEPTED\n";
        addApplications(input);

        Assertions
            .assertThat(this.strategy.recommend(69L, 2, 0.0).block())
            .isEqualTo(List.of(new Recommendation(43L, 1)));
    }

    @Test
    public void testRecommendUserWithZeroTimesSelectedMustNotBeRecommended() {
        // Number of applicants
        String input = ONE_LF
            // Applicant No422; selected in the past: 0 times
            + "1\n422 33 IN_PROGRESS\n";
        addApplications(input);

        Assertions
            .assertThatThrownBy(() -> this.strategy.recommend(33L, 1, 0.0).block())
            .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    public void testRecommendRejectedApplicationMustBeIgnored() {
        // Number of applicants
        String input = ONE_LF
            // Applicant No777; selected in the past: 1 time
            + "4\n777 54 IN_PROGRESS 777 64 ACCEPTED 777 62 REJECTED 777 61 REJECTED\n";
        addApplications(input);

        Assertions
            .assertThat(this.strategy.recommend(54L, 4, 0.0).block())
            .isEqualTo(List.of(new Recommendation(777L, 1)));
    }

    @Test
    public void testRecommendWithdrawnApplicationMustBeIgnored() {
        // Number of applicants
        String input = ONE_LF
            // Applicant No777; selected in the past: 2 times
            + "4\n777 22 IN_PROGRESS 777 64 ACCEPTED 777 62 WITHDRAWN 777 61 ACCEPTED\n";
        addApplications(input);

        Assertions
            .assertThat(this.strategy.recommend(22L, 3, 0.0).block())
            .isEqualTo(List.of(new Recommendation(777L, 2)));
    }

    @Test
    public void testRecommendApplicantsForOtherCoursesMustBeIgnored() {
        // Number of applicants
        String input = TWO_LF
            // Applicant No27; selected in the past: 1 time
            + "2\n27 90 IN_PROGRESS 27 64 ACCEPTED\n"
            // Not an Applicant No11; selected in the past: 3 times
            + "4\n11 24 IN_PROGRESS 11 84 ACCEPTED 11 92 ACCEPTED 11 68 ACCEPTED\n";
        addApplications(input);

        Assertions
            .assertThat(this.strategy.recommend(90L, 5, 0.0).block())
            .isEqualTo(List.of(new Recommendation(27L, 1)));
    }

    @Test
    public void testRecommendCanOnlySelectLimitedNumber() {
        // Number of applicants
        String input = THREE_LF
            // Applicant No33; selected in the past: 1 time
            + "2\n33 555 IN_PROGRESS 33 663 ACCEPTED\n"
            // Applicant No89; selected in the past: 2 times
            + "3\n89 555 IN_PROGRESS 89 654 ACCEPTED 89 977 ACCEPTED\n"
            // Applicant No111; selected in the past: 2 times
            + "4\n111 555 IN_PROGRESS 111 656 ACCEPTED 111 923 REJECTED 111 121 ACCEPTED\n";
        addApplications(input);

        Assertions
            .assertThat(this.strategy.recommend(555L, 1, 0.0).block())
            .containsAnyOf(new Recommendation(111, 2),
                new Recommendation(89, 2))
            .hasSize(1);
    }

    @Test
    public void testRecommendOneTestToRuleThemAll() {
        // Number of applicants
        String input = "5\n"
            // Applicant No42; selected in the past: 3 times
            + "4\n42 69 IN_PROGRESS 42 66 ACCEPTED 42 68 ACCEPTED 42 61 ACCEPTED\n"
            // Applicant No43; selected in the past: 1 time
            + "3\n43 56 ACCEPTED 43 69 IN_PROGRESS 43 666 REJECTED\n"
            // Applicant No44; selected in the past 2 times
            + "4\n44 39 ACCEPTED 44 49 ACCEPTED 44 69 IN_PROGRESS 44 62 WITHDRAWN\n"
            // Not an Applicant No45; selected in the past: 4 times
            + "4\n45 21 ACCEPTED 45 76 ACCEPTED 45 23 ACCEPTED 45 43 ACCEPTED\n"
            // Applicant No46; selected in the past: 0 times
            + "1\n46 69 IN_PROGRESS\n";
        addApplications(input);

        Assertions
            .assertThat(this.strategy.recommend(69L, 10, 0.0).block())
            .isEqualTo(List.of(new Recommendation(42L, 3),
                new Recommendation(44L, 2),
                new Recommendation(43L, 1)));
    }

    @Test
    void testRecommendNoRecommendationsFound() {
        Assertions
            .assertThatThrownBy(() -> this.strategy.recommend(69L, 10, 0.0).block())
            .isInstanceOf(ResponseStatusException.class);
    }

    /**
     * Tests with filtering (focus filtering). Use boundary testing.
     */

    @Test
    public void testRecommendMetricOnPoint() {
        // Number of applicants
        String input = ONE_LF
            // Applicant No333; selected in the past: 2 times
            + "4\n333 22 IN_PROGRESS 333 64 ACCEPTED 333 62 WITHDRAWN 333 61 ACCEPTED\n";
        addApplications(input);

        Assertions
            .assertThat(this.strategy.recommend(22L, 3, 2.0).block())
            .isEqualTo(List.of(new Recommendation(333L, 2)));
    }

    @Test
    public void testRecommendMetricOffPoint() {
        // Number of applicants
        String input = ONE_LF
            // Applicant No444; selected in the past: 2 times
            + "4\n444 22 IN_PROGRESS 444 64 ACCEPTED 444 62 WITHDRAWN 444 61 ACCEPTED\n";
        addApplications(input);

        Assertions
            .assertThatThrownBy(() -> this.strategy.recommend(22L, 3, 3.0).block())
            .isInstanceOf(ResponseStatusException.class);
    }

    @Test
    public void testRecommendMetricInPoint() {
        // Number of applicants
        String input = ONE_LF
            // Applicant No666; selected in the past: 2 times
            + "4\n666 22 IN_PROGRESS 666 64 ACCEPTED 666 62 WITHDRAWN 666 61 ACCEPTED\n";
        addApplications(input);

        Assertions
            .assertThat(this.strategy.recommend(22L, 3, 1.0).block())
            .isEqualTo(List.of(new Recommendation(666L, 2)));
    }

    @Test
    public void testRecommendMetricOutPoint() {
        // Number of applicants
        String input = ONE_LF
            // Applicant No222; selected in the past: 3 times
            + "4\n222 22 IN_PROGRESS 222 64 ACCEPTED 222 62 ACCEPTED 222 61 ACCEPTED\n";
        addApplications(input);

        Assertions
            .assertThatThrownBy(() -> this.strategy.recommend(22L, 3, 5.0).block())
            .isInstanceOf(ResponseStatusException.class);
    }
}
