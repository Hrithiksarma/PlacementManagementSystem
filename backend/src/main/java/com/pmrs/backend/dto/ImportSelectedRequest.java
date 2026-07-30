package com.pmrs.backend.dto;

import java.util.List;

public class ImportSelectedRequest {
    private List<Integer> ids;

    public List<Integer> getIds() { return ids; }
    public void          setIds(List<Integer> ids) { this.ids = ids; }
}
