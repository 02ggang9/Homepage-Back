package keeper.project.homepage;

import keeper.project.homepage.util.dto.result.CommonResult;
import keeper.project.homepage.systemadmin.exception.CustomClerkInaccessibleJobException;
import keeper.project.homepage.clerk.exception.CustomSeminarAttendanceFailException;
import keeper.project.homepage.util.exception.CustomNumberOverflowException;
import keeper.project.homepage.util.exception.ExceptionAdviceUtil;
import keeper.project.homepage.util.exception.file.CustomInvalidImageFileException;
import keeper.project.homepage.util.exception.file.CustomFileDeleteFailedException;
import keeper.project.homepage.util.exception.file.CustomFileEntityNotFoundException;
import keeper.project.homepage.util.exception.file.CustomFileNotFoundException;
import keeper.project.homepage.util.exception.file.CustomFileTransferFailedException;
import keeper.project.homepage.util.exception.file.CustomImageFormatException;
import keeper.project.homepage.util.exception.file.CustomImageIOException;
import keeper.project.homepage.util.exception.file.CustomThumbnailEntityNotFoundException;
import keeper.project.homepage.member.exception.CustomMemberInfoNotFoundException;
import keeper.project.homepage.member.exception.CustomMemberNotFoundException;
import keeper.project.homepage.util.service.result.ResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
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
  private final ExceptionAdviceUtil exceptionUtil;

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult defaultException(Exception e) {
    return responseService.getFailResult(Integer.parseInt(exceptionUtil.getMessage("unKnown.code")),
        e.getMessage() == null ? exceptionUtil.getMessage("unKnown.msg") : e.getMessage());
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult methodArgumentNotValidException(MethodArgumentNotValidException e) {
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
  protected CommonResult methodArgumentTypeMismatchException(
      MethodArgumentTypeMismatchException e) {
    return responseService.getFailResult(
        Integer.parseInt(exceptionUtil.getMessage("argumentTypeMismatch.code")),
        exceptionUtil.getMessage("argumentTypeMismatch.msg"));
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  public CommonResult accessDeniedException(AccessDeniedException e) {
    return responseService.getFailResult(
        Integer.parseInt(exceptionUtil.getMessage("accessDenied.code")),
        e.getMessage() == null ? exceptionUtil.getMessage("accessDenied.msg") : e.getMessage());
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult methodArgumentTypeMismatchException(
      MissingServletRequestParameterException e) {
    return responseService.getFailResult(
        Integer.parseInt(exceptionUtil.getMessage("missingServletRequestParameter.code")),
        exceptionUtil.getMessage("missingServletRequestParameter.msg"));
  }

  @ExceptionHandler(EmptyResultDataAccessException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult dataAccessException(EmptyResultDataAccessException e) {
    return responseService.getFailResult(
        Integer.parseInt(exceptionUtil.getMessage("dataNotFound.code")),
        e.getMessage() == null ? exceptionUtil.getMessage("dataNotFound.msg") : e.getMessage());
  }

  @ExceptionHandler(DataIntegrityViolationException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  protected CommonResult dataDuplicate(DataIntegrityViolationException e) {
    return responseService.getFailResult(
        Integer.parseInt(exceptionUtil.getMessage("dataDuplicate.code")),
        e.getMessage() == null ? exceptionUtil.getMessage("dataDuplicate.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomMemberNotFoundException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult memberNotFoundException(CustomMemberNotFoundException e) {
    return responseService.getFailResult(
        Integer.parseInt(exceptionUtil.getMessage("memberNotFound.code")),
        e.getMessage() == null ? exceptionUtil.getMessage("memberNotFound.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomMemberInfoNotFoundException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult memberInfoNotFoundException(CustomMemberInfoNotFoundException e) {
    return responseService.getFailResult(
        Integer.parseInt(exceptionUtil.getMessage("memberInfoNotFound.code")),
        e.getMessage() == null ? exceptionUtil.getMessage("memberInfoNotFound.msg")
            : e.getMessage());
  }

  @ExceptionHandler(CustomFileNotFoundException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public CommonResult fileNotFoundException(CustomFileNotFoundException e) {
    return responseService.getFailResult(
        Integer.parseInt(exceptionUtil.getMessage("fileNotFound.code")),
        e.getMessage() == null ? exceptionUtil.getMessage("fileNotFound.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomFileDeleteFailedException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult fileDeleteFailedException(CustomFileDeleteFailedException e) {
    return responseService.getFailResult(
        Integer.parseInt(exceptionUtil.getMessage("fileDeleteFailed.code")),
        exceptionUtil.getMessage("fileDeleteFailed.msg"));
  }

  @ExceptionHandler(CustomFileTransferFailedException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult fileTransferFailedException(CustomFileTransferFailedException e) {
    return responseService.getFailResult(
        Integer.parseInt(exceptionUtil.getMessage("fileTransferFailed.code")),
        exceptionUtil.getMessage("fileTransferFailed.msg"));
  }

  @ExceptionHandler(CustomFileEntityNotFoundException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult fileEntityNotFoundException(CustomFileEntityNotFoundException e) {
    return responseService.getFailResult(
        Integer.parseInt(exceptionUtil.getMessage("fileEntityNotFoundFailed.code")),
        exceptionUtil.getMessage("fileEntityNotFoundFailed.msg"));
  }

  @ExceptionHandler(CustomThumbnailEntityNotFoundException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  protected CommonResult CustomThumbnailEntityNotFoundException(
      CustomThumbnailEntityNotFoundException e) {
    return responseService.getFailResult(
        Integer.parseInt(exceptionUtil.getMessage("thumbnailEntityNotFoundFailed.code")),
        exceptionUtil.getMessage("thumbnailEntityNotFoundFailed.msg"));
  }

  @ExceptionHandler(CustomImageFormatException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult imageFormatException(CustomImageFormatException e) {
    return responseService.getFailResult(
        Integer.parseInt(exceptionUtil.getMessage("imageFormat.code")),
        exceptionUtil.getMessage("imageFormat.msg"));
  }

  @ExceptionHandler(CustomImageIOException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult imageIOException(CustomImageIOException e) {
    return responseService.getFailResult(Integer.parseInt(exceptionUtil.getMessage("imageIO.code")),
        exceptionUtil.getMessage("imageIO.msg"));
  }

  @ExceptionHandler(CustomInvalidImageFileException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public CommonResult invalidImageFileException(CustomInvalidImageFileException e) {
    return responseService.getFailResult(
        Integer.parseInt(exceptionUtil.getMessage("invalidImageFile.code")),
        e.getMessage() == null ? exceptionUtil.getMessage("invalidImageFile.msg") : e.getMessage());
  }

  @ExceptionHandler(CustomNumberOverflowException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  protected CommonResult numberOverflowException(CustomNumberOverflowException e) {
    return responseService.getFailResult(
        Integer.parseInt(exceptionUtil.getMessage("numberOverflow.code")),
        exceptionUtil.getMessage("numberOverflow.msg"));
  }
}