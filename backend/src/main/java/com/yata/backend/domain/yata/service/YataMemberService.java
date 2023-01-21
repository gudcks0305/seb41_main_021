package com.yata.backend.domain.yata.service;

import com.yata.backend.domain.yata.entity.Yata;
import com.yata.backend.domain.yata.entity.YataMember;
import com.yata.backend.domain.yata.entity.YataRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface YataMemberService {
    void accept(String userName, Long yataRequestId, Long yataId);
    void reject(String userName, Long yataRequestId, Long yataId);
    Slice<YataMember> findAcceptedRequests(String userEmail, Long yataId, Pageable pageable);
    void verifyAppliedRequest(Yata yata, Long yataRequestId);

    YataMember verifyYataMember(long yataMemeberId);
}
