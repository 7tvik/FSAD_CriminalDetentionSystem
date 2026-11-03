-- =====================================================================
-- KT_CDP_019 - V2 Seed Data (fictional demo data only)
-- Users are seeded at application startup (DataInitializer) so passwords
-- are BCrypt-hashed rather than hardcoded. Roles/permissions and all
-- domain demo data are seeded here. Relative dates keep the demo relevant.
-- =====================================================================

-- ---------- Roles ----------
INSERT INTO roles (name, description) VALUES
 ('ADMINISTRATOR',       'Full system access'),
 ('PRISON_OFFICER',      'Manages prison operations'),
 ('POLICE_OFFICIAL',     'Views authorized crime/case information'),
 ('GOVERNMENT_OFFICIAL', 'Read-only reports and statistics'),
 ('AUDITOR',             'Read-only audit and historical records');

-- ---------- Permissions ----------
INSERT INTO permissions (name, description) VALUES
 ('PRISONER_READ','View prisoners'),('PRISONER_WRITE','Create/update prisoners'),('PRISONER_DELETE','Deactivate prisoners'),
 ('CASE_READ','View cases'),('CASE_WRITE','Create/update cases'),
 ('ARTICLE_READ','View surrendered articles'),('ARTICLE_WRITE','Create/update/return articles'),
 ('RELEASE_READ','View releases and diary'),('RELEASE_WRITE','Create/update releases'),
 ('DUTY_READ','View duty register'),('DUTY_WRITE','Create/update duty records'),
 ('INTERVIEW_READ','View interview requests'),('INTERVIEW_WRITE','Create interview requests'),('INTERVIEW_REVIEW','Approve/reject/schedule interviews'),
 ('MOVEMENT_READ','View movements'),('MOVEMENT_WRITE','Create/update movements'),
 ('COURT_READ','View court movements'),('COURT_WRITE','Create/update court movements'),
 ('REPORT_READ','View reports'),
 ('DASHBOARD_READ','View dashboard'),
 ('NOTIFICATION_READ','View notifications'),
 ('USER_READ','View users'),('USER_WRITE','Manage users'),
 ('ROLE_READ','View roles/permissions'),('ROLE_WRITE','Manage roles/permissions'),
 ('AUDIT_READ','View audit logs');

-- ---------- Role -> Permission mapping ----------
-- ADMINISTRATOR: all permissions
INSERT INTO role_permissions (role_id, permission_id)
 SELECT (SELECT id FROM roles WHERE name='ADMINISTRATOR'), p.id FROM permissions p;

-- PRISON_OFFICER: operational
INSERT INTO role_permissions (role_id, permission_id)
 SELECT (SELECT id FROM roles WHERE name='PRISON_OFFICER'), p.id FROM permissions p
 WHERE p.name IN ('PRISONER_READ','PRISONER_WRITE','CASE_READ','CASE_WRITE','ARTICLE_READ','ARTICLE_WRITE',
                  'RELEASE_READ','RELEASE_WRITE','DUTY_READ','DUTY_WRITE','INTERVIEW_READ','INTERVIEW_REVIEW',
                  'MOVEMENT_READ','MOVEMENT_WRITE','COURT_READ','COURT_WRITE','REPORT_READ','DASHBOARD_READ','NOTIFICATION_READ');

-- POLICE_OFFICIAL: authorized read of crime/case data
INSERT INTO role_permissions (role_id, permission_id)
 SELECT (SELECT id FROM roles WHERE name='POLICE_OFFICIAL'), p.id FROM permissions p
 WHERE p.name IN ('PRISONER_READ','CASE_READ','MOVEMENT_READ','COURT_READ','RELEASE_READ','REPORT_READ','DASHBOARD_READ','NOTIFICATION_READ');

-- GOVERNMENT_OFFICIAL: read-only reports/stats
INSERT INTO role_permissions (role_id, permission_id)
 SELECT (SELECT id FROM roles WHERE name='GOVERNMENT_OFFICIAL'), p.id FROM permissions p
 WHERE p.name IN ('PRISONER_READ','CASE_READ','RELEASE_READ','MOVEMENT_READ','COURT_READ','REPORT_READ','DASHBOARD_READ','NOTIFICATION_READ');

