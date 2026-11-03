package com.kt.cdp.service;

import com.kt.cdp.dto.user.StaffDto;
import com.kt.cdp.entity.Staff;
import com.kt.cdp.repository.StaffRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/** Read-only access to staff records, used by duty and movement forms. */
@Service
public class StaffService {

    private final StaffRepository staffRepository;

    public StaffService(StaffRepository staffRepository) {
        this.staffRepository = staffRepository;
    }

    @Transactional(readOnly = true)
    public List<StaffDto> listActive() {
        return staffRepository.findAll().stream()
                .filter(Staff::isActive)
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    private StaffDto toDto(Staff staff) {
        return new StaffDto(
                staff.getId(),
                staff.getStaffNumber(),
                staff.getFullName(),
                staff.getRankTitle(),
                staff.getContact(),
                staff.isActive());
    }
}
