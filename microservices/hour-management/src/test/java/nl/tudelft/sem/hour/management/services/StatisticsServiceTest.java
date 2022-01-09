package nl.tudelft.sem.hour.management.services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.withPrecision;

import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import nl.tudelft.sem.hour.management.dto.AggregationStatistics;
import nl.tudelft.sem.hour.management.dto.HourDeclarationRequest;
import nl.tudelft.sem.hour.management.entities.HourDeclaration;
import nl.tudelft.sem.hour.management.repositories.HourDeclarationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
public class StatisticsServiceTest {
    @Autowired
    private transient StatisticsService statisticsService;

    @Autowired
    private transient HourDeclarationRepository hourDeclarationRepository;

    private final transient ZonedDateTime testDate = ZonedDateTime.now();

    private final transient HourDeclarationRequest hourDeclarationRequestOne =
            new HourDeclarationRequest(1234, 5678, 12, "nl");
    private final transient HourDeclarationRequest hourDeclarationRequestTwo =
            new HourDeclarationRequest(1234, 5678, 29, "de");
    private final transient HourDeclarationRequest hourDeclarationRequestThree =
            new HourDeclarationRequest(1234, 5678, 42, "tr");
    private final transient HourDeclarationRequest hourDeclarationRequestFour =
            new HourDeclarationRequest(1234, 5678, 1337.5, "gb");
    private final transient HourDeclarationRequest hourDeclarationRequestInvalidate =
            new HourDeclarationRequest(1234, 5678, -9999, "us");
    private final transient HourDeclarationRequest hourDeclarationRequestZero =
            new HourDeclarationRequest(1234, 5678, 0, "it");

    private final transient HourDeclaration hourDeclarationOne = new HourDeclaration(1,
            hourDeclarationRequestOne, false, testDate);
    private final transient HourDeclaration hourDeclarationTwo = new HourDeclaration(2,
            hourDeclarationRequestTwo, false, testDate);
    private final transient HourDeclaration hourDeclarationThree = new HourDeclaration(3,
            hourDeclarationRequestThree, false, testDate);
    private final transient HourDeclaration hourDeclarationFour = new HourDeclaration(4,
            hourDeclarationRequestFour, false, testDate);
    private final transient HourDeclaration hourDeclarationInvalidate = new HourDeclaration(5,
            hourDeclarationRequestInvalidate, false, testDate);
    private final transient HourDeclaration hourDeclarationZero = new HourDeclaration(6,
            hourDeclarationRequestZero, false, testDate);

    @BeforeEach
    void tearDown() {
        hourDeclarationRepository.deleteAll();
    }

    @Test
    void testCalculateMean() {
        List<HourDeclaration> hourDeclarationList = List.of(hourDeclarationOne,
                hourDeclarationTwo, hourDeclarationThree);

        assertThat(statisticsService.calculateMean(hourDeclarationList))
                .isEqualTo(27.6666666667, withPrecision(0.00001));
    }

    @Test
    void testCalculateMeanSkew() {
        List<HourDeclaration> hourDeclarationList = List.of(hourDeclarationOne,
                hourDeclarationTwo, hourDeclarationThree, hourDeclarationFour);

        assertThat(statisticsService.calculateMean(hourDeclarationList))
                .isEqualTo(355.125, withPrecision(0.00001));
    }

    @Test
    void testCalculateMeanInvalid() {
        List<HourDeclaration> hourDeclarationList = List.of(hourDeclarationOne,
                hourDeclarationTwo, hourDeclarationThree,
                hourDeclarationFour, hourDeclarationInvalidate);

        assertThat(statisticsService.calculateMean(hourDeclarationList)).isEqualTo(-1);
    }

    @Test
    void testCalculateMeanEmpty() {
        List<HourDeclaration> hourDeclarationList = List.of();

        assertThat(statisticsService.calculateMean(hourDeclarationList)).isEqualTo(-1);
    }

    @Test
    void testCalculateMeanZero() {
        List<HourDeclaration> hourDeclarationList = List.of(hourDeclarationZero);

        assertThat(statisticsService.calculateMean(hourDeclarationList)).isEqualTo(0);
    }

    @Test
    void testCalculateMedianOdd() {
        // Ensure sorted
        List<HourDeclaration> hourDeclarationList = List.of(hourDeclarationOne,
                hourDeclarationTwo, hourDeclarationThree);

        assertThat(statisticsService.calculateMedian(hourDeclarationList))
                .isEqualTo(29, withPrecision(0.00001));
    }

    @Test
    void testCalculateMedianEven() {
        // Ensure sorted
        List<HourDeclaration> hourDeclarationList = List.of(hourDeclarationOne,
                hourDeclarationTwo, hourDeclarationThree, hourDeclarationFour);

        assertThat(statisticsService.calculateMedian(hourDeclarationList))
                .isEqualTo(35.5, withPrecision(0.00001));
    }

