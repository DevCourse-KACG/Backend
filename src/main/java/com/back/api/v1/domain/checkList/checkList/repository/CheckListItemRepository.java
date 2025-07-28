package com.back.api.v1.domain.checkList.checkList.repository;

import com.back.api.v1.domain.checkList.checkList.entity.CheckListItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CheckListItemRepository extends JpaRepository<CheckListItem, Long> {

}