-- AUDITOR: read-only + audit
INSERT INTO role_permissions (role_id, permission_id)
 SELECT (SELECT id FROM roles WHERE name='AUDITOR'), p.id FROM permissions p
 WHERE p.name IN ('PRISONER_READ','CASE_READ','ARTICLE_READ','RELEASE_READ','DUTY_READ','INTERVIEW_READ',
                  'MOVEMENT_READ','COURT_READ','REPORT_READ','DASHBOARD_READ','NOTIFICATION_READ','AUDIT_READ');

-- ---------- Staff (user_id linked later by DataInitializer where relevant) ----------
INSERT INTO staff (staff_number, full_name, rank_title, contact, active, created_at, updated_at) VALUES
 ('STF-001','Ravi Menon','Head Warden','555-0101',TRUE,NOW(),NOW()),
 ('STF-002','Anita Rao','Warden','555-0102',TRUE,NOW(),NOW()),
 ('STF-003','John Mathew','Escort Officer','555-0103',TRUE,NOW(),NOW()),
 ('STF-004','Priya Nair','Duty Officer','555-0104',TRUE,NOW(),NOW());

-- ---------- Prisoners ----------
INSERT INTO prisoners (prisoner_number, first_name, last_name, date_of_birth, gender, address, emergency_contact, id_type, id_number, admission_date, status, facility, ward, cell, expected_release_date, active, created_at, updated_at) VALUES
 ('PRN-1001','Arjun','Desai','1990-03-12','MALE','12 River Rd, Springfield','Meera Desai 555-2001','NATIONAL_ID','NID-1001', DATE_SUB(CURDATE(), INTERVAL 200 DAY),'ACTIVE','Central Facility','A','A-12', DATE_ADD(CURDATE(), INTERVAL 0 DAY), TRUE,NOW(),NOW()),
 ('PRN-1002','Kabir','Shah','1985-07-22','MALE','5 Hillview, Metro City','Sana Shah 555-2002','NATIONAL_ID','NID-1002', DATE_SUB(CURDATE(), INTERVAL 150 DAY),'ACTIVE','Central Facility','A','A-14', DATE_ADD(CURDATE(), INTERVAL 3 DAY), TRUE,NOW(),NOW()),
 ('PRN-1003','Leela','Menon','1992-11-05','FEMALE','88 Palm St, Coastville','Ravi Menon 555-2003','NATIONAL_ID','NID-1003', DATE_SUB(CURDATE(), INTERVAL 90 DAY),'ACTIVE','Womens Facility','W','W-03', DATE_ADD(CURDATE(), INTERVAL 6 DAY), TRUE,NOW(),NOW()),
 ('PRN-1004','Farhan','Ali','1988-01-30','MALE','23 Oak Ave, Springfield','Zoya Ali 555-2004','PASSPORT','P-1004', DATE_SUB(CURDATE(), INTERVAL 300 DAY),'ACTIVE','Central Facility','B','B-01', DATE_ADD(CURDATE(), INTERVAL 20 DAY), TRUE,NOW(),NOW()),
 ('PRN-1005','Neha','Verma','1995-05-18','FEMALE','9 Lake View, Metro City','Amit Verma 555-2005','NATIONAL_ID','NID-1005', DATE_SUB(CURDATE(), INTERVAL 45 DAY),'ACTIVE','Womens Facility','W','W-07', DATE_ADD(CURDATE(), INTERVAL 25 DAY), TRUE,NOW(),NOW()),
 ('PRN-1006','Sanjay','Gupta','1979-09-09','MALE','4 Market Sq, Oldtown','Rekha Gupta 555-2006','NATIONAL_ID','NID-1006', DATE_SUB(CURDATE(), INTERVAL 500 DAY),'ACTIVE','Central Facility','B','B-09', DATE_ADD(CURDATE(), INTERVAL 60 DAY), TRUE,NOW(),NOW()),
 ('PRN-1007','Imran','Khan','1983-12-02','MALE','67 Grove St, Coastville','Nadia Khan 555-2007','NATIONAL_ID','NID-1007', DATE_SUB(CURDATE(), INTERVAL 400 DAY),'RELEASED','Central Facility','A','A-02', DATE_SUB(CURDATE(), INTERVAL 10 DAY), TRUE,NOW(),NOW()),
 ('PRN-1008','Divya','Iyer','1998-04-27','FEMALE','15 Sunset Blvd, Metro City','Karthik Iyer 555-2008','NATIONAL_ID','NID-1008', DATE_SUB(CURDATE(), INTERVAL 20 DAY),'ACTIVE','Womens Facility','W','W-11', DATE_ADD(CURDATE(), INTERVAL 120 DAY), TRUE,NOW(),NOW());

