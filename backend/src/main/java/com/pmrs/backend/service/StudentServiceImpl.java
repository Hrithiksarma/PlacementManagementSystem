package com.pmrs.backend.service;

import com.pmrs.backend.dto.AcademicStudentDTO;
import com.pmrs.backend.dto.BackfillResultDTO;
import com.pmrs.backend.entity.Role;
import com.pmrs.backend.entity.Student;
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

    private final StudentRepository    studentRepository;
    private final DepartmentRepository departmentRepository;
    private final RollNumberService    rollNumberService;
    private final AcademicErpClient    academicErpClient;
    private final UserRepository       userRepository;
    private final PasswordEncoder      passwordEncoder;

    public StudentServiceImpl(StudentRepository    studentRepository,
                               DepartmentRepository departmentRepository,
                               RollNumberService    rollNumberService,
                               AcademicErpClient    academicErpClient,
                               UserRepository       userRepository,
                               PasswordEncoder      passwordEncoder) {
        this.studentRepository    = studentRepository;
        this.departmentRepository = departmentRepository;
        this.rollNumberService    = rollNumberService;
        this.academicErpClient    = academicErpClient;
        this.userRepository       = userRepository;
        this.passwordEncoder      = passwordEncoder;
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

    @Override
    @Transactional
    public Student importStudent(String rollNo) {
        // Reject if already imported
        if (studentRepository.existsByRollNo(rollNo)) {
            throw new IllegalArgumentException(
                "Student with roll number " + rollNo + " is already in PRMS.");
        }

        // Fetch from Academic ERP
        AcademicStudentDTO academic = academicErpClient.fetchByRollNo(rollNo);

        // Map to PRMS Student entity
        Student student = new Student();
        student.setRollNo(academic.getRollNo());
        student.setName(academic.buildFullName());
        student.setEmail(academic.getEmail());
        student.setPhone(academic.getPhone());
        student.setBatchYear(academic.getAdmissionYear());
        student.setCgpa(academic.getCgpa());
        student.setActiveBacklogs(academic.getActiveBacklogs());
        student.setPlacementTier("Unplaced");    // always starts Unplaced

        if (academic.getDepartment() != null) {
            departmentRepository.findById(academic.getDepartment().getDeptId())
                .ifPresent(student::setDepartment);
        }

        Student saved = studentRepository.save(student);

        createStudentAccountIfMissing(saved);

        return saved;
    }

    /**
     * Creates a login for a student that doesn't have one yet. Username and the
     * temporary password are both the roll number — it's unique, stable, and
     * already known to the student. mustChangePassword forces them to set a
     * real password on first login (enforced both client-side and server-side).
     */
    private void createStudentAccountIfMissing(Student student) {
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