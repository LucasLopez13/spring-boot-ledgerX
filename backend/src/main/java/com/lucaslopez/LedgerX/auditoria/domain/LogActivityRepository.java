package com.lucaslopez.LedgerX.auditoria.domain;

import org.springframework.data.repository.CrudRepository;

public interface LogActivityRepository extends CrudRepository<ActivityLog, Long> {

}