-- ---------- Cases (PRN-1001 intentionally has multiple cases) ----------
INSERT INTO cases (prisoner_id, case_number, fir_number, crime_type, crime_description, police_station, court, sections, arrest_date, status, investigating_officer, remarks, created_at, updated_at) VALUES
 ((SELECT id FROM prisoners WHERE prisoner_number='PRN-1001'),'CASE-2024-001','FIR-501','THEFT','Alleged burglary of retail store','Springfield PS','District Court 1','IPC 379','2024-08-01','UNDER_TRIAL','Insp. Rao','',NOW(),NOW()),
 ((SELECT id FROM prisoners WHERE prisoner_number='PRN-1001'),'CASE-2024-002','FIR-508','FRAUD','Related financial fraud case','Springfield PS','District Court 2','IPC 420','2024-08-15','UNDER_TRIAL','Insp. Rao','Linked to CASE-2024-001',NOW(),NOW()),
 ((SELECT id FROM prisoners WHERE prisoner_number='PRN-1002'),'CASE-2024-010','FIR-610','ASSAULT','Physical altercation','Metro PS','Sessions Court','IPC 324','2024-10-01','UNDER_TRIAL','Insp. Das','',NOW(),NOW()),
 ((SELECT id FROM prisoners WHERE prisoner_number='PRN-1003'),'CASE-2024-021','FIR-701','FORGERY','Document forgery','Coastville PS','District Court 1','IPC 465','2024-11-20','UNDER_TRIAL','Insp. Pillai','',NOW(),NOW()),
 ((SELECT id FROM prisoners WHERE prisoner_number='PRN-1004'),'CASE-2023-055','FIR-330','NARCOTICS','Possession','Springfield PS','Special Court','NDPS 20','2023-11-01','UNDER_TRIAL','Insp. Rao','',NOW(),NOW()),
 ((SELECT id FROM prisoners WHERE prisoner_number='PRN-1005'),'CASE-2024-030','FIR-720','THEFT','Shoplifting','Metro PS','District Court 2','IPC 379','2024-08-05','UNDER_TRIAL','Insp. Das','',NOW(),NOW()),
 ((SELECT id FROM prisoners WHERE prisoner_number='PRN-1006'),'CASE-2023-012','FIR-115','FRAUD','Large-scale fraud','Oldtown PS','Sessions Court','IPC 420','2023-05-10','CONVICTED','Insp. Kumar','',NOW(),NOW()),
 ((SELECT id FROM prisoners WHERE prisoner_number='PRN-1008'),'CASE-2024-044','FIR-810','CYBERCRIME','Online fraud','Metro Cyber PS','Special Court','IT Act 66D','2024-09-01','UNDER_INVESTIGATION','Insp. Sharma','',NOW(),NOW());

-- ---------- Surrendered Articles ----------
INSERT INTO surrendered_articles (prisoner_id, name, category, quantity, description, condition_note, received_date, returned_date, return_status, remarks, created_at, updated_at) VALUES
 ((SELECT id FROM prisoners WHERE prisoner_number='PRN-1001'),'Wallet','PERSONAL',1,'Brown leather wallet','Good', DATE_SUB(CURDATE(), INTERVAL 200 DAY),NULL,'HELD','',NOW(),NOW()),
 ((SELECT id FROM prisoners WHERE prisoner_number='PRN-1001'),'Mobile Phone','ELECTRONICS',1,'Black smartphone','Working', DATE_SUB(CURDATE(), INTERVAL 200 DAY),NULL,'HELD','',NOW(),NOW()),
 ((SELECT id FROM prisoners WHERE prisoner_number='PRN-1002'),'Watch','PERSONAL',1,'Steel wristwatch','Good', DATE_SUB(CURDATE(), INTERVAL 150 DAY),NULL,'HELD','',NOW(),NOW()),
 ((SELECT id FROM prisoners WHERE prisoner_number='PRN-1007'),'Ring','JEWELLERY',1,'Gold ring','Good', DATE_SUB(CURDATE(), INTERVAL 400 DAY), DATE_SUB(CURDATE(), INTERVAL 10 DAY),'RETURNED','Returned at release',NOW(),NOW());

