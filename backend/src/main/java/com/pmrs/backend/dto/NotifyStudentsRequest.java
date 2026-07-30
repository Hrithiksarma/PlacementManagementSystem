package com.pmrs.backend.dto;

import java.util.List;

public class NotifyStudentsRequest {
    private String       eventType;
    private List<Integer> studentIds;

    public String        getEventType()  { return eventType; }
    public void          setEventType(String eventType) { this.eventType = eventType; }
    public List<Integer> getStudentIds() { return studentIds; }
    public void          setStudentIds(List<Integer> studentIds) { this.studentIds = studentIds; }
}
