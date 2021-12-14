package nl.tudelft.sem.hiring.procedure.recommendation;

import nl.tudelft.sem.hiring.procedure.repositories.ApplicationRepository;
import java.util.List;
import java.util.stream.Collectors;

public class TimesSelectedStrategy implements RecommendationStrategy {
    private final transient ApplicationRepository repo;

    public TimesSelectedStrategy(ApplicationRepository repo) {
        this.repo = repo;
    }

    public List<Recommendation> recommend(long courseId, int number) {
        return this.repo.findTopByTimesSelected(courseId)
                .stream()
                .limit(number)
                .map(a -> new Recommendation((Long) a[0], (Double) a[1]))
                .collect(Collectors.toList());
    }
}