-- ---------- Release Records (today / this week / this month / released) ----------
INSERT INTO release_records (prisoner_id, expected_release_date, release_type, reason, status, remarks, actual_release_date, created_at, updated_at) VALUES
 ((SELECT id FROM prisoners WHERE prisoner_number='PRN-1001'), DATE_ADD(CURDATE(), INTERVAL 0 DAY),'BAIL','Bail granted','READY','Release today',NULL,NOW(),NOW()),
 ((SELECT id FROM prisoners WHERE prisoner_number='PRN-1002'), DATE_ADD(CURDATE(), INTERVAL 3 DAY),'BAIL','Bail granted','UPCOMING','This week',NULL,NOW(),NOW()),
 ((SELECT id FROM prisoners WHERE prisoner_number='PRN-1003'), DATE_ADD(CURDATE(), INTERVAL 6 DAY),'SENTENCE_COMPLETE','Term completed','UPCOMING','This week',NULL,NOW(),NOW()),
 ((SELECT id FROM prisoners WHERE prisoner_number='PRN-1005'), DATE_ADD(CURDATE(), INTERVAL 25 DAY),'BAIL','Bail pending','ON_HOLD','On hold - verification',NULL,NOW(),NOW()),
 ((SELECT id FROM prisoners WHERE prisoner_number='PRN-1006'), DATE_ADD(CURDATE(), INTERVAL 60 DAY),'SENTENCE_COMPLETE','Scheduled','UPCOMING','Future',NULL,NOW(),NOW()),
 ((SELECT id FROM prisoners WHERE prisoner_number='PRN-1007'), DATE_SUB(CURDATE(), INTERVAL 10 DAY),'SENTENCE_COMPLETE','Term completed','RELEASED','Released', DATE_SUB(CURDATE(), INTERVAL 10 DAY),NOW(),NOW());

-- ---------- Duty Register ----------
INSERT INTO duty_register (staff_id, location, purpose, duty_date, start_time, end_time, remarks, created_at, updated_at) VALUES
 ((SELECT id FROM staff WHERE staff_number='STF-001'),'Main Gate','Gate security', CURDATE(),'08:00:00','16:00:00','',NOW(),NOW()),
 ((SELECT id FROM staff WHERE staff_number='STF-002'),'Ward A','Ward supervision', CURDATE(),'08:00:00','16:00:00','',NOW(),NOW()),
 ((SELECT id FROM staff WHERE staff_number='STF-004'),'Ward W','Ward supervision', CURDATE(),'16:00:00','23:59:00','',NOW(),NOW()),
 ((SELECT id FROM staff WHERE staff_number='STF-003'),'Court Escort','Prisoner escort to court', DATE_ADD(CURDATE(), INTERVAL 1 DAY),'07:00:00','15:00:00','',NOW(),NOW());

-- ---------- Interview Requests (various statuses) ----------
INSERT INTO interview_requests (prisoner_id, visitor_name, relationship, contact, id_details, requested_date, requested_time, purpose, status, review_date, remarks, created_at, updated_at) VALUES
 ((SELECT id FROM prisoners WHERE prisoner_number='PRN-1001'),'Meera Desai','SPOUSE','555-2001','NID-9001', DATE_ADD(CURDATE(), INTERVAL 2 DAY),'10:00:00','Family visit','PENDING',NULL,'',NOW(),NOW()),
 ((SELECT id FROM prisoners WHERE prisoner_number='PRN-1002'),'Sana Shah','SIBLING','555-2002','NID-9002', DATE_ADD(CURDATE(), INTERVAL 1 DAY),'11:00:00','Legal discussion','APPROVED', NOW(),'Approved',NOW(),NOW()),
 ((SELECT id FROM prisoners WHERE prisoner_number='PRN-1004'),'Zoya Ali','SPOUSE','555-2004','NID-9004', DATE_ADD(CURDATE(), INTERVAL 4 DAY),'09:30:00','Family visit','REJECTED', NOW(),'Slot unavailable',NOW(),NOW()),
 ((SELECT id FROM prisoners WHERE prisoner_number='PRN-1003'),'Ravi Menon','PARENT','555-2003','NID-9003', DATE_ADD(CURDATE(), INTERVAL 5 DAY),'14:00:00','Family visit','SCHEDULED', NOW(),'Scheduled',NOW(),NOW()),
 ((SELECT id FROM prisoners WHERE prisoner_number='PRN-1006'),'Rekha Gupta','SPOUSE','555-2006','NID-9006', DATE_SUB(CURDATE(), INTERVAL 3 DAY),'10:00:00','Family visit','COMPLETED', DATE_SUB(CURDATE(), INTERVAL 4 DAY),'Completed',NOW(),NOW());