    @Test
    void testCalculateMedianInvalidate() {
        List<HourDeclaration> hourDeclarationList = List.of();

        assertThat(statisticsService.calculateMedian(hourDeclarationList)).isEqualTo(-1);
    }

    @Test
    void testCalculateStandardDeviationValid() {
        List<HourDeclaration> hourDeclarationList = List.of(hourDeclarationOne,
                hourDeclarationTwo, hourDeclarationThree);

        double mean = statisticsService.calculateMean(hourDeclarationList);

        assertThat(statisticsService.calculateStandardDeviation(mean,
                hourDeclarationList)).isEqualTo(12.283683848459, withPrecision(0.00001));
    }

    @Test
    void testCalculateStandardDeviationSkew() {
        List<HourDeclaration> hourDeclarationList = List.of(hourDeclarationOne,
                hourDeclarationTwo, hourDeclarationThree, hourDeclarationFour);

        double mean = statisticsService.calculateMean(hourDeclarationList);

        assertThat(statisticsService.calculateStandardDeviation(mean,
                hourDeclarationList)).isEqualTo(567.27422546331, withPrecision(0.00001));
    }

    @Test
    void testCalculateStandardDeviationInvalid() {
        List<HourDeclaration> hourDeclarationList = List.of();

        assertThat(statisticsService.calculateStandardDeviation(0,
                hourDeclarationList)).isEqualTo(-1);
    }


    @Test
    void testCalculateAggregationStatisticsEven() {
        hourDeclarationRepository.save(hourDeclarationOne);
        hourDeclarationRepository.save(hourDeclarationTwo);
        hourDeclarationRepository.save(hourDeclarationThree);
        hourDeclarationRepository.save(hourDeclarationFour);

        List<HourDeclaration> hourDeclarationList = hourDeclarationRepository
                .findAll();

        hourDeclarationList.sort(Comparator.comparingDouble(HourDeclaration::getDeclaredHours));

        double mean = statisticsService.calculateMean(hourDeclarationList);

        AggregationStatistics expected
                = new AggregationStatistics(mean,
                statisticsService.calculateMedian(hourDeclarationList),
                statisticsService.calculateStandardDeviation(mean, hourDeclarationList));

        Optional<AggregationStatistics> actual = statisticsService
                .calculateAggregationStatistics(Set.of(1234L), Set.of(5678L));

        assertThat(actual).isNotEmpty();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    void testCalculateAggregationStatisticsOdd() {
        hourDeclarationRepository.save(hourDeclarationOne);
        hourDeclarationRepository.save(hourDeclarationTwo);
        hourDeclarationRepository.save(hourDeclarationThree);

        List<HourDeclaration> hourDeclarationList = hourDeclarationRepository.findAll();

        hourDeclarationList.sort(Comparator.comparingDouble(HourDeclaration::getDeclaredHours));

        double mean = statisticsService.calculateMean(hourDeclarationList);

        AggregationStatistics expected
                = new AggregationStatistics(mean,
                statisticsService.calculateMedian(hourDeclarationList),
                statisticsService.calculateStandardDeviation(mean, hourDeclarationList));

        Optional<AggregationStatistics> actual = statisticsService
                .calculateAggregationStatistics(Set.of(1234L), Set.of(5678L));

        assertThat(actual).isNotEmpty();
        assertThat(actual.get()).isEqualTo(expected);
    }

    @Test
    void testCalculateAggregationStatisticsNegativeMean() {
        hourDeclarationRepository.save(hourDeclarationOne);
        hourDeclarationRepository.save(hourDeclarationTwo);
        hourDeclarationRepository.save(hourDeclarationThree);
        hourDeclarationRepository.save(hourDeclarationInvalidate);

        Optional<AggregationStatistics> actual = statisticsService
                .calculateAggregationStatistics(Set.of(1234L), Set.of(5678L));

        assertThat(actual).isEmpty();
    }

    @Test
    void testCalculateAggregationStatisticsDatabaseEmpty() {
        Optional<AggregationStatistics> actual = statisticsService
                .calculateAggregationStatistics(Set.of(1234L), Set.of(5678L));

        assertThat(actual).isEmpty();
    }

    @Test
    void testCalculateAggregationStatisticsQueryEmpty() {
        hourDeclarationRepository.save(hourDeclarationOne);
        hourDeclarationRepository.save(hourDeclarationTwo);
        hourDeclarationRepository.save(hourDeclarationThree);

        Optional<AggregationStatistics> actual = statisticsService
                .calculateAggregationStatistics(Set.of(12345L), Set.of(5678L));

        assertThat(actual).isEmpty();
    }
}
