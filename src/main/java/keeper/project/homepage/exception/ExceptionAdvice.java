package keeper.project.homepage.exception;

import keeper.project.homepage.common.dto.result.CommonResult;
import keeper.project.homepage.exception.about.CustomStaticWriteContentNotFoundException;
import keeper.project.homepage.exception.about.CustomStaticWriteSubtitleImageNotFoundException;
import keeper.project.homepage.exception.about.CustomStaticWriteTitleNotFoundException;
import keeper.project.homepage.exception.about.CustomStaticWriteTypeNotFoundException;
import keeper.project.homepage.exception.attendance.CustomAttendanceException;
import keeper.project.homepage.exception.attendance.CustomGameIsOverException;
import keeper.project.homepage.exception.clerk.CustomClerkInaccessibleJobException;
import keeper.project.homepage.exception.clerk.CustomSeminarAttendanceFailException;
import keeper.project.homepage.exception.ctf.CustomContestNotFoundException;
import keeper.project.homepage.exception.ctf.CustomCtfCategoryNotFoundException;
import keeper.project.homepage.exception.ctf.CustomCtfChallengeNotFoundException;
import keeper.project.homepage.exception.ctf.CustomCtfTypeNotFoundException;
import keeper.project.homepage.exception.election.CustomCloseElectionVoteException;
import keeper.project.homepage.exception.election.CustomElectionAlreadyVotedException;
import keeper.project.homepage.exception.election.CustomElectionIsNotClosedException;
import keeper.project.homepage.exception.election.CustomElectionNotMatchCandidateException;
import keeper.project.homepage.exception.election.CustomElectionVoteCountNotMatchException;
import keeper.project.homepage.exception.election.CustomElectionCandidateExistException;
import keeper.project.homepage.exception.election.CustomElectionCandidateNotFoundException;
import keeper.project.homepage.exception.election.CustomElectionNotFoundException;
import keeper.project.homepage.exception.election.CustomElectionVoteDuplicationJobException;
import keeper.project.homepage.exception.election.CustomElectionVoterExistException;
import keeper.project.homepage.exception.election.CustomElectionVoterNotFoundException;
import keeper.project.homepage.exception.file.CustomInvalidImageFileException;
import keeper.project.homepage.exception.ctf.CustomCtfTeamNotFoundException;
import keeper.project.homepage.exception.file.CustomFileDeleteFailedException;
import keeper.project.homepage.exception.file.CustomFileEntityNotFoundException;
import keeper.project.homepage.exception.file.CustomFileNotFoundException;
import keeper.project.homepage.exception.file.CustomFileTransferFailedException;
import keeper.project.homepage.exception.file.CustomImageFormatException;
import keeper.project.homepage.exception.file.CustomImageIOException;
import keeper.project.homepage.exception.file.CustomThumbnailEntityNotFoundException;
import keeper.project.homepage.exception.library.CustomBookBorrowNotFoundException;
import keeper.project.homepage.exception.library.CustomBookDepartmentNotFoundException;
import keeper.project.homepage.exception.library.CustomBookNotFoundException;
import keeper.project.homepage.exception.library.CustomBookOverTheMaxException;
import keeper.project.homepage.exception.member.CustomAccessVirtualMemberException;
import keeper.project.homepage.exception.member.CustomAccountDeleteFailedException;
import keeper.project.homepage.exception.member.CustomMemberDuplicateException;
import keeper.project.homepage.exception.member.CustomMemberEmptyFieldException;
import keeper.project.homepage.exception.member.CustomMemberInfoNotFoundException;
import keeper.project.homepage.exception.member.CustomMemberNotFoundException;
import keeper.project.homepage.exception.point.CustomPointLogRequestNullException;
import keeper.project.homepage.exception.point.CustomPointLackException;
import keeper.project.homepage.exception.posting.CustomAccessRootCategoryException;
import keeper.project.homepage.exception.posting.CustomCategoryNotFoundException;
import keeper.project.homepage.exception.posting.CustomCommentEmptyFieldException;
import keeper.project.homepage.exception.posting.CustomCommentNotFoundException;
import keeper.project.homepage.exception.posting.CustomPostingAccessDeniedException;
import keeper.project.homepage.exception.posting.CustomPostingIncorrectException;
import keeper.project.homepage.exception.posting.CustomPostingNotFoundException;
import keeper.project.homepage.exception.posting.CustomPostingTempException;
import keeper.project.homepage.exception.sign.CustomAuthenticationEntryPointException;
import keeper.project.homepage.exception.sign.CustomLoginIdSigninFailedException;
import keeper.project.homepage.exception.sign.CustomSignUpFailedException;
import keeper.project.homepage.common.service.ResponseService;
import javax.servlet.http.HttpServletRequest;
import keeper.project.homepage.exception.study.CustomIpAddressNotFoundException;
import keeper.project.homepage.exception.study.CustomSeasonInvalidException;
import keeper.project.homepage.exception.study.CustomStudyIsNotMineException;
import keeper.project.homepage.exception.study.CustomStudyNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Log4j2
@RequiredArgsConstructor
@RestControllerAdvice
public class ExceptionAdvice {

