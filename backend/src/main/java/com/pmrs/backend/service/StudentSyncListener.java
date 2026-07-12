package com.pmrs.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pmrs.backend.dto.StudentSyncMessage;
import com.pmrs.backend.entity.Student;
import com.pmrs.backend.repository.DepartmentRepository;
import com.pmrs.backend.repository.StudentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;

/**
 * Subscribes to Academic ERP's "student-updates" Redis channel. When a
 * student is edited there, this applies the same change to PRMS's own
 * copy of the student — but only if that student was already imported.
 * Students never imported into PRMS are silently ignored.
 */
@Component
public class StudentSyncListener implements MessageListener {

    private static final Logger log = LoggerFactory.getLogger(StudentSyncListener.class);

    private final StudentRepository    studentRepository;
    private final DepartmentRepository departmentRepository;
    private final ObjectMapper         objectMapper;

    public StudentSyncListener(StudentRepository studentRepository,
                                DepartmentRepository departmentRepository,
                                ObjectMapper objectMapper) {
        this.studentRepository    = studentRepository;
        this.departmentRepository = departmentRepository;
        this.objectMapper         = objectMapper;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String json = new String(message.getBody(), StandardCharsets.UTF_8);
            StudentSyncMessage sync = objectMapper.readValue(json, StudentSyncMessage.class);
            applyUpdate(sync);
        } catch (Exception e) {
            // A malformed/unexpected event must never crash the listener thread.
            log.error("Failed to process student sync event", e);
        }
    }

    @Transactional
    void applyUpdate(StudentSyncMessage sync) {
        studentRepository.findByRollNo(sync.getRollNo()).ifPresentOrElse(student -> {
            student.setName(sync.buildFullName());
            student.setEmail(sync.getEmail());
            student.setPhone(sync.getPhone());
            student.setBatchYear(sync.getAdmissionYear());
            student.setCgpa(sync.getCgpa());
            student.setActiveBacklogs(sync.getActiveBacklogs());
            if (sync.getDeptId() != null) {
                departmentRepository.findById(sync.getDeptId()).ifPresent(student::setDepartment);
            }
            studentRepository.save(student);
            log.info("Synced student {} from Academic ERP update", sync.getRollNo());
        }, () -> log.debug("Ignoring sync for rollNo={} — not imported into PRMS", sync.getRollNo()));
    }
}
