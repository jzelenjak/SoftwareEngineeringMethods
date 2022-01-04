package nl.tudelft.sem.hour.management.validation;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import reactor.core.publisher.Mono;

public class AsyncValidatorBuilderTest {

    public static class TestValidator implements AsyncValidator {

        // Contains the next validator in the chain
        public transient AsyncValidator next;

        @Override
        public void setNext(AsyncValidator next) {
            this.next = next;
        }

        @Override
        public Mono<Boolean> validate(HttpHeaders headers, String body) {
            if (next == null) {
                return Mono.just(true);
            }
            return next.validate(headers, body);
        }
    }

    @Test
    public void testEmptyChain() {
        assertNull(AsyncValidator.Builder.newBuilder().build());
    }

    @Test
    public void testAddValidatorNull() {
        AsyncValidator chain = AsyncValidator.Builder.newBuilder()
                .addValidator(null)
                .build();

        assertNull(chain);
    }

    @Test
    public void testAddValidatorSingle() {
        TestValidator testValidator = new TestValidator();
        AsyncValidator chain = AsyncValidator.Builder.newBuilder()
                .addValidator(testValidator)
                .build();

        assertNotNull(chain);
        assertSame(testValidator, chain);
        assertNull(testValidator.next);
    }

    @Test
    public void testAddValidatorMultiple() {
        final int validators = 10;
        var testValidators = Stream.generate(TestValidator::new)
                .limit(validators)
                .toArray(TestValidator[]::new);

        AsyncValidator chain = AsyncValidator.Builder.newBuilder()
                .addValidator(testValidators[0])
                .addValidator(testValidators[1])
                .addValidator(testValidators[2])
                .addValidator(testValidators[3])
                .addValidator(testValidators[4])
                .addValidator(testValidators[5])
                .addValidator(testValidators[6])
                .addValidator(testValidators[7])
                .addValidator(testValidators[8])
                .addValidator(testValidators[9])
                .build();

        assertNotNull(chain);
        for (int i = 1; i < validators; i++) {
            assertSame(testValidators[i - 1].next, testValidators[i]);
            assertNotSame(testValidators[i - 1], testValidators[i]);
        }
    }

    @Test
    public void testAddValidatorsNull() {
        AsyncValidator chain = AsyncValidator.Builder.newBuilder()
                .addValidators(new TestValidator[0])
                .build();

        assertNull(chain);
    }

    @Test
    public void testAddValidatorsSingle() {
        TestValidator testValidator = new TestValidator();
        AsyncValidator chain = AsyncValidator.Builder.newBuilder()
                .addValidators(testValidator)
                .build();

        assertNotNull(chain);
        assertSame(testValidator, chain);
        assertNull(testValidator.next);
    }

    @Test
    public void testAddValidatorsMultiple() {
        final int validators = 10;
        var testValidators = Stream.generate(TestValidator::new)
                .limit(validators)
                .toArray(TestValidator[]::new);

        AsyncValidator chain = AsyncValidator.Builder.newBuilder()
                .addValidators(testValidators)
                .build();

        assertNotNull(chain);
        for (int i = 1; i < validators; i++) {
            assertSame(testValidators[i - 1].next, testValidators[i]);
            assertNotSame(testValidators[i - 1], testValidators[i]);
        }
    }

}