  private final ResponseService responseService;
  private final MessageSource messageSource;

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult defaultException(HttpServletRequest request, Exception e) {
    // 예외 처리의 메시지를 MessageSource에서 가져오도록 수정
    return responseService.getFailResult(Integer.parseInt(getMessage("unKnown.code")),
        e.getMessage() == null ? getMessage("unKnown.msg") : e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult methodArgumentNotValidException(HttpServletRequest request,
      MethodArgumentNotValidException e) {
    BindingResult bindingResult = e.getBindingResult();
    StringBuilder errorMessage = new StringBuilder();
    for (FieldError fieldError : bindingResult.getFieldErrors()) {
      errorMessage.append("[");
      errorMessage.append(fieldError.getField());
      errorMessage.append("] 입력: ");
      errorMessage.append(fieldError.getRejectedValue()).append(" / ");
      errorMessage.append(fieldError.getDefaultMessage()).append(" ");

    }
    return responseService.getFailResult(HttpStatus.BAD_REQUEST.value(),
        errorMessage.toString().trim());
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult methodArgumentTypeMismatchException(HttpServletRequest request,
      MethodArgumentTypeMismatchException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("argumentTypeMismatch.code")),
        getMessage("argumentTypeMismatch.msg"));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult methodArgumentTypeMismatchException(HttpServletRequest request,
      MissingServletRequestParameterException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("missingServletRequestParameter.code")),
        getMessage("missingServletRequestParameter.msg"));
  }

