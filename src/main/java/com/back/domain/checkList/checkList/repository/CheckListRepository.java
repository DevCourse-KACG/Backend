package com.back.domain.checkList.checkList.repository;

import com.back.domain.checkList.checkList.entity.CheckList;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckListRepository extends JpaRepository<CheckList, Long> {

}
