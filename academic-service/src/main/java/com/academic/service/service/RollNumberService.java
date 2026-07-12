package com.academic.service.service;

/**
 * Generates unique roll numbers for students.
 * Format: [YY][DDD][NN] — 7 digits, fully numeric.
 *   YY  = last 2 digits of admissionYear
 *   DDD = 3-digit department code (program · branch · section)
 *   NN  = 2-digit serial within this dept + year (01–99)
 */
public interface RollNumberService {

    String generateRollNumber(Integer deptId, Integer admissionYear);
}
