package keeper.project.homepage.study.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import keeper.project.homepage.util.service.auth.AuthService;
import keeper.project.homepage.util.entity.ThumbnailEntity;
import keeper.project.homepage.member.entity.MemberEntity;
import keeper.project.homepage.study.entity.StudyEntity;
import keeper.project.homepage.study.entity.StudyHasMemberEntity;
import keeper.project.homepage.util.exception.file.CustomThumbnailEntityNotFoundException;
import keeper.project.homepage.member.exception.CustomMemberNotFoundException;
import keeper.project.homepage.study.exception.CustomIpAddressNotFoundException;
import keeper.project.homepage.study.exception.CustomSeasonInvalidException;
import keeper.project.homepage.study.exception.CustomStudyIsNotMineException;
import keeper.project.homepage.study.exception.CustomStudyNotFoundException;
import keeper.project.homepage.member.repository.MemberRepository;
import keeper.project.homepage.study.repository.StudyHasMemberRepository;
import keeper.project.homepage.study.repository.StudyRepository;
import keeper.project.homepage.member.dto.UserMemberDto;
import keeper.project.homepage.study.dto.StudyDto;
import keeper.project.homepage.study.dto.StudyYearSeasonDto;
import keeper.project.homepage.study.mapper.StudyMapper;
import keeper.project.homepage.util.image.preprocessing.ImageCenterCropping;
import keeper.project.homepage.util.image.preprocessing.ImageSize;
import keeper.project.homepage.util.service.FileService;
import keeper.project.homepage.util.service.ThumbnailService;
import keeper.project.homepage.util.service.ThumbnailService.ThumbType;
import lombok.RequiredArgsConstructor;
import org.mapstruct.factory.Mappers;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class StudyService {

  private static final Integer FIRST_SEMESTER = 1;
  private static final Integer SUMMER_SESSION = 2;
  private static final Integer SECOND_SEMESTER = 3;
  private static final Integer WINTER_SESSION = 4;

  private final StudyRepository studyRepository;
  private final StudyHasMemberRepository studyHasMemberRepository;
  private final ThumbnailService thumbnailService;
  private final MemberRepository memberRepository;
  private final FileService fileService;
  private final AuthService authService;
  private final StudyMapper studyMapper = Mappers.getMapper(StudyMapper.class);

  public List<StudyYearSeasonDto> getAllStudyYearsAndSeasons() {
    List<StudyYearSeasonDto> studyYearSeasonDtoList = new ArrayList<>();

    List<Integer> years = studyRepository.findDistinctYear();
    years.sort(Comparator.naturalOrder()); // 년도 별로 오름차순 정렬
    for (Integer year : years) {
      StudyYearSeasonDto studyYearSeasonDto = new StudyYearSeasonDto();
      studyYearSeasonDto.setYear(year);

      List<Integer> season = studyRepository.findDistinctSeasonByYear(year);
      season.sort(Comparator.naturalOrder());
      studyYearSeasonDto.setSeason(season); // 각 년도마다 시즌별로 오름차순 정렬

      studyYearSeasonDtoList.add(studyYearSeasonDto);
    }
    return studyYearSeasonDtoList;
  }

  public List<StudyDto> getAllStudyList(Integer year, Integer season) {

    checkSeasonValidate(season);
    List<StudyEntity> studyEntities = studyRepository.findAllByYearAndSeason(year, season);

    List<StudyDto> studyDtos = new ArrayList<>();
    for (StudyEntity studyEntity : studyEntities) {
      studyDtos.add(studyMapper.toDto(studyEntity));
    }
    return studyDtos;
  }

  private void checkSeasonValidate(Integer season) {

    if (!FIRST_SEMESTER.equals(season) && !SUMMER_SESSION.equals(season) &&
        !SECOND_SEMESTER.equals(season) && !WINTER_SESSION.equals(season)) {
      throw new CustomSeasonInvalidException();
    }
  }

  private void checkIpAddressExist(String ipAddress) {

    if (ipAddress.isEmpty()) {
      throw new CustomIpAddressNotFoundException();
    }
  }

  @Transactional
  public StudyDto createStudy(
      StudyDto studyDto, MultipartFile thumbnail, List<Long> memberIdList) {

    // request 값 유효성 검사
    checkSeasonValidate(studyDto.getSeason());
    checkIpAddressExist(studyDto.getIpAddress());

    // 스터디장 memberList에 추가
    MemberEntity headMember = authService.getMemberEntityWithJWT();
    memberIdList.remove(headMember.getId());
    memberIdList.add(0, headMember.getId());

    // entity에서 register time을 set 할 수 없게 하기 위해
    // dto에서 register time을 저장 후 entity로 변환
    studyDto.setRegisterTime(LocalDateTime.now());

    StudyEntity studyEntity = studyMapper.toEntity(studyDto);

    ThumbnailEntity studyThumbnail = saveThumbnail(studyDto.getIpAddress(), thumbnail);
    studyEntity.setThumbnail(studyThumbnail);
    studyEntity.setHeadMember(headMember);
    studyEntity.setMemberNumber(0);
    studyRepository.save(studyEntity);

    for (Long memberId : memberIdList) {
      if (memberId != null) {
        addStudyMember(memberId, studyEntity);
      }
    }

    return studyMapper.toDto(studyEntity);
  }

  private ThumbnailEntity saveThumbnail(String ipAddress, MultipartFile thumbnail) {
    ThumbnailEntity thumbnailEntity = thumbnailService.save(ThumbType.StudyThumbnail,
        new ImageCenterCropping(ImageSize.STUDY), thumbnail, ipAddress);

    if (thumbnailEntity == null) {
      throw new CustomThumbnailEntityNotFoundException();
    }
    return thumbnailEntity;
  }

  private void deletePrevThumbnail(Long studyThumbnailId) {
    if (studyThumbnailId != null) {
      ThumbnailEntity prevThumbnail = thumbnailService.find(studyThumbnailId);
      thumbnailService.delete(prevThumbnail.getId());
    }
  }

  @Transactional
  public StudyDto modifyStudy(Long studyId, StudyDto studyDto, MultipartFile thumbnail,
      List<Long> newMemberIdList) {

    checkSeasonValidate(studyDto.getSeason());
    checkIpAddressExist(studyDto.getIpAddress());

    Long myId = authService.getMemberIdByJWT();
    StudyEntity studyEntity = studyRepository.findById(studyId)
        .orElseThrow(CustomStudyNotFoundException::new);

    checkStudyIsMine(myId, studyEntity);

    modifyStudyMembers(studyId, newMemberIdList, myId, studyEntity);

    Long prevStudyThumbnailId = null;
    if (studyEntity.getThumbnail() != null) {
      prevStudyThumbnailId = studyEntity.getThumbnail().getId();
    }

    ThumbnailEntity studyThumbnail = saveThumbnail(studyDto.getIpAddress(), thumbnail);
    studyEntity.setThumbnail(studyThumbnail);
    studyEntity.setTitle(studyDto.getTitle());
    studyEntity.setInformation(studyDto.getInformation());
    studyEntity.setYear(studyDto.getYear());
    studyEntity.setSeason(studyDto.getSeason());
    studyEntity.setGitLink(studyDto.getGitLink());
    studyEntity.setNoteLink(studyDto.getNoteLink());
    studyEntity.setEtcLink(studyDto.getEtcLink());

    studyRepository.save(studyEntity);

    if (prevStudyThumbnailId != null) {
      deletePrevThumbnail(prevStudyThumbnailId);
    }

    return studyMapper.toDto(studyEntity);
  }

  private void modifyStudyMembers(Long studyId, List<Long> newMemberIdList, Long headMemberId,
      StudyEntity studyEntity) {
    List<Long> originMemberIdList = getOriginMemberIdList(studyId);
    addNewMembers(newMemberIdList, headMemberId, studyEntity, originMemberIdList);
    removeOriginMembers(newMemberIdList, headMemberId, studyEntity, originMemberIdList);
  }

  private void removeOriginMembers(List<Long> newMemberIdList, Long headMemberId,
      StudyEntity studyEntity, List<Long> originMemberIdList) {
    for (Long originMemberId : originMemberIdList) {
      if (originMemberId.equals(headMemberId)) {
        continue;
      }

      if (!newMemberIdList.contains(originMemberId)) {
        removeStudyMember(originMemberId, studyEntity);
      }
    }
  }

  private void addNewMembers(List<Long> newMemberIdList, Long headMemberId,
      StudyEntity studyEntity, List<Long> originMemberIdList) {
    for (Long newMemberId : newMemberIdList) {
      if (newMemberId.equals(headMemberId)) {
        continue;
      }

      if (!originMemberIdList.contains(newMemberId)) {
        addStudyMember(newMemberId, studyEntity);
      }
    }
  }

  private List<Long> getOriginMemberIdList(Long studyId) {
    List<StudyHasMemberEntity> studyHasMemberEntities = studyHasMemberRepository
        .findAllByStudyId(studyId);
    return studyHasMemberEntities.stream()
        .map(StudyHasMemberEntity::getMember)
        .map(MemberEntity::getId).toList();
  }

  private void checkStudyIsMine(Long myId, StudyEntity studyEntity) {
    if (!myId.equals(studyEntity.getHeadMember().getId())) {
      throw new CustomStudyIsNotMineException();
    }
  }

  @Transactional
  public List<UserMemberDto> addStudyMember(Long studyId, Long memberId) {
    Long myId = authService.getMemberIdByJWT();
    StudyEntity studyEntity = studyRepository.findById(studyId)
        .orElseThrow(CustomStudyNotFoundException::new);

    checkStudyIsMine(myId, studyEntity);

    if (!isHeadMember(myId, memberId)) {
      addStudyMember(memberId, studyEntity);
    }

    return getStudyMemberDtoList(studyEntity);
  }

  private void addStudyMember(Long memberId, StudyEntity studyEntity) {
    if (isAlreadyStudyMember(studyEntity, memberId)) {
      return;
    }
    MemberEntity addMemberEntity = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomMemberNotFoundException(memberId));
    StudyHasMemberEntity studyHasMemberEntity = StudyHasMemberEntity.builder()
        .member(addMemberEntity)
        .study(studyEntity)
        .registerTime(LocalDateTime.now())
        .build();
    studyEntity.getStudyHasMemberEntities().add(studyHasMemberEntity);
    addMemberEntity.getStudyHasMemberEntities().add(studyHasMemberEntity);
    studyHasMemberRepository.save(studyHasMemberEntity);

    studyEntity.setMemberNumber(studyEntity.getMemberNumber() + 1);
    studyRepository.save(studyEntity);
  }

  @Transactional
  public List<UserMemberDto> removeStudyMember(Long studyId, Long memberId) {
    Long myId = authService.getMemberIdByJWT();
    StudyEntity studyEntity = studyRepository.findById(studyId)
        .orElseThrow(CustomStudyNotFoundException::new);

    checkStudyIsMine(myId, studyEntity);

    if (!isHeadMember(myId, memberId)) {
      removeStudyMember(memberId, studyEntity);
    }

    return getStudyMemberDtoList(studyEntity);
  }

  private List<UserMemberDto> getStudyMemberDtoList(StudyEntity studyEntity) {
    List<UserMemberDto> memberDtoList = new ArrayList<>();
    List<StudyHasMemberEntity> studyHasMemberEntities = studyEntity.getStudyHasMemberEntities();
    studyHasMemberEntities.sort(Comparator.comparing(StudyHasMemberEntity::getRegisterTime));
    for (StudyHasMemberEntity studyHasMember : studyHasMemberEntities) {
      UserMemberDto memberDto = new UserMemberDto();
      memberDto.initWithEntity(studyHasMember.getMember());
      memberDtoList.add(memberDto);
    }
    return memberDtoList;
  }

  private void removeStudyMember(Long memberId, StudyEntity studyEntity) {
    if (!isAlreadyStudyMember(studyEntity, memberId)) {
      return;
    }
    MemberEntity removeMemberEntity = memberRepository.findById(memberId)
        .orElseThrow(() -> new CustomMemberNotFoundException(memberId));
    studyEntity.getStudyHasMemberEntities().removeIf(studyHasMemberEntity -> (
        memberId.equals(studyHasMemberEntity.getMember().getId())
    ));
    removeMemberEntity.getStudyHasMemberEntities().removeIf(studyHasMemberEntity -> (
        memberId.equals(studyHasMemberEntity.getMember().getId())
    ));
    studyHasMemberRepository.deleteByMember(removeMemberEntity);

    studyEntity.setMemberNumber(studyEntity.getMemberNumber() - 1);
    studyRepository.save(studyEntity);
  }

  private boolean isAlreadyStudyMember(StudyEntity studyEntity, Long memberId) {
    for (StudyHasMemberEntity studyHasMemberEntity : studyEntity.getStudyHasMemberEntities()) {
      if (studyHasMemberEntity.getMember().getId().equals(memberId)) {
        return true;
      }
    }
    return false;
  }

  private boolean isHeadMember(Long myId, Long memberId) {
    return myId.equals(memberId);
  }
}
