package com.back.api.v1.domain.checkList.checkList.repository;

import com.back.api.v1.domain.checkList.checkList.entity.CheckList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckListRepository extends JpaRepository<CheckList, Long> {

}
