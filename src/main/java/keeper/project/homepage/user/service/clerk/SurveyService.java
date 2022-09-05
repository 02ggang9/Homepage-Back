package keeper.project.homepage.user.service.clerk;

import static keeper.project.homepage.entity.clerk.SurveyReplyEntity.SurveyReply.OTHER_DORMANT;
import static keeper.project.homepage.util.service.SurveyUtilService.NO_SURVEY;

import java.time.LocalDateTime;
import java.util.Optional;
import keeper.project.homepage.user.dto.clerk.response.ClosedSurveyInformationResponseDto;
import keeper.project.homepage.common.service.util.AuthService;
import keeper.project.homepage.entity.clerk.SurveyEntity;
import keeper.project.homepage.entity.clerk.SurveyMemberReplyEntity;
import keeper.project.homepage.entity.clerk.SurveyReplyEntity;
import keeper.project.homepage.entity.clerk.SurveyReplyExcuseEntity;
import keeper.project.homepage.entity.member.MemberEntity;
import keeper.project.homepage.exception.clerk.CustomSurveyInVisibleException;
import keeper.project.homepage.exception.clerk.CustomSurveyMemberReplyNotFoundException;
import keeper.project.homepage.exception.clerk.CustomSurveyNotFoundException;
import keeper.project.homepage.repository.clerk.SurveyMemberReplyRepository;
import keeper.project.homepage.repository.clerk.SurveyReplyExcuseRepository;
import keeper.project.homepage.repository.clerk.SurveyReplyRepository;
import keeper.project.homepage.repository.clerk.SurveyRepository;
import keeper.project.homepage.user.dto.clerk.response.SurveyModifyResponseDto;
import keeper.project.homepage.user.dto.clerk.request.SurveyResponseRequestDto;
import keeper.project.homepage.user.dto.clerk.response.SurveyInformationResponseDto;
import keeper.project.homepage.user.service.member.MemberUtilService;
import keeper.project.homepage.util.service.SurveyUtilService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SurveyService {

  private final SurveyRepository surveyRepository;
  private final SurveyMemberReplyRepository surveyMemberReplyRepository;
  private final SurveyReplyRepository surveyReplyRepository;
  private final SurveyReplyExcuseRepository surveyReplyExcuseRepository;
  private final SurveyUtilService surveyUtilService;
  private final MemberUtilService memberUtilService;
  private final AuthService authService;

  @Transactional
  public Long responseSurvey(Long surveyId, SurveyResponseRequestDto requestDto) {
    SurveyEntity survey = surveyUtilService.getSurveyById(surveyId);
    validateVisibleSurvey(survey);
    MemberEntity member = authService.getMemberEntityWithJWT();

    return surveyMemberReplyRepository.save(generateMemberReplyEntity(survey, member, requestDto))
        .getId();
  }

  private SurveyMemberReplyEntity generateMemberReplyEntity(SurveyEntity survey,
      MemberEntity member, SurveyResponseRequestDto requestDto) {
    SurveyMemberReplyEntity memberReply = SurveyMemberReplyEntity.builder()
        .member(member)
        .survey(survey)
        .reply(surveyReplyRepository.getById(requestDto.getReplyId()))
        .replyTime(LocalDateTime.now())
        .build();

    survey.getRespondents().add(memberReply);

    if (requestDto.getReplyId().equals(OTHER_DORMANT.getId())) {
      SurveyReplyExcuseEntity surveyReplyExcuseEntity = SurveyReplyExcuseEntity.newInstance(
          memberReply,
          requestDto.getExcuse());

      memberReply.setMemberReplyExcuse(surveyReplyExcuseEntity);
    }

    return memberReply;
  }

  private void validateVisibleSurvey(SurveyEntity survey) {
    if (survey.getIsVisible().equals(false)) {
      throw new CustomSurveyInVisibleException();
    }
  }

  @Transactional
  public SurveyModifyResponseDto modifyResponse(Long surveyId,
      SurveyResponseRequestDto requestDto) {
    MemberEntity member = authService.getMemberEntityWithJWT();

    SurveyMemberReplyEntity memberReply = surveyMemberReplyRepository.findBySurveyIdAndMemberId(
            surveyId, member.getId())
        .orElseThrow(CustomSurveyMemberReplyNotFoundException::new);

    SurveyReplyEntity modifyReplyType = surveyReplyRepository.getById(requestDto.getReplyId());
    SurveyReplyEntity beforeReply = memberReply.getReply();
    modifyReply(memberReply, requestDto, beforeReply, modifyReplyType);

    return SurveyModifyResponseDto.from(surveyMemberReplyRepository.save(memberReply),
        requestDto.getExcuse());
  }

  private void modifyReply(SurveyMemberReplyEntity memberReply, SurveyResponseRequestDto requestDto,
      SurveyReplyEntity beforeReply, SurveyReplyEntity modifyReplyType) {
    memberReply.setReply(modifyReplyType); // 응답 갱신
    memberReply.setReplyTime(LocalDateTime.now());

    if (beforeReply.getId().equals(OTHER_DORMANT.getId())) {
      // 기타에서 기타로 수정
      if (modifyReplyType.getId().equals(OTHER_DORMANT.getId())) {
        memberReply.getSurveyReplyExcuseEntity().setRestExcuse(requestDto.getExcuse());
        return;
      }
      // 기타에서 활동 등으로 수정
      memberReply.setSurveyReplyExcuseEntity(null);
    }
    // 활동 등에서 기타로 수정
    if (modifyReplyType.getId().equals(OTHER_DORMANT.getId())) {
      SurveyReplyExcuseEntity surveyReplyExcuseEntity = SurveyReplyExcuseEntity.newInstance(
          memberReply,
          requestDto.getExcuse());

      memberReply.setMemberReplyExcuse(surveyReplyExcuseEntity);
    }
  }

  public SurveyInformationResponseDto getSurveyInformation(Long surveyId) {
    SurveyEntity survey = surveyRepository.findById(surveyId)
        .orElseThrow(CustomSurveyNotFoundException::new);
    Long reqMemberId = authService.getMemberIdByJWT();
    MemberEntity member = memberUtilService.getById(reqMemberId);

    Boolean isResponded = false;
    SurveyMemberReplyEntity surveyMemberReplyEntity = null;
    if (isUserRespondedSurvey(survey, member)) {
      isResponded = true;
      surveyMemberReplyEntity = surveyMemberReplyRepository.findBySurveyIdAndMemberId(
              surveyId, reqMemberId)
          .orElseThrow(CustomSurveyMemberReplyNotFoundException::new);
    }

    return SurveyInformationResponseDto.of(survey, surveyMemberReplyEntity, isResponded);
  }

  private Boolean isUserRespondedSurvey(SurveyEntity survey, MemberEntity member) {
    return survey.getRespondents()
        .stream()
        .map(SurveyMemberReplyEntity::getMember)
        .toList()
        .contains(member);
  }

  public Long getLatestVisibleSurveyId() {
    LocalDateTime now = LocalDateTime.now();
    SurveyEntity survey = surveyRepository.findTop1ByOpenTimeBeforeAndCloseTimeAfterAndIsVisibleTrueOrderByCloseTimeDesc(
            now, now)
        .orElse(NO_SURVEY);

    return survey.getId();
  }

  public ClosedSurveyInformationResponseDto getLatestClosedSurveyInformation() {
    LocalDateTime now = LocalDateTime.now();
    SurveyEntity latestClosedSurvey = surveyRepository
        .findTop1ByCloseTimeBeforeAndIsVisibleTrueOrderByCloseTimeDesc(now)
        .orElse(NO_SURVEY);
    Long reqMemberId = authService.getMemberIdByJWT();

    Optional<SurveyMemberReplyEntity> surveyMemberReply = surveyMemberReplyRepository
        .findBySurveyIdAndMemberId(latestClosedSurvey.getId(), reqMemberId);
    if (surveyMemberReply.isEmpty()) {
      return ClosedSurveyInformationResponseDto.noResponse(latestClosedSurvey);
    }
    return ClosedSurveyInformationResponseDto.of(latestClosedSurvey, surveyMemberReply.get());
  }

}
