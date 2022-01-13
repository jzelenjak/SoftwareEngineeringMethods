package nl.tudelft.sem.hiring.procedure.validation;

import java.util.Set;
import lombok.AllArgsConstructor;
import nl.tudelft.sem.hiring.procedure.cache.CourseInfoResponseCache;
import nl.tudelft.sem.hiring.procedure.services.SubmissionService;
import nl.tudelft.sem.hiring.procedure.utils.GatewayConfig;
import nl.tudelft.sem.jwt.JwtUtils;

@AllArgsConstructor
public class AsyncValidatorDirector {
    private final transient JwtUtils jwtUtils;
    private final transient GatewayConfig gatewayConfig;
    private final transient SubmissionService submissionService;
    private final transient CourseInfoResponseCache courseInfoCache;

    /**
     * Validation chain that contains auth, role (lecturer and admin), responsible lecturer,
     * course exists, user exists and ta limit validators. Used in hireTa endpoint
     *
     * @param courseId the id of the course for which to perform the checks
     * @param userId the id of the user for which to perform the checks
     * @return a pre-built chain that contains all the aforementioned.
     */
    public AsyncValidator hiringChain(long courseId, long userId) {
        return AsyncValidator.Builder.newBuilder()
            .addValidators(
                new AsyncAuthValidator(jwtUtils),
                new AsyncRoleValidator(jwtUtils, Set.of(AsyncRoleValidator.Roles.LECTURER,
                    AsyncRoleValidator.Roles.ADMIN)),
                new AsyncLecturerValidator(jwtUtils, gatewayConfig, courseId),
                new AsyncCourseExistsValidator(courseInfoCache, courseId),
                new AsyncUserExistsValidator(gatewayConfig, userId),
                new AsyncTaLimitValidator(submissionService, courseInfoCache, courseId)
            ).build();
    }

    /**
     * Validation chain that contains auth, role (lecturer and admin)
     * and responsible lecturer validators.
     *
     * @param courseId the id of the course for which to perform the checks
     * @return a pre-built chain that contains all the aforementioned.
     */
    public AsyncValidator chainAdminResponsibleLecturer(long courseId) {
        return AsyncValidator.Builder.newBuilder()
            .addValidators(
                new AsyncAuthValidator(jwtUtils),
                new AsyncRoleValidator(jwtUtils, Set.of(AsyncRoleValidator.Roles.LECTURER,
                    AsyncRoleValidator.Roles.ADMIN)),
                new AsyncLecturerValidator(jwtUtils, gatewayConfig, courseId)
            ).build();
    }

    /**
     * Validation chain that contains auth and role (lecturer and admin) validators.
     *
     * @return a pre-built chain that contains all the aforementioned.
     */
    public AsyncValidator chainAdminAnyLecturer() {
        return AsyncValidator.Builder.newBuilder()
            .addValidators(
                new AsyncAuthValidator(jwtUtils),
                new AsyncRoleValidator(jwtUtils, Set.of(AsyncRoleValidator.Roles.LECTURER,
                    AsyncRoleValidator.Roles.ADMIN))
            ).build();
    }

    /**
     * Validation chain that contains auth and role validators. The roles in the role validator
     * are specified based on the presence of userId.
     *
     * @param userId a parameter that when null, the role validator will contain the student role.
     *               Otherwise, the role validator will contain the admin and lecturer roles.
     * @return a pre-built chain that contains all the aforementioned.
     */
    public AsyncValidator chainVariableValidator(Long userId) {
        AsyncValidator.Builder builder = AsyncValidator.Builder.newBuilder();
        builder.addValidator(new AsyncAuthValidator(jwtUtils));
        if (userId != null) {
            builder.addValidator(new AsyncRoleValidator(jwtUtils,
                Set.of(AsyncRoleValidator.Roles.LECTURER, AsyncRoleValidator.Roles.ADMIN)));

        } else {
            builder.addValidator(new AsyncRoleValidator(jwtUtils,
                Set.of(AsyncRoleValidator.Roles.STUDENT)));
        }
        return builder.build();
    }

    /**
     * Validation chain that contains auth, role (student), course time and course candidacy
     * validators.
     *
     * @param courseId the id of the course for which to perform the checks
     * @return a pre-built chain that contains all the aforementioned.
     */
    public AsyncValidator chainStudentCourseTimeAndMax(long courseId) {
        return AsyncValidator.Builder.newBuilder()
            .addValidators(
                new AsyncAuthValidator(jwtUtils),
                new AsyncRoleValidator(jwtUtils, Set.of(AsyncRoleValidator.Roles.STUDENT)),
                new AsyncCourseTimeValidator(courseInfoCache, courseId),
                new AsyncCourseCandidacyValidator(jwtUtils, submissionService,
                    gatewayConfig, courseId))
            .build();
    }

    /**
     * Validation chain that contains auth, role (student, admin) and course time validators.
     *
     * @param courseId the id of the course for which to perform the checks
     * @return a pre-built chain that contains all the aforementioned.
     */
    public AsyncValidator chainStudentAdminCourseTime(long courseId) {
        return AsyncValidator.Builder.newBuilder()
            .addValidators(
                new AsyncAuthValidator(jwtUtils),
                new AsyncRoleValidator(jwtUtils, Set.of(AsyncRoleValidator.Roles.STUDENT,
                    AsyncRoleValidator.Roles.ADMIN)),
                new AsyncCourseTimeValidator(courseInfoCache, courseId)
            ).build();
    }
}
