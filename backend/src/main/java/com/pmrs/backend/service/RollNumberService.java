package com.pmrs.backend.service;

/**
 * Contract for assigning roll numbers to students.
 *
 * <h2>Why an interface?</h2>
 * Roll numbers are academic identifiers.  The PRMS generates them today because
 * no dedicated Academic Service exists, but that responsibility could move
 * elsewhere in the future.  This interface is the boundary between the two
 * concerns.  {@code StudentService} depends only on this contract — it never
 * knows whether the roll number came from a local counter table, an HTTP call
 * to an Academic Service, or any other source.
 *
 * <h2>Future migration path</h2>
 * <ol>
 *   <li>Create {@code AcademicServiceRollNumberService implements RollNumberService}.</li>
 *   <li>Annotate it {@code @Primary}.</li>
 *   <li>Remove {@code @Primary} from {@link LocalRollNumberService}.</li>
 *   <li>{@code StudentService} requires <b>zero changes</b>.</li>
 * </ol>
 */
public interface RollNumberService {

    /**
     * Returns a unique, collision-safe roll number for the given student placement.
     *
     * <p>The roll number follows the format {@code [YY][DDD][NN]}:
     * <ul>
     *   <li>{@code YY}  — last two digits of {@code batchYear} (e.g. {@code 25})</li>
     *   <li>{@code DDD} — three-digit department code encoding program, branch, section</li>
     *   <li>{@code NN}  — two-digit serial within this department and batch year (01–99)</li>
     * </ul>
     * Total length is always 7 characters.  Example: {@code "2511104"}.
     *
     * <p>Implementations must guarantee uniqueness even under concurrent calls
     * for the same {@code deptId} and {@code batchYear}.
     *
     * <p>This method participates in the caller's transaction by default
     * ({@code Propagation.REQUIRED}).  Callers should open a transaction before
     * invoking so that the serial advance and the student INSERT commit atomically.
     *
     * @param deptId    the student's department ID ({@code Departments.dept_id})
     * @param batchYear the student's batch year (e.g. 2025)
     * @return a 7-character numeric roll number string
     * @throws IllegalArgumentException if {@code deptId} has no registered code mapping
     */
    String generateRollNumber(Integer deptId, Integer batchYear);
}