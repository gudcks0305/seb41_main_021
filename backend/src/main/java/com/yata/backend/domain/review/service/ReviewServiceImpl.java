package com.yata.backend.domain.review.service;

import com.yata.backend.domain.member.entity.Member;
import com.yata.backend.domain.member.service.MemberService;
import com.yata.backend.domain.review.entity.Checklist;
import com.yata.backend.domain.review.entity.Review;
import com.yata.backend.domain.review.entity.ReviewChecklist;
import com.yata.backend.domain.review.repository.Review.JpaReviewRepository;
import com.yata.backend.domain.yata.entity.Yata;
import com.yata.backend.domain.yata.entity.YataMember;
import com.yata.backend.domain.yata.service.YataMemberService;
import com.yata.backend.domain.yata.service.YataService;
import com.yata.backend.global.exception.CustomLogicException;
import com.yata.backend.global.exception.ExceptionCode;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@Transactional
public class ReviewServiceImpl implements ReviewService {
    private final JpaReviewRepository jpaReviewRepository;
    private final YataService yataService;
    private final MemberService memberService;
    private final YataMemberService yataMemberService;
    private final CheckListService checkListService;

    public ReviewServiceImpl(YataService yataService,
                             MemberService memberService,
                             JpaReviewRepository jpaReviewRepository,
                             YataMemberService yataMemberservice,
                             CheckListService checkListService) {

        this.yataService = yataService;
        this.memberService = memberService;
        this.jpaReviewRepository = jpaReviewRepository;
        this.yataMemberService = yataMemberservice;
        this.checkListService = checkListService;
    }

    public Review createReview(List<Long> checkListIds, String username, long yataId, Long yataMemberId) {
        Yata yata = yataService.findYata(yataId);
        Member fromMember = memberService.findMember(username); //작성자
        Review review = new Review();
        YataMember yataMember = null;

        if (yataMemberId == null) {
            yataMember = yataMemberService.verifyPossibleYataMemberByUserName(yata, fromMember);
            review.setToMember(yata.getMember()); //대상자 : yata 글주인
            yataMember.setReviewWritten(true);
        } else {
            yataMember = yataMemberService.verifyPossibleYataMember(yataMemberId, yata);//존재하는 야타멤버아이딘지 확인해주고
            review.setToMember(yataMember.getMember()); //대상자 yataMember 리뷰 조회
            yataMember.setReviewReceived(true);
        }

        validateYataOwner(yataMemberId, yata, fromMember); // 운전자 일 경우 글주인 체크
        alreadyReviewed(yata, fromMember, review); // 리뷰 있는지 확인
        verifyPaidYataMember(yataMember); // 지불 했는지 확인
        review.setFromMember(fromMember);
        review.setYata(yata);

        List<Checklist> checklists = checkListService.checklistIdsToChecklists(checkListIds);
        List<ReviewChecklist> reviewChecklists = checkListService.checklistsToReviewChecklists(checklists, review);

        review.setReviewChecklists(reviewChecklists);

        calculateFuelTank(checklists, review.getToMember());
        return jpaReviewRepository.save(review);
    }

    public Map<Checklist, Long> findAllReview(String email) {
        Member member = memberService.findMember(email);
        List<Review> myReviews = jpaReviewRepository.findAllByToMember(member);

        Map<Checklist, Long> checklistCount = myReviews.stream()
                .flatMap(review -> review.getReviewChecklists().stream())
                .distinct()
                .collect(Collectors.groupingBy(ReviewChecklist::getChecklist,
                        Collectors.counting()));

        return checklistCount;
    }

    /*검증로직*/
    private void calculateFuelTank(List<Checklist> checklists, Member toMember) {
        int positiveCount = (int) checklists.stream().filter(Checklist::isCheckpn).count();
        int negativeCount = checklists.size() - positiveCount;

        BigDecimal ChangeNum = new BigDecimal(String.valueOf(0.1));
        BigDecimal FuelTankScore = new BigDecimal(String.valueOf(toMember.getFuelTank()));

        if (positiveCount > negativeCount) toMember.setFuelTank(FuelTankScore.add(ChangeNum).doubleValue());
        else toMember.setFuelTank(FuelTankScore.subtract(ChangeNum).doubleValue());
    }

    private void alreadyReviewed(Yata yata, Member fromMember, Review review) {
        // 해당 yataId 에 해당 member 가 한 리뷰가 이미 있는지
        jpaReviewRepository.findByYataAndFromMemberAndToMember(yata, fromMember, review.getToMember()).ifPresent(r -> {
            throw new CustomLogicException(ExceptionCode.ALREADY_REVIEWED);
        });
    }

    private void validateYataOwner(Long yataMemberId, Yata yata, Member fromMember) {
        if (yataMemberId != null) { //-> 작성자는 운전자임
            yataService.equalMember(yata.getMember().getEmail(), fromMember.getEmail());
        }
    }


    //yatamember가 결제 상태인지 검증
    private void verifyPaidYataMember(YataMember yataMember) {
        if (!yataMember.isYataPaid()) throw new CustomLogicException(ExceptionCode.PAYMENT_NOT_YET);
    }

}