-- ---------- Movements (In-Out register) ----------
INSERT INTO movements (person_type, person_ref_id, person_name, movement_type, destination, reason, movement_date, time_out, expected_return, actual_return, escort_officer, status, remarks, created_at, updated_at) VALUES
 ('PRISONER',(SELECT id FROM prisoners WHERE prisoner_number='PRN-1004'),'Farhan Ali','MEDICAL','City Hospital','Medical checkup', CURDATE(), TIMESTAMP(CURDATE(),'09:00:00'), TIMESTAMP(CURDATE(),'13:00:00'),NULL,(SELECT id FROM staff WHERE staff_number='STF-003'),'OUT','Currently out',NOW(),NOW()),
 ('PRISONER',(SELECT id FROM prisoners WHERE prisoner_number='PRN-1005'),'Neha Verma','MEDICAL','City Hospital','Follow-up', DATE_SUB(CURDATE(), INTERVAL 2 DAY), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY),'09:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY),'12:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 2 DAY),'11:30:00'),(SELECT id FROM staff WHERE staff_number='STF-003'),'RETURNED','Returned on time',NOW(),NOW()),
 -- Overdue: went out yesterday, expected back yesterday, still not returned
 ('PRISONER',(SELECT id FROM prisoners WHERE prisoner_number='PRN-1006'),'Sanjay Gupta','TRANSFER','District Jail','Transfer hearing', DATE_SUB(CURDATE(), INTERVAL 1 DAY), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY),'08:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 1 DAY),'17:00:00'),NULL,(SELECT id FROM staff WHERE staff_number='STF-003'),'OUT','Not yet returned',NOW(),NOW());

-- ---------- Court Hearings (scheduled + returned) ----------
INSERT INTO movements (person_type, person_ref_id, person_name, movement_type, destination, reason, movement_date, time_out, expected_return, actual_return, escort_officer, status, remarks, created_at, updated_at) VALUES
 ('PRISONER',(SELECT id FROM prisoners WHERE prisoner_number='PRN-1001'),'Arjun Desai','COURT','District Court 1','Hearing CASE-2024-001', DATE_ADD(CURDATE(), INTERVAL 1 DAY),NULL, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY),'16:00:00'),NULL,(SELECT id FROM staff WHERE staff_number='STF-003'),'SCHEDULED','Court production',NOW(),NOW());

INSERT INTO court_hearings (movement_id, prisoner_id, case_id, court, hearing_date, destination, departure_time, expected_return, actual_return, escort_officer, status, outcome, remarks, created_at, updated_at) VALUES
 ((SELECT id FROM movements WHERE person_name='Arjun Desai' AND movement_type='COURT' LIMIT 1),
  (SELECT id FROM prisoners WHERE prisoner_number='PRN-1001'),
  (SELECT id FROM cases WHERE case_number='CASE-2024-001'),
  'District Court 1', DATE_ADD(CURDATE(), INTERVAL 1 DAY),'District Court 1',NULL, TIMESTAMP(DATE_ADD(CURDATE(), INTERVAL 1 DAY),'16:00:00'),NULL,(SELECT id FROM staff WHERE staff_number='STF-003'),'SCHEDULED',NULL,'Upcoming hearing',NOW(),NOW()),
 (NULL,
  (SELECT id FROM prisoners WHERE prisoner_number='PRN-1002'),
  (SELECT id FROM cases WHERE case_number='CASE-2024-010'),
  'Sessions Court', DATE_SUB(CURDATE(), INTERVAL 5 DAY),'Sessions Court', TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY),'08:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY),'17:00:00'), TIMESTAMP(DATE_SUB(CURDATE(), INTERVAL 5 DAY),'16:30:00'),(SELECT id FROM staff WHERE staff_number='STF-003'),'RETURNED','Adjourned','Returned to prison',NOW(),NOW());

-- ---------- Notifications (broadcast; recipient null = all) ----------
INSERT INTO notifications (recipient_user_id, type, title, message, entity_type, entity_id, read_flag, created_at) VALUES
 (NULL,'RELEASE_UPCOMING','Release scheduled today','Arjun Desai (PRN-1001) is scheduled for release today.','PRISONER',NULL,FALSE,NOW()),
 (NULL,'MOVEMENT_OVERDUE','Overdue movement','Sanjay Gupta (PRN-1006) has not returned by expected time.','MOVEMENT',NULL,FALSE,NOW());
