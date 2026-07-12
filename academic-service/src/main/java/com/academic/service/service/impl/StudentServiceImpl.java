package com.academic.service.service.impl;

import com.academic.service.dto.StudentRequestDTO;
import com.academic.service.dto.StudentResponseDTO;
import com.academic.service.dto.StudentSyncMessage;
import com.academic.service.entity.Department;
import com.academic.service.entity.Student;
import com.academic.service.exception.StudentNotFoundException;
import com.academic.service.repository.DepartmentRepository;
import com.academic.service.repository.StudentRepository;
import com.academic.service.service.RollNumberService;
import com.academic.service.service.StudentService;
import com.academic.service.service.StudentSyncPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StudentServiceImpl implements StudentService {

    private final StudentRepository    studentRepository;
    private final DepartmentRepository departmentRepository;
    private final RollNumberService    rollNumberService;
    private final StudentSyncPublisher studentSyncPublisher;

    public StudentServiceImpl(StudentRepository studentRepository,
                               DepartmentRepository departmentRepository,
                               RollNumberService rollNumberService,
                               StudentSyncPublisher studentSyncPublisher) {
        this.studentRepository    = studentRepository;
        this.departmentRepository = departmentRepository;
        this.rollNumberService    = rollNumberService;
        this.studentSyncPublisher = studentSyncPublisher;
    }

    @Override
    @Transactional
    public StudentResponseDTO create(StudentRequestDTO req) {
        Department dept = departmentRepository.findById(req.getDeptId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Department not found: " + req.getDeptId()));

        String rollNo = rollNumberService.generateRollNumber(
            dept.getDeptId(), req.getAdmissionYear());

        Student student = mapFromRequest(req, new Student());
        student.setRollNo(rollNo);
        student.setDepartment(dept);

        return toDTO(studentRepository.save(student));
    }

    @Override
    public List<StudentResponseDTO> findAll() {
        return studentRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Override
    public StudentResponseDTO findByRollNo(String rollNo) {
        return studentRepository.findByRollNo(rollNo)
            .map(this::toDTO)
            .orElseThrow(() -> new StudentNotFoundException(rollNo));
    }

    @Override
    @Transactional
    public StudentResponseDTO update(String rollNo, StudentRequestDTO req) {
        Student student = studentRepository.findByRollNo(rollNo)
            .orElseThrow(() -> new StudentNotFoundException(rollNo));

        Department dept = departmentRepository.findById(req.getDeptId())
            .orElseThrow(() -> new IllegalArgumentException(
                "Department not found: " + req.getDeptId()));

        mapFromRequest(req, student);
        student.setDepartment(dept);
        // roll_no is never updated — it is permanent.

        Student saved = studentRepository.save(student);

        // Fire-and-forget: let PRMS know this student's data changed so it
        // can update its own imported copy without a manual re-import.
        studentSyncPublisher.publishUpdate(toSyncMessage(saved));

        return toDTO(saved);
    }

    @Override
    @Transactional
    public void delete(String rollNo) {
        Student student = studentRepository.findByRollNo(rollNo)
            .orElseThrow(() -> new StudentNotFoundException(rollNo));
        studentRepository.delete(student);
    }

    // ── Private helpers ───────────────────────────────────────

    /** Maps all editable request fields onto a Student entity. */
    private Student mapFromRequest(StudentRequestDTO req, Student s) {
        s.setFirstName(req.getFirstName());
        s.setMiddleName(req.getMiddleName());
        s.setLastName(req.getLastName());
        s.setGender(req.getGender());
        s.setEmail(req.getEmail());
        s.setPhone(req.getPhone());
        s.setAdmissionYear(req.getAdmissionYear());
        s.setCgpa(req.getCgpa());
        s.setActiveBacklogs(req.getActiveBacklogs() != null ? req.getActiveBacklogs() : 0);
        s.setAddress(req.getAddress());
        s.setClass10Percentage(req.getClass10Percentage());
        s.setClass12Percentage(req.getClass12Percentage());
        s.setGraduationCgpa(req.getGraduationCgpa());
        return s;
    }

    private StudentResponseDTO toDTO(Student s) {
        StudentResponseDTO dto = new StudentResponseDTO();
        dto.setStudentId(s.getStudentId());
        dto.setRollNo(s.getRollNo());
        dto.setFirstName(s.getFirstName());
        dto.setMiddleName(s.getMiddleName());
        dto.setLastName(s.getLastName());
        dto.setGender(s.getGender());
        dto.setEmail(s.getEmail());
        dto.setPhone(s.getPhone());
        dto.setAdmissionYear(s.getAdmissionYear());
        dto.setCgpa(s.getCgpa());
        dto.setActiveBacklogs(s.getActiveBacklogs());
        dto.setAddress(s.getAddress());
        dto.setClass10Percentage(s.getClass10Percentage());
        dto.setClass12Percentage(s.getClass12Percentage());
        dto.setGraduationCgpa(s.getGraduationCgpa());

        if (s.getDepartment() != null) {
            StudentResponseDTO.DepartmentInfo di = new StudentResponseDTO.DepartmentInfo();
            di.setDeptId(s.getDepartment().getDeptId());
            di.setSection(s.getDepartment().getSection());
            di.setProgram(s.getDepartment().getProgram());
            di.setBranch(s.getDepartment().getBranch());
            di.setDeptName(s.getDepartment().getDeptName());
            dto.setDepartment(di);
        }
        return dto;
    }

    private StudentSyncMessage toSyncMessage(Student s) {
        StudentSyncMessage msg = new StudentSyncMessage();
        msg.setRollNo(s.getRollNo());
        msg.setFirstName(s.getFirstName());
        msg.setMiddleName(s.getMiddleName());
        msg.setLastName(s.getLastName());
        msg.setEmail(s.getEmail());
        msg.setPhone(s.getPhone());
        msg.setDeptId(s.getDepartment() != null ? s.getDepartment().getDeptId() : null);
        msg.setAdmissionYear(s.getAdmissionYear());
        msg.setCgpa(s.getCgpa());
        msg.setActiveBacklogs(s.getActiveBacklogs());
        return msg;
    }
}
