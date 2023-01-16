package com.yata.backend.domain.yata.repository.yataRequestRepo;

import com.yata.backend.domain.yata.entity.Yata;
import com.yata.backend.domain.yata.entity.YataRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface JpaYataRequestRepository extends JpaRepository<YataRequest, Long>, YataRequestRepository {
    Optional<YataRequest> findByMember_EmailAndYata_YataId(String email, Long yataId);
    Slice<YataRequest> findAllByYata(Yata yata, Pageable pageable);
}
