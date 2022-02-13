package keeper.project.homepage.service.posting;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import keeper.project.homepage.dto.posting.PostingDto;
import keeper.project.homepage.entity.posting.CategoryEntity;
import keeper.project.homepage.entity.member.MemberEntity;
import keeper.project.homepage.entity.member.MemberHasPostingDislikeEntity;
import keeper.project.homepage.entity.member.MemberHasPostingLikeEntity;
import keeper.project.homepage.entity.posting.PostingEntity;
import keeper.project.homepage.entity.ThumbnailEntity;
import keeper.project.homepage.exception.member.CustomMemberNotFoundException;
import keeper.project.homepage.repository.posting.CategoryRepository;
import keeper.project.homepage.repository.member.MemberHasPostingDislikeRepository;
import keeper.project.homepage.repository.member.MemberHasPostingLikeRepository;
import keeper.project.homepage.repository.member.MemberRepository;
import keeper.project.homepage.repository.posting.PostingRepository;
import keeper.project.homepage.repository.ThumbnailRepository;
import keeper.project.homepage.service.util.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class PostingService {

  private final PostingRepository postingRepository;
  private final CategoryRepository categoryRepository;
  private final MemberRepository memberRepository;
  private final ThumbnailRepository thumbnailRepository;
  private final MemberHasPostingLikeRepository memberHasPostingLikeRepository;
  private final MemberHasPostingDislikeRepository memberHasPostingDislikeRepository;
  private final AuthService authService;

  public static final Integer isNotTempPosting = 0;
  public static final Integer isTempPosting = 1;

  public List<PostingEntity> findAll(Pageable pageable) {

    List<PostingEntity> postingEntities = postingRepository.findAllByIsTemp(isNotTempPosting,
        pageable).getContent();

    for (PostingEntity postingEntity : postingEntities) {
      setWriterInfo(postingEntity);
    }
    return postingEntities;
  }

  private void setWriterInfo(PostingEntity postingEntity) {
    if (postingEntity.getCategoryId().getName().equals("비밀게시판")) {
      postingEntity.setWriter("익명");
    } else {
      postingEntity.setWriter(postingEntity.getMemberId().getNickName());
      postingEntity.setWriterId(postingEntity.getMemberId().getId());
      if (postingEntity.getMemberId().getThumbnail() != null) {
        postingEntity.setWriterThumbnailId(postingEntity.getMemberId().getThumbnail().getId());
      }
    }
  }

  public List<PostingEntity> findAllByCategoryId(Long categoryId, Pageable pageable) {

    Optional<CategoryEntity> categoryEntity = categoryRepository.findById(Long.valueOf(categoryId));
    List<PostingEntity> postingEntities = postingRepository.findAllByCategoryIdAndIsTemp(
        categoryEntity.get(), isNotTempPosting, pageable);

    for (PostingEntity postingEntity : postingEntities) {
      setWriterInfo(postingEntity);
    }

    return postingEntities;
  }

  public PostingEntity save(PostingDto dto) {

    Optional<CategoryEntity> categoryEntity = categoryRepository.findById(
        Long.valueOf(dto.getCategoryId()));
    Optional<ThumbnailEntity> thumbnailEntity = thumbnailRepository.findById(dto.getThumbnailId());
    MemberEntity memberEntity = getMemberEntityWithJWT();
    dto.setRegisterTime(new Date());
    dto.setUpdateTime(new Date());
    PostingEntity postingEntity = dto.toEntity(categoryEntity.get(), memberEntity,
        thumbnailEntity.get());

    memberEntity.getPosting().add(postingEntity);
    return postingRepository.save(postingEntity);
  }

  @Transactional
  public PostingEntity getPostingById(Long pid) {

    PostingEntity postingEntity = postingRepository.findById(pid).get();
    setWriterInfo(postingEntity);

    return postingEntity;
  }

  @Transactional
  public PostingEntity updateById(PostingDto dto, Long postingId) {
    PostingEntity tempEntity = postingRepository.findById(postingId).get();

    dto.setUpdateTime(new Date());
    dto.setCommentCount(tempEntity.getCommentCount());
    dto.setLikeCount(tempEntity.getLikeCount());
    dto.setDislikeCount(tempEntity.getDislikeCount());
    dto.setVisitCount(tempEntity.getVisitCount());

    if (tempEntity.getMemberId().getId() != getMemberEntityWithJWT().getId()) {
      throw new RuntimeException("작성자만 수정할 수 있습니다.");
    }

    tempEntity.updateInfo(dto.getTitle(), dto.getContent(),
        dto.getUpdateTime(), dto.getIpAddress(),
        dto.getAllowComment(), dto.getIsNotice(), dto.getIsSecret());

    return postingRepository.save(tempEntity);
  }

  @Transactional
  public PostingEntity updateInfoById(PostingEntity postingEntity, Long postingId) {
    PostingEntity tempEntity = postingRepository.findById(postingId).get();

    tempEntity.updateInfo(postingEntity.getTitle(), postingEntity.getContent(),
        postingEntity.getUpdateTime(), postingEntity.getIpAddress(),
        postingEntity.getAllowComment(), postingEntity.getIsNotice(), postingEntity.getIsSecret());

    return postingRepository.save(tempEntity);
  }

  @Transactional
  public int deleteById(Long postingId) {
    Optional<PostingEntity> postingEntity = postingRepository.findById(postingId);

    if (postingEntity.isPresent()) {
      MemberEntity memberEntity = memberRepository.findById(
          postingEntity.get().getMemberId().getId()).get();

      if (memberEntity.getId() != getMemberEntityWithJWT().getId()) {
        throw new RuntimeException("작성자만 삭제할 수 있습니다.");
      }

      memberEntity.getPosting().remove(postingEntity.get());
      postingRepository.delete(postingEntity.get());
      return 1;
    } else {
      return 0;
    }
  }

  @Transactional
  public List<PostingEntity> searchPosting(String type, String keyword,
      Long categoryId, Pageable pageable) {

    CategoryEntity categoryEntity = categoryRepository.findById(categoryId).get();
    List<PostingEntity> postingEntities = new ArrayList<>();
    switch (type) {
      case "T": {
        postingEntities = postingRepository.findAllByCategoryIdAndTitleContainingAndIsTemp(
            categoryEntity, keyword, isNotTempPosting, pageable);
        break;
      }
      case "C": {
        postingEntities = postingRepository.findAllByCategoryIdAndContentContainingAndIsTemp(
            categoryEntity, keyword, isNotTempPosting, pageable);
        break;
      }
      case "TC": {
        postingEntities = postingRepository.findAllByCategoryIdAndTitleContainingOrCategoryIdAndContentContainingAndIsTemp(
            categoryEntity, keyword, categoryEntity, keyword, isNotTempPosting, pageable);
        break;
      }
      case "W": {
        Optional<MemberEntity> memberEntity = memberRepository.findByNickName(keyword);
        if (!memberEntity.isPresent()) {
          break;
        }
        postingEntities = postingRepository.findAllByCategoryIdAndMemberIdAndIsTemp(categoryEntity,
            memberEntity.get(), isNotTempPosting, pageable);
        break;
      }
    }

    for (PostingEntity postingEntity : postingEntities) {
      setWriterInfo(postingEntity);
    }

    return postingEntities;
  }

  @Transactional
  public boolean isPostingLike(Long postingId, String type) {

    MemberEntity memberEntity = getMemberEntityWithJWT();
    PostingEntity postingEntity = postingRepository.findById(postingId).get();
    MemberHasPostingLikeEntity memberHasPostingLikeEntity = MemberHasPostingLikeEntity.builder()
        .memberId(memberEntity).postingId(postingEntity).build();

    if (type.equals("INC")) {
      if (postingRepository.existsByMemberHasPostingLikeEntitiesContaining(
          memberHasPostingLikeEntity)) {
        return false;
      } else {
        postingEntity.increaseLikeCount(memberHasPostingLikeEntity);
        postingRepository.save(postingEntity);
        return true;
      }
    } else {
      if (postingRepository.existsByMemberHasPostingLikeEntitiesContaining(
          memberHasPostingLikeEntity)) {
        memberHasPostingLikeRepository.deleteByMemberIdAndPostingId(memberEntity, postingEntity);
        postingEntity.decreaseLikeCount();
        postingRepository.saveAndFlush(postingEntity);
        return true;
      } else {
        return false;
      }
    }
  }

  @Transactional
  public boolean isPostingDislike(Long postingId, String type) {

    MemberEntity memberEntity = getMemberEntityWithJWT();
    PostingEntity postingEntity = postingRepository.findById(postingId).get();
    MemberHasPostingDislikeEntity memberHasPostingDislikeEntity = MemberHasPostingDislikeEntity.builder()
        .memberId(memberEntity).postingId(postingEntity).build();

    if (type.equals("INC")) {
      if (postingRepository.existsByMemberHasPostingDislikeEntitiesContaining(
          memberHasPostingDislikeEntity)) {
        return false;
      } else {
        postingEntity.increaseDislikeCount(memberHasPostingDislikeEntity);
        postingRepository.save(postingEntity);
        return true;
      }
    } else {
      if (postingRepository.existsByMemberHasPostingDislikeEntitiesContaining(
          memberHasPostingDislikeEntity)) {
        memberHasPostingDislikeRepository.deleteByMemberIdAndPostingId(memberEntity, postingEntity);
        postingEntity.decreaseDislikeCount();
        postingRepository.saveAndFlush(postingEntity);
        return true;
      } else {
        return false;
      }
    }
  }

  private MemberEntity getMemberEntityWithJWT() {
    Long memberId = authService.getMemberIdByJWT();
    Optional<MemberEntity> member = memberRepository.findById(memberId);
    if (member.isEmpty()) {
      throw new CustomMemberNotFoundException();
    }
    return member.get();
  }
}
