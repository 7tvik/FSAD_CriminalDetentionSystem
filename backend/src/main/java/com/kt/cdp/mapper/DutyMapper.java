package com.kt.cdp.mapper;

import com.kt.cdp.dto.duty.DutyResponse;
import com.kt.cdp.entity.DutyRecord;
import com.kt.cdp.entity.Staff;
import org.springframework.stereotype.Component;

/** Converts {@link DutyRecord} entities into {@link DutyResponse} DTOs. */
@Component
public class DutyMapper {

    public DutyResponse toResponse(DutyRecord duty) {
        Staff staff = duty.getStaff();
        return new DutyResponse(
                duty.getId(),
                staff != null ? staff.getId() : null,
                staff != null ? staff.getFullName() : null,
                staff != null ? staff.getStaffNumber() : null,
                duty.getLocation(),
                duty.getPurpose(),
                duty.getDutyDate(),
                duty.getStartTime(),
                duty.getEndTime(),
                duty.getRemarks(),
                duty.getCreatedAt()
        );
    }
}
