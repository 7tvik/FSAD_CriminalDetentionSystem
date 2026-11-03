package com.kt.cdp.controller;

import com.kt.cdp.dto.release.ReleaseResponse;

import java.util.List;

/**
 * Minimal CSV rendering for release exports, built with plain strings (no external
 * CSV library). Values are quoted and embedded quotes doubled per RFC 4180.
 */
final class CsvSupport {

    private CsvSupport() {}

    /** Columns: prisonerNumber, prisonerName, expectedReleaseDate, releaseType, status. */
    static String releasesToCsv(List<ReleaseResponse> releases) {
        StringBuilder sb = new StringBuilder();
        sb.append("prisonerNumber,prisonerName,expectedReleaseDate,releaseType,status\n");
        for (ReleaseResponse r : releases) {
            sb.append(field(r.prisonerNumber())).append(',')
              .append(field(r.prisonerName())).append(',')
              .append(field(r.expectedReleaseDate() == null ? null : r.expectedReleaseDate().toString())).append(',')
              .append(field(r.releaseType() == null ? null : r.releaseType().name())).append(',')
              .append(field(r.status() == null ? null : r.status().name())).append('\n');
        }
        return sb.toString();
    }

    private static String field(String value) {
        String safe = value == null ? "" : value;
        return '"' + safe.replace("\"", "\"\"") + '"';
    }
}