  @ExceptionHandler(CustomMemberNotFoundException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult memberNotFoundException(HttpServletRequest request,
      CustomMemberNotFoundException e) {
    // 예외 처리의 메시지를 MessageSource에서 가져오도록 수정
    return responseService.getFailResult(Integer.parseInt(getMessage("memberNotFound.code")),
        e.getMessage() == null ? getMessage("memberNotFound.msg") : e.getMessage());
  }

  // code정보에 해당하는 메시지를 조회합니다.
  public String getMessage(String code) {
    return getMessage(code, null);
  }

  // code정보, 추가 argument로 현재 locale에 맞는 메시지를 조회합니다.
  public String getMessage(String code, Object[] args) {
    return messageSource.getMessage(code, args, LocaleContextHolder.getLocale());
  }

  // ExceptionAdvice
  @ExceptionHandler(CustomLoginIdSigninFailedException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult signInFailed(HttpServletRequest request,
      CustomLoginIdSigninFailedException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("SigninFailed.code")),
        e.getMessage() == null ? getMessage("SigninFailed.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomAuthenticationEntryPointException.class)
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  public CommonResult authenticationEntryPointException(HttpServletRequest request,
      CustomAuthenticationEntryPointException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("entryPointException.code")),
        getMessage("entryPointException.msg"));
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public CommonResult accessDeniedException(HttpServletRequest request, AccessDeniedException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("accessDenied.code")),
        e.getMessage() == null ? getMessage("accessDenied.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomSignUpFailedException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public CommonResult signUpFailedException(HttpServletRequest request,
      CustomSignUpFailedException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("signUpFailed.code")),
        e.getMessage() == null ? getMessage("signUpFailed.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomFileNotFoundException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public CommonResult fileNotFoundException(HttpServletRequest request,
      CustomFileNotFoundException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("fileNotFound.code")),
        e.getMessage() == null ? getMessage("fileNotFound.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomPointLackException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public CommonResult transferPointLackException(HttpServletRequest request,
      CustomPointLackException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("pointLackException.code")),
        e.getMessage() == null ? getMessage("pointLackException.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomAttendanceException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public CommonResult attendanceException(HttpServletRequest request,
      CustomAttendanceException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("attendanceFailed.code")),
        e.getMessage() == null ? getMessage("attendanceFailed.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomGameIsOverException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public CommonResult gameIsOverException(HttpServletRequest request, CustomGameIsOverException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("gameIsOver.code")),
        e.getMessage() == null ? getMessage("gameIsOver.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomBookNotFoundException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult bookNotFoundException(HttpServletRequest request,
      CustomBookNotFoundException e) {
    // 예외 처리의 메시지를 MessageSource에서 가져오도록 수정
    return responseService.getFailResult(Integer.parseInt(getMessage("bookNotFound.code")),
        getMessage("bookNotFound.msg"));
  }

  @ExceptionHandler(CustomBookOverTheMaxException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult bookOverTheMaxException(HttpServletRequest request,
      CustomBookOverTheMaxException e) {
    // 예외 처리의 메시지를 MessageSource에서 가져오도록 수정
    return responseService.getFailResult(Integer.parseInt(getMessage("bookOverTheMax.code")),
        getMessage("bookOverTheMax.msg"));
  }

  @ExceptionHandler(CustomFileDeleteFailedException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult fileDeleteFailedException(HttpServletRequest request,
      CustomFileDeleteFailedException e) {
    // 예외 처리의 메시지를 MessageSource에서 가져오도록 수정
    return responseService.getFailResult(Integer.parseInt(getMessage("fileDeleteFailed.code")),
        getMessage("fileDeleteFailed.msg"));
  }

  @ExceptionHandler(CustomFileTransferFailedException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult fileTransferFailedException(HttpServletRequest request,
      CustomFileTransferFailedException e) {
    // 예외 처리의 메시지를 MessageSource에서 가져오도록 수정
    return responseService.getFailResult(Integer.parseInt(getMessage("fileTransferFailed.code")),
        getMessage("fileTransferFailed.msg"));
  }

  @ExceptionHandler(CustomFileEntityNotFoundException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult fileEntityNotFoundException(HttpServletRequest request,
      CustomFileEntityNotFoundException e) {
    // 예외 처리의 메시지를 MessageSource에서 가져오도록 수정
    return responseService.getFailResult(
        Integer.parseInt(getMessage("fileEntityNotFoundFailed.code")),
        getMessage("fileEntityNotFoundFailed.msg"));
  }

  @ExceptionHandler(CustomThumbnailEntityNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult CustomThumbnailEntityNotFoundException(HttpServletRequest request,
      CustomThumbnailEntityNotFoundException e) {
    // 예외 처리의 메시지를 MessageSource에서 가져오도록 수정
    return responseService.getFailResult(
        Integer.parseInt(getMessage("thumbnailEntityNotFoundFailed.code")),
        getMessage("thumbnailEntityNotFoundFailed.msg"));
  }

  @ExceptionHandler(CustomImageFormatException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult imageFormatException(HttpServletRequest request,
      CustomImageFormatException e) {
    // 예외 처리의 메시지를 MessageSource에서 가져오도록 수정
    return responseService.getFailResult(Integer.parseInt(getMessage("imageFormat.code")),
        getMessage("imageFormat.msg"));
  }

  @ExceptionHandler(CustomImageIOException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult imageIOException(HttpServletRequest request,
      CustomImageIOException e) {
    // 예외 처리의 메시지를 MessageSource에서 가져오도록 수정
    return responseService.getFailResult(Integer.parseInt(getMessage("imageIO.code")),
        getMessage("imageIO.msg"));
  }

  @ExceptionHandler(CustomInvalidImageFileException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public CommonResult invalidImageFileException(HttpServletRequest request,
      CustomInvalidImageFileException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("invalidImageFile.code")),
        e.getMessage() == null ? getMessage("invalidImageFile.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomMemberEmptyFieldException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult memberEmptyFieldException(HttpServletRequest request,
      CustomMemberEmptyFieldException e) {
    // 예외 처리의 메시지를 MessageSource에서 가져오도록 수정
    return responseService.getFailResult(Integer.parseInt(getMessage("memberEmptyField.code")),
        getMessage("memberEmptyField.msg"));
  }

  @ExceptionHandler(CustomMemberInfoNotFoundException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult memberInfoNotFoundException(HttpServletRequest request,
      CustomMemberInfoNotFoundException e) {
    // 예외 처리의 메시지를 MessageSource에서 가져오도록 수정
    return responseService.getFailResult(Integer.parseInt(getMessage("memberInfoNotFound.code")),
        e.getMessage() == null ? getMessage("memberInfoNotFound.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomMemberDuplicateException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult memberDuplicateException(HttpServletRequest request,
      CustomMemberDuplicateException e) {
    // 예외 처리의 메시지를 MessageSource에서 가져오도록 수정
    return responseService.getFailResult(Integer.parseInt(getMessage("memberDuplicate.code")),
        e.getMessage() == null ? getMessage("memberDuplicate.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomCommentNotFoundException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult commentNotFoundException(HttpServletRequest request,
      CustomCommentNotFoundException e) {
    // 예외 처리의 메시지를 MessageSource에서 가져오도록 수정
    return responseService.getFailResult(Integer.parseInt(getMessage("commentNotFound.code")),
        getMessage("commentNotFound.msg"));
  }

  @ExceptionHandler(CustomAccountDeleteFailedException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult accountDeleteFailedException(HttpServletRequest request,
      CustomAccountDeleteFailedException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("accountDeleteFailed.code")),
        getMessage("accountDeleteFailed.msg"));
  }

  @ExceptionHandler(CustomCommentEmptyFieldException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult commentEmptyFieldException(HttpServletRequest request,
      CustomCommentEmptyFieldException e) {
    // 예외 처리의 메시지를 MessageSource에서 가져오도록 수정
    return responseService.getFailResult(Integer.parseInt(getMessage("commentEmptyField.code")),
        e.getMessage() == null ? getMessage("commentEmptyField.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomNumberOverflowException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult numberOverflowException(HttpServletRequest request,
      CustomNumberOverflowException e) {
    // 예외 처리의 메시지를 MessageSource에서 가져오도록 수정
    return responseService.getFailResult(Integer.parseInt(getMessage("numberOverflow.code")),
        getMessage("numberOverflow.msg"));
  }

  @ExceptionHandler(CustomCategoryNotFoundException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult categoryNotFoundException(HttpServletRequest request,
      CustomCategoryNotFoundException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("categoryNotFound.code")),
        getMessage("categoryNotFound.msg"));
  }

  @ExceptionHandler(CustomAccessRootCategoryException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult accessRootCategoryException(HttpServletRequest request,
      CustomAccessRootCategoryException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("accessRootCategory.code")),
        getMessage("accessRootCategory.msg"));
  }

  @ExceptionHandler(CustomBookBorrowNotFoundException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult bookBorrowNotFoundException(HttpServletRequest request,
      CustomBookBorrowNotFoundException e) {
    // 예외 처리의 메시지를 MessageSource에서 가져오도록 수정
    return responseService.getFailResult(Integer.parseInt(getMessage("bookBorrowNotFound.code")),
        getMessage("bookBorrowNotFound.msg"));
  }

  @ExceptionHandler(CustomPointLogRequestNullException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult pointLogRequestNullException(HttpServletRequest request,
      CustomPointLogRequestNullException e) {
    // 예외 처리의 메시지를 MessageSource에서 가져오도록 수정
    return responseService.getFailResult(
        Integer.parseInt(getMessage("pointLogRequestNullException.code")),
        getMessage("pointLogRequestNullException.msg"));
  }

  @ExceptionHandler(CustomBookDepartmentNotFoundException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult bookDepartmentNotFoundException(HttpServletRequest request,
      CustomBookDepartmentNotFoundException e) {
    // 예외 처리의 메시지를 MessageSource에서 가져오도록 수정
    return responseService.getFailResult(
        Integer.parseInt(getMessage("bookDepartmentNotFound.code")),
        getMessage("bookDepartmentNotFound.msg"));
  }

  @ExceptionHandler(CustomSeasonInvalidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult seasonInvalid(HttpServletRequest request,
      CustomSeasonInvalidException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("seasonInvalid.code")),
        e.getMessage() == null ? getMessage("seasonInvalid.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomIpAddressNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult ipAddressNotFound(HttpServletRequest request,
      CustomIpAddressNotFoundException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("ipAddressNotFound.code")),
        e.getMessage() == null ? getMessage("ipAddressNotFound.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomStudyNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult studyNotFound(HttpServletRequest request,
      CustomStudyNotFoundException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("studyNotFound.code")),
        e.getMessage() == null ? getMessage("studyNotFound.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomStudyIsNotMineException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult studyNotMine(HttpServletRequest request,
      CustomStudyIsNotMineException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("studyNotMine.code")),
        e.getMessage() == null ? getMessage("studyNotMine.msg") : e.getMessage());
  }

  @ExceptionHandler(EmptyResultDataAccessException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult dataAccessException(HttpServletRequest request,
      EmptyResultDataAccessException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("dataNotFound.code")),
        e.getMessage() == null ? getMessage("dataNotFound.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomAccessVirtualMemberException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult accessVirtualMember(HttpServletRequest request,
      CustomAccessVirtualMemberException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("accessVirtualMember.code")),
        e.getMessage() == null ? getMessage("accessVirtualMember.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomPostingNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult postingNotFound(HttpServletRequest request,
      CustomPostingNotFoundException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("postingNotFound.code")),
        e.getMessage() == null ? getMessage("postingNotFound.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomPostingIncorrectException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult postingIncorrect(HttpServletRequest request,
      CustomPostingIncorrectException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("postingIncorrect.code")),
        e.getMessage() == null ? getMessage("postingIncorrect.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomPostingTempException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult postingTemp(HttpServletRequest request,
      CustomPostingTempException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("postingTemp.code")),
        e.getMessage() == null ? getMessage("postingTemp.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomPostingAccessDeniedException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult postingAccessDenied(HttpServletRequest request,
      CustomPostingAccessDeniedException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("postingAccessDenied.code")),
        e.getMessage() == null ? getMessage("postingAccessDenied.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomStaticWriteTypeNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult staticWriteTitleNotFound(HttpServletRequest request,
      CustomStaticWriteTypeNotFoundException e) {
    return responseService.getFailResult(
        Integer.parseInt(getMessage("staticWriteTypeNotFound.code")),
        e.getMessage() == null ? getMessage("staticWriteTypeNotFound.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomStaticWriteTitleNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult staticWriteTitleNotFound(HttpServletRequest request,
      CustomStaticWriteTitleNotFoundException e) {
    return responseService.getFailResult(
        Integer.parseInt(getMessage("staticWriteTitleNotFound.code")),
        e.getMessage() == null ? getMessage("staticWriteTitleNotFound.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomStaticWriteSubtitleImageNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult staticWriteSubtitleImageNotFound(HttpServletRequest request,
      CustomStaticWriteSubtitleImageNotFoundException e) {
    return responseService.getFailResult(
        Integer.parseInt(getMessage("staticWriteSubtitleImageNotFound.code")),
        e.getMessage() == null ? getMessage("staticWriteSubtitleImageNotFound.msg")
            : e.getMessage());
  }

  @ExceptionHandler(CustomStaticWriteContentNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult staticWriteContentNotFound(HttpServletRequest request,
      CustomStaticWriteContentNotFoundException e) {
    return responseService.getFailResult(
        Integer.parseInt(getMessage("staticWriteContentNotFound.code")),
        e.getMessage() == null ? getMessage("staticWriteContentNotFound.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomContestNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult contestNotFound(HttpServletRequest request,
      CustomContestNotFoundException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("contestNotFound.code")),
        e.getMessage() == null ? getMessage("contestNotFound.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomCtfCategoryNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult ctfCategoryNotFound(HttpServletRequest request,
      CustomCtfCategoryNotFoundException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("ctfCategoryNotFound.code")),
        e.getMessage() == null ? getMessage("ctfCategoryNotFound.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomCtfTypeNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult ctfTypeNotFound(HttpServletRequest request,
      CustomCtfTypeNotFoundException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("ctfTypeNotFound.code")),
        e.getMessage() == null ? getMessage("ctfTypeNotFound.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomCtfChallengeNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult ctfChallengeNotFound(HttpServletRequest request,
      CustomCtfChallengeNotFoundException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("ctfChallengeNotFound.code")),
        e.getMessage() == null ? getMessage("ctfChallengeNotFound.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomCtfTeamNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult teamNotFound(HttpServletRequest request,
      CustomCtfTeamNotFoundException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("ctfTeamNotFound.code")),
        e.getMessage() == null ? getMessage("ctfTeamNotFound.msg") : e.getMessage());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  protected CommonResult dataDuplicate(HttpServletRequest request,
      DataIntegrityViolationException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("dataDuplicate.code")),
        e.getMessage() == null ? getMessage("dataDuplicate.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomElectionNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult electionNotFound(HttpServletRequest request,
      CustomElectionNotFoundException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("electionNotFound.code")),
        e.getMessage() == null ? getMessage("electionNotFound.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomElectionCandidateNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult electionCandidateNotFound(HttpServletRequest request,
      CustomElectionCandidateNotFoundException e) {
    return responseService.getFailResult(
        Integer.parseInt(getMessage("electionCandidateNotFound.code")),
        e.getMessage() == null ? getMessage("electionCandidateNotFound.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomElectionVoterNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult electionVoterNotFound(HttpServletRequest request,
      CustomElectionVoterNotFoundException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("electionVoterNotFound.code")),
        e.getMessage() == null ? getMessage("electionVoterNotFound.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomElectionCandidateExistException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult electionCandidateExist(HttpServletRequest request,
      CustomElectionCandidateExistException e) {
    return responseService.getFailResult(
        Integer.parseInt(getMessage("electionCandidateExist.code")),
        e.getMessage() == null ? getMessage("electionCandidateExist.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomElectionVoterExistException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult electionVoterExist(HttpServletRequest request,
      CustomElectionVoterExistException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("electionVoterExist.code")),
        e.getMessage() == null ? getMessage("electionVoterExist.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomElectionVoteCountNotMatchException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult electionCandidateCountNotMatch(HttpServletRequest request,
      CustomElectionVoteCountNotMatchException e) {
    return responseService.getFailResult(
        Integer.parseInt(getMessage("electionVoteCountNotMatch.code")),
        e.getMessage() == null ? getMessage("electionVoteCountNotMatch.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomElectionVoteDuplicationJobException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult electionVoteDuplicationJob(HttpServletRequest request,
      CustomElectionVoteDuplicationJobException e) {
    return responseService.getFailResult(
        Integer.parseInt(getMessage("electionVoteDuplicationJob.code")),
        e.getMessage() == null ? getMessage("electionVoteDuplicationJob.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomCloseElectionVoteException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult closeElectionVote(HttpServletRequest request,
      CustomCloseElectionVoteException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("closeElectionVote.code")),
        e.getMessage() == null ? getMessage("closeElectionVote.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomElectionNotMatchCandidateException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult electionNotMatchCandidate(HttpServletRequest request,
      CustomElectionNotMatchCandidateException e) {
    return responseService.getFailResult(
        Integer.parseInt(getMessage("electionNotMatchCandidate.code")),
        e.getMessage() == null ? getMessage("electionNotMatchCandidate.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomElectionAlreadyVotedException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult electionAlreadyVoted(HttpServletRequest request,
      CustomElectionAlreadyVotedException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("alreadyVoted.code")),
        e.getMessage() == null ? getMessage("alreadyVoted.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomElectionIsNotClosedException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult electionIsNotClosed(HttpServletRequest request,
      CustomElectionIsNotClosedException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("IsNotClosedElection.code")),
        e.getMessage() == null ? getMessage("IsNotClosedElection.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomClerkInaccessibleJobException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult clerkInaccessibleJob(CustomClerkInaccessibleJobException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("inaccessibleJob.code")),
        e.getMessage() == null ? getMessage("inaccessibleJob.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomSeminarAttendanceFailException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult seminarAttendanceFail(CustomSeminarAttendanceFailException e) {
    return responseService.getFailResult(Integer.parseInt(getMessage("seminarAttendanceFail.code")),
        e.getMessage() == null ? getMessage("seminarAttendanceFail.msg") : e.getMessage());
  }

}