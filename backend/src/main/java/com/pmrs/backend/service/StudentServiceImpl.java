package com.pmrs.backend.service;

import com.pmrs.backend.dto.BackfillResultDTO;
import com.pmrs.backend.entity.Department;
import com.pmrs.backend.entity.Role;
import com.pmrs.backend.entity.Student;
import com.pmrs.backend.entity.StudentFormSubmission;
import com.pmrs.backend.entity.User;
import com.pmrs.backend.exception.ResourceNotFoundException;
import com.pmrs.backend.repository.DepartmentRepository;
import com.pmrs.backend.repository.StudentRepository;
import com.pmrs.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    // Institute roll numbers are exactly 7 digits (e.g. 2521512) — no letters.
    private static final java.util.regex.Pattern ROLL_NO_PATTERN =
            java.util.regex.Pattern.compile("^[0-9]{7}$");

    private final StudentRepository    studentRepository;
    private final DepartmentRepository departmentRepository;
    private final RollNumberService    rollNumberService;
    private final UserRepository       userRepository;
    private final PasswordEncoder      passwordEncoder;
    private final WelcomeEmailService  welcomeEmailService;

    public StudentServiceImpl(StudentRepository    studentRepository,
                               DepartmentRepository departmentRepository,
                               RollNumberService    rollNumberService,
                               UserRepository       userRepository,
                               PasswordEncoder      passwordEncoder,
                               WelcomeEmailService  welcomeEmailService) {
        this.studentRepository    = studentRepository;
        this.departmentRepository = departmentRepository;
        this.rollNumberService    = rollNumberService;
        this.userRepository       = userRepository;
        this.passwordEncoder      = passwordEncoder;
        this.welcomeEmailService  = welcomeEmailService;
    }

    @Override
    public List<Student> getAllStudents() {
        return studentRepository.findAll();
    }

    @Override
    public Student getStudentById(Integer id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
    }

    /**
     * Persists a new student after assigning a roll number.
     *
     * <p>{@code @Transactional} here is critical: it opens a single transaction
     * that covers both the counter increment (inside RollNumberService) and the
     * student INSERT.  The two operations commit together.  If the student save
     * fails for any reason, the counter rolls back — the serial is not wasted.
     *
     * <p>StudentService never knows how the roll number is produced.  It simply
     * asks RollNumberService for one and sets it on the entity before saving.
     */
    @Override
    @Transactional
    public Student saveStudent(Student student) {
        String rollNo = rollNumberService.generateRollNumber(
            student.getDepartment().getDeptId(),
            student.getBatchYear()
        );
        student.setRollNo(rollNo);
        return studentRepository.save(student);
    }

    /**
     * Updates an existing student's mutable fields.
     *
     * <p>Roll number is intentionally excluded — it is permanent once assigned
     * and must never change through an update operation.
     */
    @Override
    public Student updateStudent(Integer id, Student updated) {
        Student existing = studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        existing.setName(updated.getName());
        existing.setDepartment(updated.getDepartment());
        existing.setBatchYear(updated.getBatchYear());
        existing.setCgpa(updated.getCgpa());
        existing.setEmail(updated.getEmail());
        existing.setActiveBacklogs(updated.getActiveBacklogs());
        existing.setPhone(updated.getPhone());
        existing.setPlacementTier(updated.getPlacementTier());
        // roll_no is deliberately absent — roll numbers are permanent.
        return studentRepository.save(existing);
    }

    @Override
    public void deleteStudent(Integer id) {
        studentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Student not found with id: " + id));
        studentRepository.deleteById(id);
    }

    @Override
    public List<Student> getStudentsByCgpa(Double cgpa) {
        return studentRepository.findByCgpaGreaterThanEqual(cgpa);
    }

    @Override
    public List<Student> getFilteredStudents(String branch, String program, Integer batchYear) {
        return studentRepository.findByDepartment_BranchAndDepartment_ProgramAndBatchYear(
                branch, program, batchYear
        );
    }

    /**
     * Promotes a staged Google-Form student submission into a real Student.
     * Validation failures throw {@link IllegalArgumentException} with a clear
     * message so the bulk-import caller can record it as this row's failure
     * reason without aborting the whole batch.
     */
    @Override
    @Transactional
    public Student importFromSubmission(StudentFormSubmission submission) {
        String name    = trimToNull(submission.getFullName());
        String email   = trimToNull(submission.getEmail());
        String phone   = trimToNull(submission.getPhone());
        String program = trimToNull(submission.getProgram());
        String branch  = trimToNull(submission.getBranch());
        String rollNo  = submission.getRollNo() == null ? "" : submission.getRollNo().trim();

        if (name == null)    throw new IllegalArgumentException("Full name is missing.");
        if (email == null)   throw new IllegalArgumentException("Email is missing.");
        if (phone == null)   throw new IllegalArgumentException("Phone is missing.");
        if (program == null) throw new IllegalArgumentException("Program is missing.");
        if (branch == null)  throw new IllegalArgumentException("Branch/department is missing.");
        if (submission.getBatchYear() == null)
            throw new IllegalArgumentException("Batch year is missing or not a number.");

        // Roll number now comes straight from the student's form answer.
        if (rollNo.isEmpty()) {
            throw new IllegalArgumentException(
                "No roll number was submitted — ask the student to resubmit the form.");
        }
        if (!ROLL_NO_PATTERN.matcher(rollNo).matches()) {
            throw new IllegalArgumentException(
                "Roll number '" + rollNo + "' must be exactly 7 digits (no letters) — check the form response for typos.");
        }
        if (studentRepository.existsByRollNo(rollNo)) {
            throw new IllegalArgumentException(
                "Roll number " + rollNo + " is already in PRMS — this student may be a duplicate submission.");
        }

        if (studentRepository.existsByEmail(email)) {
            throw new IllegalArgumentException(
                "A student with email " + email + " already exists in PRMS.");
        }

        // A branch+program spans multiple sections; assign the first section
        // (an officer can move the student to another section afterward).
        List<Department> matches =
                departmentRepository.findByBranchAndProgramOrderByDeptIdAsc(branch, program);
        if (matches.isEmpty()) {
            throw new IllegalArgumentException(
                    "No department matches branch '" + branch + "' and program '" + program + "'.");
        }
        Department department = matches.get(0);

        Student student = new Student();
        student.setName(name);
        student.setEmail(email);
        student.setPhone(phone);
        student.setDepartment(department);
        student.setBatchYear(submission.getBatchYear());
        student.setCgpa(submission.getCgpa());
        student.setActiveBacklogs(
                submission.getActiveBacklogs() != null ? submission.getActiveBacklogs() : 0);
        student.setResumeUrl(submission.getResumeUrl());
        student.setPhotoUrl(submission.getPhotoUrl());
        student.setGradeSheetUrl(submission.getGradeSheetUrl());
        student.setPlacementTier("Unplaced");   // always starts Unplaced
        student.setRollNo(rollNo);              // exactly as the student typed it

        Student saved = studentRepository.save(student);
        createStudentAccountIfMissing(saved);
        return saved;
    }

    private static String trimToNull(String value) {
        if (value == null) return null;
        String t = value.trim();
        return t.isEmpty() ? null : t;
    }

    /**
     * Creates a login for a student that doesn't have one yet. Username and the
     * temporary password are both the roll number — it's unique, stable, and
     * already known to the student. mustChangePassword forces them to set a
     * real password on first login (enforced both client-side and server-side).
     *
     * <p>Package-private so the Google-Form student-import flow
     * ({@code StudentFormServiceImpl}) can reuse the exact same account-creation
     * logic after it saves a form-imported student.
     */
    void createStudentAccountIfMissing(Student student) {
        if (userRepository.existsByStudentId(student.getStudentId())) {
            return;
        }
        String username = student.getRollNo();
        if (userRepository.existsByUsername(username)) {
            return;
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(username));
        user.setEmail(student.getEmail());
        user.setRole(Role.STUDENT);
        user.setStudentId(student.getStudentId());
        user.setMustChangePassword(true);
        userRepository.save(user);

        // First (and only) time an account is created for this student — welcome them.
        welcomeEmailService.sendWelcomeEmail(
                student.getEmail(), student.getName(), student.getRollNo());
    }

    @Override
    @Transactional
    public BackfillResultDTO backfillStudentAccounts() {
        int created = 0;
        int skipped = 0;
        List<String> createdRollNumbers = new java.util.ArrayList<>();

        for (Student student : studentRepository.findAll()) {
            if (userRepository.existsByStudentId(student.getStudentId())) {
                skipped++;
                continue;
            }
            createStudentAccountIfMissing(student);
            createdRollNumbers.add(student.getRollNo());
            created++;
        }

        return new BackfillResultDTO(created, skipped, createdRollNumbers);
    }
}